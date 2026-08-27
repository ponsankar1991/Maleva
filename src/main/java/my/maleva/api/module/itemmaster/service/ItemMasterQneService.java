package my.maleva.api.module.itemmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.QneAfterCommit;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneStockRequest;
import my.maleva.api.integration.qne.dto.QneStockResponse;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QNE sync for stock items — the Java port of the QNE side of legacy
 * {@code ItemMasterServices} (InsertItemMaster's push and UpdateItemmasterId's
 * reconcile-then-push).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemMasterQneService {

    private final QneGateway gateway;
    private final ItemMasterRepository items;
    private final UomRepository uoms;

    /** Pushes a newly created item once its insert commits (see CustomerQneService). */
    public void pushCreatedAfterCommit(ItemMaster saved) {
        QneAfterCommit.run(() -> {
            QnePushResult result = pushCreated(saved);
            if (!result.success()) {
                log.warn("QNE push for new item {} did not complete: {}",
                        saved.getId(), result.message());
            }
        });
    }

    public QnePushResult pushCreated(ItemMaster item) {
        if (!QnePayloads.isBlank(item.getQneId())) {
            return QnePushResult.alreadyPushed(item.getQneId(), item.getQneCode(),
                    "Item already exists in QNE as " + item.getQneCode());
        }

        QneCall<QneStockResponse> call = gateway.createStock(buildRequest(item, uomDescription(item)));
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        items.claimQneIdentity(item.getId(), call.data().getId(), call.data().getStockCode());
        return QnePushResult.ok(call.data().getId(), call.data().getStockCode(), null,
                "Stock pushed to QNE as " + call.data().getStockCode());
    }

    /**
     * Legacy {@code UpdateItemmasterId}: look up every never-synced local item
     * in QNE by stock code; matched items get their ids written back, absent
     * ones are pushed one by one — an individual push failure is logged and
     * the loop continues (the legacy fail-fast abort was deliberately
     * commented out).
     */
    public QnePushResult reconcile(Integer companyRefId) {
        List<ItemMaster> pending = items.findQneReconcileCandidates(companyRefId);
        if (pending.isEmpty()) {
            return QnePushResult.ok(null, null, null, "No items waiting for a QNE id");
        }

        Map<String, QneStockResponse> inQne = new HashMap<>();
        List<String> codes = pending.stream()
                .map(i -> QnePayloads.orEmpty(i.getProdCode()).trim())
                .distinct()
                .toList();
        for (List<String> chunk : QnePayloads.chunks(codes, 100)) {
            QneCall<List<QneStockResponse>> call = gateway.findStocksByStockCodes(chunk);
            if (!call.success()) {
                return QnePushResult.rejected(call.message());
            }
            for (QneStockResponse stock : call.data()) {
                if (!QnePayloads.isBlank(stock.getStockCode())) {
                    inQne.put(stock.getStockCode().trim(), stock);
                }
            }
        }

        int matched = 0;
        int pushed = 0;
        int failed = 0;
        for (ItemMaster item : pending) {
            QneStockResponse existing = inQne.get(QnePayloads.orEmpty(item.getProdCode()).trim());
            if (existing != null) {
                matched += items.reconcileQneIdentity(companyRefId,
                        existing.getStockCode().trim(), existing.getId(), existing.getStockCode());
                continue;
            }
            QneCall<QneStockResponse> call = gateway.createStock(buildRequest(item, uomDescription(item)));
            if (call.success()) {
                items.claimQneIdentity(item.getId(), call.data().getId(), call.data().getStockCode());
                pushed++;
            } else {
                failed++;
                log.warn("QNE reconcile could not push item {} ({}): {}",
                        item.getId(), item.getProdCode(), call.message());
            }
        }
        return QnePushResult.ok(null, null, null,
                "Reconciled " + matched + " items from QNE, pushed " + pushed + " new, "
                        + failed + " failed of " + pending.size() + " candidates");
    }

    private String uomDescription(ItemMaster item) {
        if (item.getUomCode() == null) {
            return "";
        }
        return uoms.findById(item.getUomCode())
                .map(Uom::getDescription)
                .orElse("");
    }

    /**
     * Field mapping pinned by legacy {@code ItemMasterServices.InsertItemMaster}:
     * the four stock flags are hardcoded true, and ListPrice and MinPrice both
     * carry the sales rate — QNE never gets a discount floor.
     */
    static QneStockRequest buildRequest(ItemMaster item, String uomDescription) {
        return QneStockRequest.builder()
                .stockCode(QnePayloads.orEmpty(item.getProdCode()).trim())
                .stockName(item.getPName())
                .baseUom(uomDescription)
                .isBundled(true)
                .stockControl(true)
                .useSerialNo(true)
                .useBatchNo(true)
                .listPrice(QnePayloads.d(item.getSalesRate()))
                .minPrice(QnePayloads.d(item.getSalesRate()))
                .purchasePrice(QnePayloads.d(item.getPurchaseRate()))
                .build();
    }
}
