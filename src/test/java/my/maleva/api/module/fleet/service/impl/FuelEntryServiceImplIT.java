package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.fleet.entity.FuelEntry;
import my.maleva.api.module.fleet.repository.FuelEntryRepository;
import my.maleva.api.module.fleet.service.FuelEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The fuel entry delete, exercised against MalevanewDemo.
 *
 * <p>The row each test works on is inserted here and everything runs inside the
 * test's own transaction, so nothing is left behind.
 *
 * <p>These cases exist because the delete used to fire a bulk UPDATE and test
 * its affected-row count against 0. The pool sets {@code SET NOCOUNT ON}, so
 * SQL Server sends no row count and JDBC reports -1 for every UPDATE on this
 * datasource: the count was never 0, and DELETE /api/fuel-entries/{id} answered
 * 200 for an id belonging to another company while changing nothing.
 */
@SpringBootTest
@Transactional
class FuelEntryServiceImplIT {

    private static final int COMPANY = 6;

    /** FStatus values: 0 is a row raised at a desk, 1 one raised by the driver app. */
    private static final int DESK = 0;
    private static final int DRIVER_APP = 1;

    @Autowired private FuelEntryService service;
    @Autowired private FuelEntryRepository repository;

    @Test
    void deletingAnotherCompanysEntryIsNotFoundAndLeavesItAlone() {
        FuelEntry entry = repository.saveAndFlush(newEntry(DESK));

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(entry.getId(), COMPANY + 1, false, "tester"));

        assertEquals(1, repository.findById(entry.getId()).orElseThrow().getActive(),
                "a delete aimed at the wrong company must not touch the row");
    }

    @Test
    void deletingAnIdThatDoesNotExistIsNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> service.delete(0, COMPANY, false, "tester"));
    }

    /** mobile=true is the legacy guard: only a driver-app row (FStatus 1) may go that way. */
    @Test
    void deletingADeskRowThroughTheDriverAppPathIsNotFound() {
        FuelEntry entry = repository.saveAndFlush(newEntry(DESK));

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(entry.getId(), COMPANY, true, "tester"));

        assertEquals(1, repository.findById(entry.getId()).orElseThrow().getActive());
    }

    @Test
    void deletingADriverAppRowThroughTheDriverAppPathSucceeds() {
        FuelEntry entry = repository.saveAndFlush(newEntry(DRIVER_APP));

        service.delete(entry.getId(), COMPANY, true, "tester");

        assertEquals(2, repository.findById(entry.getId()).orElseThrow().getActive());
    }

    @Test
    void deleteHidesTheEntryFromTheEditForm() {
        FuelEntry entry = repository.saveAndFlush(newEntry(DESK));

        service.delete(entry.getId(), COMPANY, false, "tester");

        FuelEntry stored = repository.findById(entry.getId()).orElseThrow();
        assertEquals(2, stored.getActive());
        assertEquals("tester", stored.getModifiedBy());
        assertThrows(EntityNotFoundException.class,
                () -> service.getForEdit(entry.getId(), null, COMPANY));
    }

    /** The delete must not be reported as done when it removed nothing. */
    @Test
    void aSecondDeleteOfTheSameEntryIsNotFound() {
        FuelEntry entry = repository.saveAndFlush(newEntry(DESK));
        service.delete(entry.getId(), COMPANY, false, "tester");

        assertTrue(assertThrows(EntityNotFoundException.class,
                () -> service.delete(entry.getId(), COMPANY, false, "tester"))
                .getMessage().contains(String.valueOf(entry.getId())));
    }

    // ------------------------------------------------------------ helpers

    /**
     * A blank entry for this company. Every NOT NULL column has to be filled -
     * FStatus included, which the table rejects as null even though the entity
     * does not say so.
     */
    private FuelEntry newEntry(int fStatus) {
        LocalDateTime now = LocalDateTime.now();
        return FuelEntry.builder()
                .companyRefId(COMPANY)
                .saleDate(now)
                .cNumber(999_999_001)
                .cNumberDisplay("FE999999001")
                .active(1)
                .fStatus(fStatus)
                .aliter(0f).aAmount(0f)
                .pliter(0f).pRate(0f).pAmount(0f)
                .gliter(0f).gAmount(0f)
                .dPliter(0f).dpAmount(0f)
                .dGliter(0f).dgAmount(0f)
                .remarks("integration test")
                .createdDate(now).createdBy("tester")
                .modifiedDate(now).modifiedBy("tester")
                .build();
    }
}
