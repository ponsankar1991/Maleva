package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderQuickUpdateDto;
import my.maleva.api.module.saleorder.mapper.SaleOrderMasterMapper;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * An update whose URL names one sale order and whose body names another must be refused
 * before anything is written.
 *
 * This is the shape of the 2026-09-10 incident: the Planning screen's update modal takes
 * the URL id from the selected row and the body from whatever it last loaded, so a load
 * that never completed left the two pointing at different orders. The service used to log
 * the mismatch, keep the path id, and then copy the body's master row and child rows onto
 * it - sale order 21507 ended up holding 21500's job number, customer and line items.
 */
@ExtendWith(MockitoExtension.class)
class SaleOrderUpdateIdMismatchTest {

    private static final Integer PATH_ID = 21507;
    private static final Integer OTHER_ID = 21500;

    @Mock
    private SaleOrderMasterRepository repository;

    @Mock
    private SaleOrderMasterMapper mapper;

    @InjectMocks
    private SaleOrderMasterServiceImpl service;

    @Test
    @DisplayName("update refuses a body that names a different sale order, and writes nothing")
    void updateRejectsMismatchedBodyId() {
        SaleOrderDTO dto = new SaleOrderDTO();
        dto.setId(OTHER_ID);
        dto.setCompanyRefId(6);
        dto.setCNumberDisplay("MY002605171");

        assertThatThrownBy(() -> service.update(PATH_ID, dto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("21507")
                .hasMessageContaining("21500");

        // The row is never even loaded, so no part of the aggregate can be touched.
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);

        // The body is left as the caller sent it - the old code overwrote it with the path id.
        assertThat(dto.getId()).isEqualTo(OTHER_ID);
    }

    @Test
    @DisplayName("updateMaster refuses a mismatched body id")
    void updateMasterRejectsMismatchedBodyId() {
        SaleOrderMasterDto dto = new SaleOrderMasterDto();
        dto.setId(OTHER_ID);
        dto.setCompanyRefId(6);

        assertThatThrownBy(() -> service.updateMaster(PATH_ID, dto))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("quick update refuses a mismatched body id")
    void quickUpdateRejectsMismatchedBodyId() {
        SaleOrderQuickUpdateDto dto = new SaleOrderQuickUpdateDto();
        dto.setId(OTHER_ID);
        dto.setCompanyRefId(6);

        assertThatThrownBy(() -> service.updateQuickFields(PATH_ID, dto))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("a body that omits the id is allowed - the URL is then the only identifier")
    void absentBodyIdIsNotAMismatch() {
        SaleOrderDTO withoutId = new SaleOrderDTO();
        withoutId.setCompanyRefId(6);

        SaleOrderDTO withZeroId = new SaleOrderDTO();
        withZeroId.setId(0);
        withZeroId.setCompanyRefId(6);

        // Neither contradicts the path, so neither is refused for a mismatch. Both still
        // fail further in, inside the real save these mocks do not stand up - the point is
        // only that they get past the guard, so assert on the message, not the type.
        assertThatThrownBy(() -> service.update(PATH_ID, withoutId))
                .hasMessageNotContaining("ID mismatch");
        assertThatThrownBy(() -> service.update(PATH_ID, withZeroId))
                .hasMessageNotContaining("ID mismatch");
    }
}
