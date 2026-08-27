package my.maleva.api.module.itemmaster.service;

import my.maleva.api.integration.qne.dto.QneStockRequest;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy ItemMasterServices.InsertItemMaster field mapping — the
 * four stock flags hardcoded true, and MinPrice equal to ListPrice.
 */
class ItemMasterQnePayloadTest {

    @Test
    void mapsLegacyInsertItemMasterFields() {
        ItemMaster item = new ItemMaster();
        item.setId(11);
        item.setProdCode(" FRT-001 ");
        item.setPName("FREIGHT CHARGES");
        item.setSalesRate(150.5f);
        item.setPurchaseRate(120.25f);

        QneStockRequest request = ItemMasterQneService.buildRequest(item, "TRIP");

        assertThat(request.getStockCode()).isEqualTo("FRT-001");
        assertThat(request.getStockName()).isEqualTo("FREIGHT CHARGES");
        assertThat(request.getBaseUom()).isEqualTo("TRIP");
        assertThat(request.isBundled()).isTrue();
        assertThat(request.isStockControl()).isTrue();
        assertThat(request.isUseSerialNo()).isTrue();
        assertThat(request.isUseBatchNo()).isTrue();
        assertThat(request.getListPrice()).isEqualTo(150.5, org.assertj.core.data.Offset.offset(0.001));
        assertThat(request.getMinPrice()).isEqualTo(request.getListPrice());
        assertThat(request.getPurchasePrice()).isEqualTo(120.25, org.assertj.core.data.Offset.offset(0.001));
    }
}
