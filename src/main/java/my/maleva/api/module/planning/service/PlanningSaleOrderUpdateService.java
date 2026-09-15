package my.maleva.api.module.planning.service;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.planning.dto.PlanningSaleOrderUpdateResponse;
import my.maleva.api.module.planning.dto.request.PlanningSaleOrderUpdateRequest;
import my.maleva.api.module.saleorder.entity.SaleOrderDelivery;
import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import my.maleva.api.module.saleorder.repository.SaleOrderDeliveryRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderPickupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Planning screen's Update window.
 *
 * Writes the fields that window shows - pickup/delivery date, origin/destination, quantity and
 * total weight, warehouse enter/exit date and address - and reconciles the job's pickup and
 * delivery stop rows. Nothing else on the sale order is touched. It replaced a full PUT of the
 * sale order built from a form that never loaded the totals, which set GrossAmount, TaxAmount,
 * Amount and CurrencyValue to 0 and deleted and re-inserted every line item and stop.
 */
@Service
public class PlanningSaleOrderUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(PlanningSaleOrderUpdateService.class);

    private static final int DELETED_STATUS = 2;

    /** Separator of the joined stop columns (PickupAddress, pickupQuantitylist, ...). */
    private static final String LIST_SEPARATOR = "{@}";

    private static final DateTimeFormatter GRID_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final List<DateTimeFormatter> ACCEPTED_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

    private static final StopFields<SaleOrderPickup> PICKUP = new StopFields<>(
            "Pickup",
            SaleOrderPickup::getId,
            SaleOrderPickup::getPickupAddress,
            SaleOrderPickup::getPickupQuantity,
            (saleOrderId, now) -> SaleOrderPickup.builder().saleOrderMasterRefId(saleOrderId).createdDate(now).build(),
            (stop, values) -> {
                stop.setPickupAddress(values.address());
                stop.setPickupTime(values.time());
                stop.setPickupWeight(values.weight());
                stop.setPickupQuantity(values.quantity());
            });

    private static final StopFields<SaleOrderDelivery> DELIVERY = new StopFields<>(
            "Delivery",
            SaleOrderDelivery::getId,
            SaleOrderDelivery::getDeliveryAddress,
            SaleOrderDelivery::getDeliveryQuantity,
            (saleOrderId, now) -> SaleOrderDelivery.builder().saleOrderMasterRefId(saleOrderId).createdDate(now).build(),
            (stop, values) -> {
                stop.setDeliveryAddress(values.address());
                stop.setDeliveryTime(values.time());
                stop.setDeliveryWeight(values.weight());
                stop.setDeliveryQuantity(values.quantity());
            });

    private final SaleOrderMasterRepository saleOrderRepository;
    private final SaleOrderPickupRepository pickupRepository;
    private final SaleOrderDeliveryRepository deliveryRepository;

    public PlanningSaleOrderUpdateService(SaleOrderMasterRepository saleOrderRepository,
                                          SaleOrderPickupRepository pickupRepository,
                                          SaleOrderDeliveryRepository deliveryRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.pickupRepository = pickupRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public PlanningSaleOrderUpdateResponse update(PlanningSaleOrderUpdateRequest request) {
        Integer saleOrderId = request.getSaleOrderId();
        Integer companyId = request.getCompanyId();

        saleOrderRepository.findByIdAndCompanyRefId(saleOrderId, companyId)
                .filter(order -> !Objects.equals(order.getActive(), DELETED_STATUS))
                .orElseThrow(() -> new EntityNotFoundException("Sale order not found: " + saleOrderId));

        // Everything is parsed and matched before the first write, so a bad value or a stale
        // stop leaves the job exactly as it was.
        LocalDateTime pickupDate = parseDateTime(request.getPickupDate(), "Pickup Date");
        LocalDateTime deliveryDate = parseDateTime(request.getDeliveryDate(), "Delivery Date");
        LocalDateTime enterDate = parseDateTime(request.getWareHouseEnterDate(), "Warehouse Enter Date");
        LocalDateTime exitDate = parseDateTime(request.getWareHouseExitDate(), "Warehouse Exit Date");

        LocalDateTime now = LocalDateTime.now();
        StopPlan<SaleOrderPickup> pickupPlan = planStops(request.getPickups(), request.getRemovedPickupIds(),
                pickupRepository.findBySaleOrderMasterRefId(saleOrderId), PICKUP, saleOrderId, now);
        StopPlan<SaleOrderDelivery> deliveryPlan = planStops(request.getDeliveries(), request.getRemovedDeliveryIds(),
                deliveryRepository.findBySaleOrderMasterRefId(saleOrderId), DELIVERY, saleOrderId, now);

        String origin = trimToEmpty(request.getOrigin());
        String destination = trimToEmpty(request.getDestination());
        String quantity = trimToEmpty(request.getQuantity());
        String totalWeight = trimToEmpty(request.getTotalWeight());
        String wareHouseAddress = request.getWareHouseAddress() == null ? "" : request.getWareHouseAddress();
        String pickupAddress = joinList(pickupPlan.addresses());
        String deliveryAddress = joinList(deliveryPlan.addresses());
        String pickupQuantities = joinList(pickupPlan.quantities());
        String deliveryQuantities = joinList(deliveryPlan.quantities());
        // Same rule as the Sale Order screen's save: the stop quantities, else the job quantity.
        String quantityList = pickupQuantities.isEmpty() ? quantity : pickupQuantities;

        saleOrderRepository.updatePlanningFields(saleOrderId, companyId,
                pickupDate, deliveryDate, enterDate, exitDate, wareHouseAddress,
                emptyToNull(origin), emptyToNull(destination),
                positiveOrNull(request.getOriginRefId()), positiveOrNull(request.getDestinationRefId()),
                quantity, totalWeight,
                emptyToNull(pickupAddress), emptyToNull(deliveryAddress),
                pickupQuantities, deliveryQuantities, quantityList,
                positiveOrNull(request.getEmployeeId()), now);

        applyStops(pickupPlan, PICKUP, pickupRepository);
        applyStops(deliveryPlan, DELIVERY, deliveryRepository);

        logger.info("Planning update saved - saleOrderId: {}, company: {}, pickups saved/removed: {}/{}, deliveries saved/removed: {}/{}",
                saleOrderId, companyId, pickupPlan.writes().size(), pickupPlan.deletes().size(),
                deliveryPlan.writes().size(), deliveryPlan.deletes().size());

        return PlanningSaleOrderUpdateResponse.builder()
                .ok(true)
                .message("Sale order updated successfully")
                .saleOrderId(saleOrderId)
                .pickupDate(formatForGrid(pickupDate))
                .deliveryDate(formatForGrid(deliveryDate))
                .origin(origin)
                .destination(destination)
                .quantity(quantity)
                .totalWeight(totalWeight)
                .packageType(quantity + "/" + totalWeight)
                .wareHouseEnterDate(formatForGrid(enterDate))
                .wareHouseExitDate(formatForGrid(exitDate))
                .wareHouseAddress(wareHouseAddress)
                .pickupAddress(pickupAddress)
                .deliveryAddress(deliveryAddress)
                .pickupQuantityList(pickupQuantities)
                .deliveryQuantityList(deliveryQuantities)
                .pickupCount(pickupPlan.addresses().size())
                .deliveryCount(deliveryPlan.addresses().size())
                .build();
    }

    /**
     * Works out what the form does to one kind of stop, without writing anything.
     *
     * A stop with an id must belong to THIS job - a form left open while the Sale Order screen
     * re-created the stops would otherwise write onto rows that are gone or belong elsewhere.
     * Only ids the user removed are deleted; a stop that is neither on the form nor removed
     * (added by someone else meanwhile) stays, after the form's stops in the joined lists.
     */
    private <T> StopPlan<T> planStops(List<PlanningSaleOrderUpdateRequest.Stop> requested,
                                      List<Integer> removedIds,
                                      List<T> existing,
                                      StopFields<T> fields,
                                      Integer saleOrderId,
                                      LocalDateTime now) {
        Map<Integer, T> existingById = new LinkedHashMap<>();
        existing.forEach(stop -> existingById.put(fields.id().apply(stop), stop));

        if (requested == null) {
            return new StopPlan<>(List.of(), List.of(),
                    existing.stream().map(fields.address()).toList(),
                    existing.stream().map(fields.quantity()).toList());
        }

        Set<Integer> removed = new LinkedHashSet<>();
        if (removedIds != null) {
            removedIds.stream().filter(Objects::nonNull).forEach(removed::add);
        }

        List<StopWrite<T>> writes = new ArrayList<>();
        Set<Integer> kept = new HashSet<>();
        int position = 0;
        for (PlanningSaleOrderUpdateRequest.Stop item : requested) {
            position++;
            String label = fields.label() + " stop " + position;

            T stop;
            if (item.getId() != null && item.getId() > 0) {
                stop = existingById.get(item.getId());
                if (stop == null) {
                    throw new InvalidRequestException(fields.label() + " stop " + item.getId()
                            + " is no longer on this job. Close and reopen Update before saving.");
                }
                if (removed.contains(item.getId())) {
                    throw new InvalidRequestException(fields.label() + " stop " + item.getId()
                            + " cannot be both kept and removed.");
                }
                if (!kept.add(item.getId())) {
                    throw new InvalidRequestException(fields.label() + " stop " + item.getId() + " is sent twice.");
                }
            } else {
                stop = fields.create().apply(saleOrderId, now);
            }

            String address = trimToEmpty(item.getAddress());
            if (address.isEmpty()) {
                throw new InvalidRequestException(label + " needs an address.");
            }

            writes.add(new StopWrite<>(stop, new StopValues(
                    address,
                    parseDateTime(item.getTime(), label + " time"),
                    trimToEmpty(item.getWeight()),
                    trimToEmpty(item.getQuantity()))));
        }

        List<T> deletes = removed.stream().map(existingById::get).filter(Objects::nonNull).toList();

        List<String> addresses = new ArrayList<>();
        List<String> quantities = new ArrayList<>();
        writes.forEach(write -> {
            addresses.add(write.values().address());
            quantities.add(write.values().quantity());
        });
        existingById.forEach((id, stop) -> {
            if (!kept.contains(id) && !removed.contains(id)) {
                addresses.add(fields.address().apply(stop));
                quantities.add(fields.quantity().apply(stop));
            }
        });

        return new StopPlan<>(writes, deletes, addresses, quantities);
    }

    private static <T> void applyStops(StopPlan<T> plan, StopFields<T> fields, JpaRepository<T, Integer> repository) {
        if (!plan.deletes().isEmpty()) {
            repository.deleteAll(plan.deletes());
        }
        if (!plan.writes().isEmpty()) {
            plan.writes().forEach(write -> fields.write().accept(write.stop(), write.values()));
            repository.saveAll(plan.writes().stream().map(StopWrite::stop).toList());
        }
    }

    private static LocalDateTime parseDateTime(String value, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        for (DateTimeFormatter format : ACCEPTED_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted format.
            }
        }
        throw new InvalidRequestException(label + " is not a valid date/time: " + trimmed);
    }

    private static String joinList(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(LIST_SEPARATOR));
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static Integer positiveOrNull(Integer value) {
        return value != null && value > 0 ? value : null;
    }

    private static String formatForGrid(LocalDateTime value) {
        return value == null ? "" : value.format(GRID_FORMAT);
    }

    private record StopFields<T>(String label,
                                 Function<T, Integer> id,
                                 Function<T, String> address,
                                 Function<T, String> quantity,
                                 BiFunction<Integer, LocalDateTime, T> create,
                                 BiConsumer<T, StopValues> write) {
    }

    private record StopValues(String address, LocalDateTime time, String weight, String quantity) {
    }

    private record StopWrite<T>(T stop, StopValues values) {
    }

    private record StopPlan<T>(List<StopWrite<T>> writes, List<T> deletes,
                               List<String> addresses, List<String> quantities) {
    }
}
