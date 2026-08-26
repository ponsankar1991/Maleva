package my.maleva.api.module.inventory.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.inventory.dto.*;
import my.maleva.api.module.inventory.entity.AssetCondition;
import my.maleva.api.module.inventory.entity.AssetStatus;
import my.maleva.api.module.inventory.entity.InventoryItem;
import my.maleva.api.module.inventory.entity.ItemType;
import my.maleva.api.module.inventory.repository.InventoryAssetRepository;
import my.maleva.api.module.inventory.repository.InventoryItemRepository;
import my.maleva.api.module.inventory.service.InventoryItemService;
import my.maleva.api.module.inventory.service.InventoryService;
import my.maleva.api.module.inventory.service.RepairableAssetService;
import my.maleva.api.module.billing.billorder.repository.BillsOrderDetailsRepository;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.productmaster.entity.ProductMasterCStock;
import my.maleva.api.module.productmaster.repository.ProductMasterCStockRepository;
import my.maleva.api.module.productmaster.repository.ProductMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Catalogue CRUD for workshop inventory items.
 *
 * Creating an item touches four tables and is deliberately a single transaction:
 * ProductMaster (the product record other modules select from), InventoryItem
 * (workshop configuration), ProductMasterCStock (the balance row) and, when there
 * is an opening balance, InventoryTransaction (the movement that explains it).
 */
