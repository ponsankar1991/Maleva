package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.mapper.SaleOrderMasterMapper;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.service.SaleOrderCreateGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * A new sale order saved, the page gave up waiting, the operator pressed Save
 * again: the second request must hand back the first order, not create another.
 */
@ExtendWith(MockitoExtension.class)
class SaleOrderCreateReplayTest {

    private static final String KEY = "so-create-7f3a";

    @Mock
    private SaleOrderMasterRepository repository;

    @Mock
    private SaleOrderMasterMapper mapper;

    @Spy
    private SaleOrderCreateGuard createGuard = new SaleOrderCreateGuard();

    @InjectMocks
    private SaleOrderMasterServiceImpl service;

    private static SaleOrderDTO newOrder(String key) {
        SaleOrderDTO dto = new SaleOrderDTO();
        dto.setId(0);
        dto.setCompanyRefId(6);
        dto.setCustomerRefId(49);
        dto.setClientRequestId(key);
        return dto;
    }

    @Test
    void aRepeatedCreateReturnsTheOrderAlreadyMadeAndWritesNothing() {
        SaleOrderMasterDto first = new SaleOrderMasterDto();
        first.setId(21600);
        first.setCNumberDisplay("MY002605172");
        createGuard.begin(KEY);
        createGuard.complete(KEY, first);

        assertThat(service.save(newOrder(KEY))).isSameAs(first);

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void aRepeatWhileTheFirstCreateIsStillRunningIsRefused() {
        createGuard.begin(KEY);

        assertThatThrownBy(() -> service.save(newOrder(KEY)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("still being saved");

        verifyNoInteractions(repository);
    }

    @Test
    void aFailedCreateReleasesTheKeySoARetryReallyCreates() {
        // The mocks do not stand a real save up, so this create fails part-way.
        assertThatThrownBy(() -> service.save(newOrder(KEY)));

        // The key was released rather than left "running" for ever.
        assertThat(createGuard.begin(KEY)).isEqualTo(Optional.empty());
    }
}