@Service
@Transactional
public class InventoryItemServiceImpl implements InventoryItemService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryItemServiceImpl.class);

    private static final String STATUS_IN_STOCK = "IN_STOCK";
    private static final String STATUS_LOW_STOCK = "LOW_STOCK";
    private static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK";

    @Autowired private InventoryItemRepository itemRepository;
    @Autowired private InventoryAssetRepository assetRepository;
    @Autowired private ProductMasterRepository productMasterRepository;
    @Autowired private ProductMasterCStockRepository cstockRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private RepairableAssetService repairableAssetService;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private BillsOrderDetailsRepository billsOrderDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------------- ensure store item

    @Override
    @Transactional
    public InventoryItem ensureStoreItem(Integer companyRefId, Integer productRefId, String itemType,
                                         Double unitCost, Integer defaultSupplierRefId, String modifiedBy) {
        return ensureStoreItem(companyRefId, productRefId, itemType, unitCost,
                defaultSupplierRefId, modifiedBy, null);
    }

    @Override
    @Transactional
    public InventoryItem ensureStoreItem(Integer companyRefId, Integer productRefId, String itemType,
                                         Double unitCost, Integer defaultSupplierRefId, String modifiedBy,
                                         String baseUomOverride) {

        Optional<InventoryItem> existing = itemRepository
                .findByCompanyRefIdAndProductRefId(companyRefId, productRefId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ProductMaster product = productMasterRepository.findById(productRefId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with ID: " + productRefId));

        // PART when unstated: it is tracked by plain quantity, which is the only
        // thing a receipt can be sure of. The caller normally does say, because the
        // person receiving the goods can see whether they are holding a filter or
        // a drum of oil, and the two are issued in different units.
        ItemType type = itemType == null || itemType.trim().isEmpty()
                ? ItemType.PART
                : parseType(itemType);

        // The unit the caller chose wins - on a goods receipt the person is
        // holding the box. Failing that, the product's own UOM if the UOM master
        // recognises it, so the item reads in the same unit the buyer ordered
        // in; failing that, the type's default.
        String suppliedUom = baseUomOverride == null || baseUomOverride.trim().isEmpty()
                ? (product.getUomCode() == null ? null : String.valueOf(product.getUomCode()))
                : baseUomOverride.trim();
        String baseUom = displayUom(uomLookup(companyRefId), defaultUom(suppliedUom, type));

        InventoryItem item = itemRepository.save(InventoryItem.builder()
                .companyRefId(companyRefId)
                .productRefId(productRefId)
                .itemType(type)
                .baseUom(baseUom)
                .unitCost(unitCost)
                .defaultSupplierRefId(defaultSupplierRefId)
                .active(1)
                .modifiedBy(modifiedBy)
                .build());

        // Movements are written against this row, so it has to exist before any
        // quantity is recorded or the balance would have nothing to hang off.
        if (cstockRepository.findByCompanyRefIdAndProductRefId(companyRefId, productRefId).isEmpty()) {
            cstockRepository.save(ProductMasterCStock.builder()
                    .companyRefId(companyRefId)
                    .productRefId(productRefId)
                    .cstock(0.0)
                    .modifiedBy(modifiedBy)
                    .build());
        }

        logger.info("Store item auto-created on receipt: company={}, product={} ({}), uom={}, supplier={}",
                companyRefId, productRefId, product.getProdCode(), baseUom, defaultSupplierRefId);
        return item;
    }

    @Override
    @Transactional
    public InventoryTransactionDto receivePurchaseLine(ReceivePurchaseLineRequestDto request) {
        // Catalogue first: a movement written before the store record exists would
        // hold quantity against a product no inventory screen lists.
        InventoryItem item = ensureStoreItem(
                request.getCompanyRefId(),
                request.getProductRefId(),
                request.getItemType(),
                request.getUnitCost() == null ? null : request.getUnitCost().doubleValue(),
                request.getSupplierRefId(),
                request.getCreatedBy(),
                request.getBaseUom());

        // An item the store already carries keeps what it was catalogued as
        // unless this receipt says otherwise. It often does: the first receipt
        // guessed the kind or inherited a stale unit from ProductMaster, and the
        // person now holding the goods can see what they really are. Applied
        // before the serialised guard below, so switching an item to a
        // serial-tracked kind is refused with the same clear message.
        applyReceiptOverrides(item, request);

        // A serialised item's on-hand is counted from its registered units, not from
        // the quantity balance, so adding to that balance would move a number no
        // screen reads and leave the item still showing nothing in stock. Refusing
        // is the honest outcome: these need a serial per unit, which one order line
        // carrying one serial box cannot supply.
        if (item.getItemType().isSerialised()) {
            throw new InvalidRequestException(
                    "'" + item.getItemType().name() + "' items are tracked by serial number. "
                  + "Register each unit under Inventory instead of receiving a quantity here.");
        }

        // Claimed only now, after every reason to refuse has already had its turn -
        // a line rejected for being serialised, or for any other reason above, must
        // not come out of this method marked as received when nothing moved.
        // The WHERE clause inside claimForReceiving is the actual guard: it matches
        // only a line that has never been claimed, so a second click - or two
        // clicks arriving together - cannot both succeed. See its Javadoc for why
        // that holds even under real concurrency, not just under a single click.
        int claimed = billsOrderDetailsRepository.claimForReceiving(
                request.getBillsOrderDetailsRefId(), request.getQuantity());
        if (claimed == 0) {
            throw new InvalidRequestException(
                    "This order line has already been received into stock. "
                  + "Reload the order to see the current quantity on hand.");
        }

        // The item's unit cost follows the latest priced receipt. Not an average -
        // averaging needs the valued balance the schema does not yet track - but
        // "what we last paid" is the figure the workshop charges a job from, and
        // updating it here is what turns an item catalogued at RM 0 before prices
        // existed into one the job order screen can actually cost a line with.
        if (request.getUnitCost() != null && request.getUnitCost().signum() > 0) {
            double latest = request.getUnitCost().doubleValue();
            if (item.getUnitCost() == null || Double.compare(item.getUnitCost(), latest) != 0) {
                item.setUnitCost(latest);
                item.setModifiedBy(request.getCreatedBy());
                itemRepository.save(item);
            }
        }

        return inventoryService.stockIn(StockInRequestDto.builder()
                .companyRefId(request.getCompanyRefId())
                .productRefId(request.getProductRefId())
                .quantity(request.getQuantity())
                .unitCost(request.getUnitCost())
                .referenceType("PURCHASE_ORDER")
                .referenceId(request.getPurchaseOrderRefId())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build());
    }

    /**
     * Re-catalogues an existing store item when the receipt states a different
     * kind or unit.
     *
     * Only ever moves the item to what the receipt says; a request that leaves
     * these blank changes nothing. The unit is stored in display form, the same
     * way ensureStoreItem writes it, so the two paths cannot drift apart.
     */
    private void applyReceiptOverrides(InventoryItem item, ReceivePurchaseLineRequestDto request) {
        boolean changed = false;

        String requestedType = trimOrNull(request.getItemType());
        if (requestedType != null) {
            ItemType parsed = parseType(requestedType);
            if (parsed != item.getItemType()) {
                logger.info("Re-cataloguing item {} from {} to {} on receipt",
                        item.getId(), item.getItemType(), parsed);
                item.setItemType(parsed);
                changed = true;
            }
        }

        String requestedUom = trimOrNull(request.getBaseUom());
        if (requestedUom != null) {
            String resolved = displayUom(uomLookup(item.getCompanyRefId()),
                    defaultUom(requestedUom, item.getItemType()));
            if (resolved != null && !resolved.equalsIgnoreCase(item.getBaseUom())) {
                logger.info("Changing item {} unit from {} to {} on receipt",
                        item.getId(), item.getBaseUom(), resolved);
                item.setBaseUom(resolved);
                changed = true;
            }
        }

        if (changed) {
            item.setModifiedBy(request.getCreatedBy());
            itemRepository.save(item);
        }
    }

    // ------------------------------------------------------------------ create

    @Override
    @Transactional
    public InventoryItemResponseDto create(InventoryItemRequestDto request) {
        ItemType type = parseType(request.getItemType());
        // Resolved once: it is both the item's stock-keeping unit and what the
        // new product's UOM_Code is matched against.
        String baseUom = displayUom(uomLookup(request.getCompanyRefId()),
                defaultUom(request.getBaseUom(), type));

        String serial = request.getFirstSerialNo() == null ? null : request.getFirstSerialNo().trim();
        if (serial != null && serial.isEmpty()) {
            serial = null;
        }
        // A serial is optional even for a serialised type. Setting up the item is
        // cataloguing it - deciding it exists, what it fits and where it lives -
        // which is a separate act from having a unit in your hand to register.
        // Requiring one here forced a made-up serial whenever the catalogue was
        // built ahead of the stock, and a made-up serial is worse than none: it
        // becomes a unit the system thinks is on the shelf. The item is created
        // with zero units, and units are registered from the item page as they
        // actually arrive.
        if (!type.isSerialised() && serial != null) {
            throw new InvalidRequestException(
                    "Serial numbers only apply to ASSET and TOOL items, not " + type);
        }

        ProductMaster product = request.getProductRefId() != null
                ? useExistingProduct(request)
                : createProduct(request, baseUom);

        InventoryItem item = itemRepository.save(InventoryItem.builder()
                .companyRefId(request.getCompanyRefId())
                .productRefId(product.getId())
                .itemType(type)
                .category(trimOrNull(request.getCategory()))
                .brand(trimOrNull(request.getBrand()))
                .fitsModel(trimOrNull(request.getFitsModel()))
                .baseUom(baseUom)
                .minQty(type.isSerialised() ? null : nvl(request.getMinQty()))
                .reorderQty(type.isSerialised() ? null : nvl(request.getReorderQty()))
                .unitCost(nvl(request.getUnitCost()))
                .storageLocation(trimOrNull(request.getStorageLocation()))
                .binCode(trimOrNull(request.getBinCode()))
                .defaultSupplierRefId(request.getDefaultSupplierRefId())
                .remarks(trimOrNull(request.getRemarks()))
                .active(1)
                .modifiedBy(request.getModifiedBy())
                .build());

        // A balance row must exist before any movement is recorded against it.
        // An existing product usually has one already - creating a second would
        // break the one-row-per-company-and-product rule, so only add if missing.
        if (cstockRepository.findByCompanyRefIdAndProductRefId(
                request.getCompanyRefId(), product.getId()).isEmpty()) {
            cstockRepository.save(ProductMasterCStock.builder()
                    .companyRefId(request.getCompanyRefId())
                    .productRefId(product.getId())
                    .cstock(0.0)
                    .modifiedBy(request.getModifiedBy())
                    .build());
        }

        if (type.isSerialised() && serial != null) {
            repairableAssetService.registerAsset(RegisterAssetRequestDto.builder()
                    .companyRefId(request.getCompanyRefId())
                    .productRefId(product.getId())
                    .serialNo(serial)
                    .remarks("First unit registered with item")
                    .createdBy(request.getModifiedBy())
                    .build());
        } else if (request.getOpeningQty() != null
                && request.getOpeningQty().compareTo(BigDecimal.ZERO) > 0) {
            inventoryService.stockIn(StockInRequestDto.builder()
                    .companyRefId(request.getCompanyRefId())
                    .productRefId(product.getId())
                    .quantity(request.getOpeningQty())
                    .referenceType("OPENING")
                    .remarks(trimOrNull(request.getRemarks()))
                    .createdBy(request.getModifiedBy())
                    .build());
        }

        logger.info("Inventory item created: company={}, code={}, type={}, productRefId={}, reusedProduct={}",
                request.getCompanyRefId(), product.getProdCode(), type, product.getId(),
                request.getProductRefId() != null);

        // Push the inserts and detach everything, so the item is re-read with its
        // ProductMaster and Supplier associations resolved. Without this the freshly
        // saved instance is returned straight from the persistence context with those
        // associations still null, and the response would carry no item code or name.
        entityManager.flush();
        entityManager.clear();

        return toDetail(itemRepository.findById(item.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory item not found after create, id: " + item.getId())));
    }

    /**
     * Bring a product that already exists in ProductMaster into the workshop store.
     * Its code and name are left exactly as they are, so the product other modules
     * already reference by id does not change underneath them.
     */
    private ProductMaster useExistingProduct(InventoryItemRequestDto request) {
        ProductMaster product = productMasterRepository.findById(request.getProductRefId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with ID: " + request.getProductRefId()));

        if (!request.getCompanyRefId().equals(product.getCompanyRefId())) {
            throw new InvalidRequestException(
                    "Product " + product.getProdCode() + " belongs to another company");
        }
        if (itemRepository.existsByCompanyRefIdAndProductRefId(
                request.getCompanyRefId(), product.getId())) {
            throw new InvalidRequestException("Product '" + product.getProdCode()
                    + "' is already set up in the workshop store. Open it from the inventory"
                    + " list to change its settings.");
        }
        return product;
    }

    /** Create a brand new product along with its workshop settings. */
    private ProductMaster createProduct(InventoryItemRequestDto request, String baseUom) {
        String code = request.getItemCode().trim();
        if (productMasterRepository.existsByCompanyRefIdAndProdCode(request.getCompanyRefId(), code)) {
            throw new InvalidRequestException("Item Code '" + code + "' already exists for this"
                    + " company. Pick it from the existing product list instead of creating"
                    + " it again.");
        }

        LocalDateTime now = LocalDateTime.now();
        double cost = request.getUnitCost() == null ? 0.0 : request.getUnitCost();

        // Every column below is NOT NULL in ProductMaster. The table has DEFAULT
        // constraints for several of them, but a default only applies when the
        // column is omitted from the INSERT - Hibernate always lists every mapped
        // column, so a null here becomes an explicit NULL and the insert is
        // rejected. They are therefore all set explicitly.
        return productMasterRepository.save(ProductMaster.builder()
                .companyRefId(request.getCompanyRefId())
                .prodCode(code)
                .pcodeDigits(numericCodeDigits(code))
                .pname(request.getItemName().trim())
                .printName(request.getItemName().trim())
                .taxCode(resolveTaxCode(request))
                .uomCode(resolveUomCode(request, baseUom))
                .mrp(0.0)
                .purchaseRate(cost)
                .landingCost(cost)
                .salesRate(0.0)
                .saleRateType(false)
                .sorting(0)
                .activestatus(1)
                .isProduct(1)
                .createdDate(now)
                .modifiedDate(now)
                .modifiedBy(request.getModifiedBy())
                .build());
    }

    // ------------------------------------------------------------------ update

    @Override
    @Transactional
    public InventoryItemResponseDto update(Integer id, InventoryItemRequestDto request) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with ID: " + id));

        ItemType type = parseType(request.getItemType());
        if (type != item.getItemType()) {
            throw new InvalidRequestException("Item type cannot be changed after creation. "
                    + "This item is tracked as " + item.getItemType()
                    + "; create a separate item to track it as " + type + ".");
        }

        ProductMaster product = productMasterRepository.findById(item.getProductRefId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ProductMaster not found with ID: " + item.getProductRefId()));

        String code = request.getItemCode().trim();
        if (!code.equalsIgnoreCase(product.getProdCode())
                && productMasterRepository.existsByCompanyRefIdAndProdCode(item.getCompanyRefId(), code)) {
            throw new InvalidRequestException("Item Code '" + code + "' already exists for this company");
        }

        product.setProdCode(code);
        product.setPname(request.getItemName().trim());
        product.setPurchaseRate(request.getUnitCost());
        product.setModifiedBy(request.getModifiedBy());
        product.setModifiedDate(LocalDateTime.now());
        productMasterRepository.save(product);

        item.setCategory(trimOrNull(request.getCategory()));
        item.setBrand(trimOrNull(request.getBrand()));
        item.setFitsModel(trimOrNull(request.getFitsModel()));
        item.setBaseUom(displayUom(uomLookup(item.getCompanyRefId()),
                defaultUom(request.getBaseUom(), type)));
        if (!type.isSerialised()) {
            item.setMinQty(nvl(request.getMinQty()));
            item.setReorderQty(nvl(request.getReorderQty()));
        }
        item.setUnitCost(nvl(request.getUnitCost()));
        item.setStorageLocation(trimOrNull(request.getStorageLocation()));
        item.setBinCode(trimOrNull(request.getBinCode()));
        item.setDefaultSupplierRefId(request.getDefaultSupplierRefId());
        item.setRemarks(trimOrNull(request.getRemarks()));
        item.setModifiedBy(request.getModifiedBy());
        itemRepository.save(item);

        logger.info("Inventory item updated: id={}, code={}", id, code);
        return toDetail(item);
    }

    // ------------------------------------------------------------------ select

    @Override
    public InventoryItemResponseDto getById(Integer id) {
        return toDetail(itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with ID: " + id)));
    }

    @Override
    public InventoryItemResponseDto getByProduct(Integer companyRefId, Integer productRefId) {
        return toDetail(itemRepository.findByCompanyRefIdAndProductRefId(companyRefId, productRefId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No inventory item configured for product " + productRefId)));
    }

    @Override
    public List<InventoryItemListDto> search(Integer companyRefId, String itemType, String search) {
        String term = (search == null || search.trim().isEmpty())
                ? "%"
                : "%" + search.trim().toLowerCase() + "%";

        List<InventoryItem> items = (itemType == null || itemType.trim().isEmpty())
                ? itemRepository.search(companyRefId, term)
                : itemRepository.searchByType(companyRefId, parseType(itemType), term);

        return toListRows(companyRefId, items);
    }

    @Override
    public List<InventoryItemListDto> getLowStock(Integer companyRefId) {
        return toListRows(companyRefId, itemRepository.findLowStock(companyRefId));
    }

    @Override
    public List<UomOptionDto> getUomOptions(Integer companyRefId) {
        return jdbcTemplate.query(
                "SELECT Id, Code, Description FROM UOM WHERE CompanyRefId = ? AND Active = 1"
                        + " ORDER BY Description",
                (rs, rowNum) -> UomOptionDto.builder()
                        .uomCode(rs.getInt("Id"))
                        .code(rs.getString("Code"))
                        .description(rs.getString("Description"))
                        .build(),
                companyRefId);
    }

    @Override
    public List<AvailableProductDto> getAvailableProducts(Integer companyRefId) {
        return itemRepository.findProductsWithoutInventorySettings(companyRefId)
                .stream()
                .map(p -> AvailableProductDto.builder()
                        .productRefId(p.getId())
                        .prodCode(p.getProdCode())
                        .pname(p.getPname())
                        .uomCode(p.getUomCode())
                        .purchaseRate(p.getPurchaseRate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deactivate(Integer id, String modifiedBy) {
        Optional<InventoryItem> found = itemRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }
        InventoryItem item = found.get();
        item.setActive(0);
        item.setModifiedBy(modifiedBy);
        itemRepository.save(item);

        productMasterRepository.findById(item.getProductRefId()).ifPresent(p -> {
            p.setActivestatus(0);
            p.setModifiedBy(modifiedBy);
            p.setModifiedDate(LocalDateTime.now());
            productMasterRepository.save(p);
        });

        logger.info("Inventory item deactivated: id={}", id);
        return true;
    }

    // ------------------------------------------------------------------ mapping

    /**
     * Builds list rows using two batch queries (balances, unit counts) rather than
     * per-row lookups, so the list screen cost does not grow with the catalogue.
     */
    private List<InventoryItemListDto> toListRows(Integer companyRefId, List<InventoryItem> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> productIds = items.stream()
                .map(InventoryItem::getProductRefId).collect(Collectors.toList());

        Map<Integer, Double> balances = cstockRepository
                .findByCompanyRefIdAndProductRefIdIn(companyRefId, productIds).stream()
                .collect(Collectors.toMap(ProductMasterCStock::getProductRefId,
                        s -> s.getCstock() == null ? 0.0 : s.getCstock(), (a, b) -> a));

        Map<Integer, Map<AssetStatus, Long>> unitCounts = loadUnitCounts(companyRefId, productIds);
        // Loaded once for the page rather than per row.
        Map<String, String> uomNames = uomLookup(companyRefId);

        return items.stream().map(i -> {
            Map<AssetStatus, Long> counts = unitCounts.getOrDefault(i.getProductRefId(), Map.of());
            BigDecimal onHand = onHandOf(i, balances, counts);
            return InventoryItemListDto.builder()
                    .id(i.getId())
                    .productRefId(i.getProductRefId())
                    .itemCode(i.getProductMaster() != null ? i.getProductMaster().getProdCode() : null)
                    .itemName(i.getProductMaster() != null ? i.getProductMaster().getPname() : null)
                    .itemType(i.getItemType().name())
                    .serialised(i.getItemType().isSerialised())
                    .category(i.getCategory())
                    .baseUom(displayUom(uomNames, i.getBaseUom()))
                    .onHand(onHand)
                    .minQty(i.getMinQty())
                    .unitCost(i.getUnitCost())
                    .stockValue(valueOf(onHand, i.getUnitCost()))
                    .storageLocation(i.getStorageLocation())
                    .stockStatus(statusOf(i, onHand))
                    .totalUnits(i.getItemType().isSerialised() ? totalUnits(counts) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    private InventoryItemResponseDto toDetail(InventoryItem i) {
        Double balance = cstockRepository
                .findByCompanyRefIdAndProductRefId(i.getCompanyRefId(), i.getProductRefId())
                .stream().findFirst().map(ProductMasterCStock::getCstock).orElse(0.0);

        Map<AssetStatus, Long> counts = i.getItemType().isSerialised()
                ? loadUnitCounts(i.getCompanyRefId(), List.of(i.getProductRefId()))
                    .getOrDefault(i.getProductRefId(), Map.of())
                : Map.of();

        // How much of the shelf stock is reconditioned rather than new. Only
        // meaningful for serialised items - a bulk part has no unit identity.
        Map<AssetCondition, Long> conditions = i.getItemType().isSerialised()
                ? loadConditionCounts(i.getCompanyRefId(), List.of(i.getProductRefId()))
                    .getOrDefault(i.getProductRefId(), Map.of())
                : Map.of();

        BigDecimal onHand = i.getItemType().isSerialised()
                ? BigDecimal.valueOf(counts.getOrDefault(AssetStatus.AVAILABLE, 0L))
                : BigDecimal.valueOf(balance == null ? 0.0 : balance);

        // The product's accounting unit and tax, so the detail screen can show
        // what was actually written to ProductMaster rather than only the
        // workshop's own stock-keeping label.
        Integer productUomCode = i.getProductMaster() != null ? i.getProductMaster().getUomCode() : null;
        Integer productTaxCode = i.getProductMaster() != null ? i.getProductMaster().getTaxCode() : null;

        String uomLabel = displayUom(uomLookup(i.getCompanyRefId()), i.getBaseUom());

        return InventoryItemResponseDto.builder()
                .id(i.getId())
                .companyRefId(i.getCompanyRefId())
                .productRefId(i.getProductRefId())
                .itemCode(i.getProductMaster() != null ? i.getProductMaster().getProdCode() : null)
                .itemName(i.getProductMaster() != null ? i.getProductMaster().getPname() : null)
                .itemType(i.getItemType().name())
                .serialised(i.getItemType().isSerialised())
                .category(i.getCategory())
                .brand(i.getBrand())
                .fitsModel(i.getFitsModel())
                .baseUom(uomLabel)
                .uomCode(productUomCode)
                .uomName(lookupName("UOM", productUomCode))
                .taxCode(productTaxCode)
                .taxName(lookupName("TaxMaster", productTaxCode))
                .minQty(i.getMinQty())
                .reorderQty(i.getReorderQty())
                .unitCost(i.getUnitCost())
                .storageLocation(i.getStorageLocation())
                .binCode(i.getBinCode())
                .defaultSupplierRefId(i.getDefaultSupplierRefId())
                .defaultSupplierName(i.getDefaultSupplier() != null
                        ? i.getDefaultSupplier().getSupplierName() : null)
                .remarks(i.getRemarks())
                .active(i.getActive())
                .onHand(onHand)
                .stockValue(valueOf(onHand, i.getUnitCost()))
                .stockStatus(statusOf(i, onHand))
                .totalUnits(i.getItemType().isSerialised() ? totalUnits(counts) : null)
                .availableUnits(i.getItemType().isSerialised()
                        ? counts.getOrDefault(AssetStatus.AVAILABLE, 0L).intValue() : null)
                .installedUnits(i.getItemType().isSerialised()
                        ? counts.getOrDefault(AssetStatus.INSTALLED, 0L).intValue() : null)
                .underRepairUnits(i.getItemType().isSerialised()
                        ? counts.getOrDefault(AssetStatus.UNDER_REPAIR, 0L).intValue() : null)
                .awaitingReconUnits(i.getItemType().isSerialised()
                        ? counts.getOrDefault(AssetStatus.AWAITING_RECON, 0L).intValue() : null)
                .scrappedUnits(i.getItemType().isSerialised()
                        ? counts.getOrDefault(AssetStatus.SCRAPPED, 0L).intValue() : null)
                .availableNewUnits(i.getItemType().isSerialised()
                        ? conditions.getOrDefault(AssetCondition.NEW, 0L).intValue() : null)
                .availableReconUnits(i.getItemType().isSerialised()
                        ? conditions.getOrDefault(AssetCondition.RECON, 0L).intValue() : null)
                .createdDate(i.getCreatedDate())
                .modifiedDate(i.getModifiedDate())
                .modifiedBy(i.getModifiedBy())
                .build();
    }

    private Map<Integer, Map<AssetStatus, Long>> loadUnitCounts(Integer companyRefId, List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Map<AssetStatus, Long>> out = new HashMap<>();
        for (Object[] row : assetRepository.countByProductAndStatus(companyRefId, productIds)) {
            Integer productRefId = (Integer) row[0];
            AssetStatus status = (AssetStatus) row[1];
            Long count = ((Number) row[2]).longValue();
            out.computeIfAbsent(productRefId, k -> new EnumMap<>(AssetStatus.class)).put(status, count);
        }
        return out;
    }

    private BigDecimal onHandOf(InventoryItem i, Map<Integer, Double> balances, Map<AssetStatus, Long> counts) {
        if (i.getItemType().isSerialised()) {
            return BigDecimal.valueOf(counts.getOrDefault(AssetStatus.AVAILABLE, 0L));
        }
        return BigDecimal.valueOf(balances.getOrDefault(i.getProductRefId(), 0.0));
    }

    private Map<Integer, Map<AssetCondition, Long>> loadConditionCounts(
            Integer companyRefId, List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Map<AssetCondition, Long>> out = new HashMap<>();
        for (Object[] row : assetRepository.countAvailableByProductAndCondition(companyRefId, productIds)) {
            Integer productRefId = (Integer) row[0];
            AssetCondition condition = (AssetCondition) row[1];
            Long count = ((Number) row[2]).longValue();
            out.computeIfAbsent(productRefId, k -> new EnumMap<>(AssetCondition.class))
                    .put(condition, count);
        }
        return out;
    }

    /**
     * Units the workshop still holds. Scrapped units are excluded - they have
     * been written off, so counting them would overstate what is on hand.
     */
    private Integer totalUnits(Map<AssetStatus, Long> counts) {
        return (int) counts.entrySet().stream()
                .filter(e -> e.getKey() != AssetStatus.SCRAPPED)
                .mapToLong(Map.Entry::getValue)
                .sum();
    }

    private BigDecimal valueOf(BigDecimal onHand, Double unitCost) {
        return onHand.multiply(BigDecimal.valueOf(unitCost == null ? 0.0 : unitCost))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String statusOf(InventoryItem i, BigDecimal onHand) {
        if (onHand.compareTo(BigDecimal.ZERO) <= 0) {
            return STATUS_OUT_OF_STOCK;
        }
        // "At or below" - reaching the reorder level is the point at which you
        // reorder, so it counts as low. This must stay <= to match the low-stock
        // query in InventoryItemRepository: with < , an item sitting exactly on
        // its level appeared in the needs-attention list while its own row still
        // showed a green In Stock chip.
        if (!i.getItemType().isSerialised() && i.getMinQty() != null
                && onHand.compareTo(BigDecimal.valueOf(i.getMinQty())) <= 0) {
            return STATUS_LOW_STOCK;
        }
        return STATUS_IN_STOCK;
    }

    // ------------------------------------------------------------------ helpers

    private ItemType parseType(String raw) {
        try {
            return ItemType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Item Type must be one of "
                    + Arrays.toString(ItemType.values()) + ", got: " + raw);
        }
    }

    /**
     * ProductMaster.Tax_Code is NOT NULL. Use the caller's value, otherwise the
     * company's first configured tax. Never invents master data silently - a company
     * with no tax set up is a real configuration problem and says so.
     */
    private Integer resolveTaxCode(InventoryItemRequestDto request) {
        if (request.getTaxCode() != null) {
            return request.getTaxCode();
        }
        Integer resolved = firstIdFor("TaxMaster", request.getCompanyRefId());
        if (resolved == null) {
            throw new InvalidRequestException(
                    "No tax code was supplied and this company has no tax records configured. "
                    + "Set up a tax record first, or pass taxCode in the request.");
        }
        return resolved;
    }

    /**
     * Resolve ProductMaster.UOM_Code (NOT NULL) for a newly created product.
     *
     * Order: the caller's explicit uomCode, then the UOM master row that matches
     * the unit chosen on the form, then the company's first UOM as a last resort.
     *
     * The match matters. Without it every new product took whichever UOM happened
     * to sort first - KG for company 6 - so an oil item created as litres was
     * filed against ProductMaster as kilograms, and any other module reading
     * UOM_Code saw the wrong unit.
     */
    private Integer resolveUomCode(InventoryItemRequestDto request, String baseUom) {
        if (request.getUomCode() != null) {
            return request.getUomCode();
        }

        Integer matched = matchUomToBaseUom(request.getCompanyRefId(), baseUom);
        if (matched != null) {
            return matched;
        }

        Integer resolved = firstIdFor("UOM", request.getCompanyRefId());
        if (resolved == null) {
            throw new InvalidRequestException(
                    "No UOM code was supplied and this company has no UOM records configured. "
                    + "Set up a UOM record first, or pass uomCode in the request.");
        }
        logger.warn("No UOM master row matches '{}' for company {} - falling back to UOM id {}."
                + " Add a UOM record for '{}' so new products carry the right unit.",
                baseUom, request.getCompanyRefId(), resolved, baseUom);
        return resolved;
    }

    /**
     * Find the active UOM master row for a stock-keeping unit. Matches on either
     * Description or Code, and accepts the spellings these tables use in
     * practice - LITRE and LTR for L, UNIT(S) for Unit, and so on.
     */
    private Integer matchUomToBaseUom(Integer companyRefId, String baseUom) {
        if (baseUom == null || baseUom.trim().isEmpty()) {
            return null;
        }
        List<String> aliases = uomAliases(baseUom.trim());
        String placeholders = String.join(",", Collections.nCopies(aliases.size(), "?"));

        List<Object> args = new ArrayList<>();
        args.add(companyRefId);
        args.addAll(aliases);
        args.addAll(aliases);

        List<Integer> ids = jdbcTemplate.queryForList(
                "SELECT TOP 1 Id FROM UOM WHERE CompanyRefId = ? AND Active = 1 AND ("
                        + " UPPER(LTRIM(RTRIM(Description))) IN (" + placeholders + ")"
                        + " OR UPPER(LTRIM(RTRIM(Code))) IN (" + placeholders + ")) ORDER BY Id",
                Integer.class, args.toArray());
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * ProductMaster.PCode_Digits holds the numeric value of a short all-digit
     * product code, and 0 for anything else - the same rule the legacy
     * SP_ProductMaster applied.
     */
    private Integer numericCodeDigits(String code) {
        if (code == null || code.isEmpty() || code.length() >= 8 || !code.matches("\\d+")) {
            return 0;
        }
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<String> uomAliases(String baseUom) {
        switch (baseUom.toUpperCase()) {
            case "L":
                return List.of("L", "LTR", "LITRE", "LITER", "LITRES", "LITERS");
            case "PCS":
                return List.of("PCS", "PC", "PCSS", "PIECE", "PIECES");
            case "UNIT":
                return List.of("UNIT", "UNITS", "UNIT(S)");
            default:
                return List.of(baseUom.toUpperCase());
        }
    }

    /**
     * Every way a UOM row can be referred to, mapped to its Description.
     *
     * Keys are the row's Id, its Code and its Description, all upper-cased. That
     * lets a stored baseUom be healed on the way out whatever was put in it -
     * screens have written the Id ("14") and the Code ("09") at different points,
     * and both render as nonsense next to a quantity ("1100 09").
     */
    private Map<String, String> uomLookup(Integer companyRefId) {
        Map<String, String> lookup = new HashMap<>();
        jdbcTemplate.query(
                "SELECT Id, Code, Description FROM UOM WHERE CompanyRefId = ?",
                rs -> {
                    String description = rs.getString("Description");
                    if (description == null || description.trim().isEmpty()) {
                        return;
                    }
                    String value = description.trim();
                    lookup.put(String.valueOf(rs.getInt("Id")), value);
                    String code = rs.getString("Code");
                    if (code != null && !code.trim().isEmpty()) {
                        lookup.putIfAbsent(code.trim().toUpperCase(), value);
                    }
                    lookup.putIfAbsent(value.toUpperCase(), value);
                },
                companyRefId);
        return lookup;
    }

    /** Display label for a stored baseUom, left untouched when nothing matches. */
    private String displayUom(Map<String, String> lookup, String baseUom) {
        if (baseUom == null || baseUom.trim().isEmpty()) {
            return baseUom;
        }
        return lookup.getOrDefault(baseUom.trim().toUpperCase(), baseUom.trim());
    }

    /**
     * Readable name for a UOM or TaxMaster row. Both tables carry Description,
     * so one lookup serves each. Returns null rather than throwing when the row
     * is missing - a detail screen should still render if master data is untidy.
     */
    private String lookupName(String table, Integer id) {
        if (id == null) {
            return null;
        }
        List<String> names = jdbcTemplate.queryForList(
                "SELECT TOP 1 Description FROM " + table + " WHERE Id = ?", String.class, id);
        return names.isEmpty() ? null : names.get(0);
    }

    private Integer firstIdFor(String table, Integer companyRefId) {
        List<Integer> ids = jdbcTemplate.queryForList(
                "SELECT TOP 1 Id FROM " + table + " WHERE CompanyRefId = ? ORDER BY Id",
                Integer.class, companyRefId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String defaultUom(String supplied, ItemType type) {
        if (supplied != null && !supplied.trim().isEmpty()) {
            return supplied.trim();
        }
        switch (type) {
            case CONSUMABLE: return "L";
            case PART: return "Pcs";
            default: return "Unit";
        }
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Double nvl(Double d) {
        return d == null ? 0.0 : d;
    }
}
