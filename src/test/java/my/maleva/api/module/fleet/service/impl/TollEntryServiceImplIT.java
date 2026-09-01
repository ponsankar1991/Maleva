package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.fleet.entity.TollEntry;
import my.maleva.api.module.fleet.repository.TollEntryRepository;
import my.maleva.api.module.fleet.service.TollEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The toll entry delete, exercised against MalevanewDemo.
 *
 * <p>The row each test works on is inserted here and everything runs inside the
 * test's own transaction, so nothing is left behind.
 *
 * <p>These cases exist because the delete used to fire a bulk UPDATE and test
 * its affected-row count against 0. The pool sets {@code SET NOCOUNT ON}, so
 * SQL Server sends no row count and JDBC reports -1 for every UPDATE on this
 * datasource: the count was never 0, and DELETE /api/toll-entries/{id} answered
 * 200 for an id belonging to another company while changing nothing.
 */
@SpringBootTest
@Transactional
class TollEntryServiceImplIT {

    private static final int COMPANY = 6;

    @Autowired private TollEntryService service;
    @Autowired private TollEntryRepository repository;

    @Test
    void deletingAnotherCompanysEntryIsNotFoundAndLeavesItAlone() {
        TollEntry entry = repository.saveAndFlush(newEntry());

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(entry.getId(), COMPANY + 1, "tester"));

        assertEquals(1, repository.findById(entry.getId()).orElseThrow().getActive(),
                "a delete aimed at the wrong company must not touch the row");
    }

    @Test
    void deletingAnIdThatDoesNotExistIsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> service.delete(0, COMPANY, "tester"));
    }

    @Test
    void deleteHidesTheEntryFromTheEditForm() {
        TollEntry entry = repository.saveAndFlush(newEntry());

        service.delete(entry.getId(), COMPANY, "tester");

        TollEntry stored = repository.findById(entry.getId()).orElseThrow();
        assertEquals(2, stored.getActive());
        assertEquals("tester", stored.getModifiedBy());
        assertThrows(EntityNotFoundException.class,
                () -> service.getForEdit(entry.getId(), null, COMPANY));
    }

    @Test
    void aSecondDeleteOfTheSameEntryIsNotFound() {
        TollEntry entry = repository.saveAndFlush(newEntry());
        service.delete(entry.getId(), COMPANY, "tester");

        assertTrue(assertThrows(EntityNotFoundException.class,
                () -> service.delete(entry.getId(), COMPANY, "tester"))
                .getMessage().contains(String.valueOf(entry.getId())));
    }

    // ------------------------------------------------------------ helpers

    private TollEntry newEntry() {
        LocalDateTime now = LocalDateTime.now();
        return TollEntry.builder()
                .companyRefId(COMPANY)
                .saleDate(now)
                .cNumber(999_999_001)
                .cNumberDisplay("TE999999001")
                .active(1)
                .amount(0f)
                .remarks("integration test")
                .createdDate(now).createdBy("tester")
                .modifiedDate(now).modifiedBy("tester")
                .build();
    }
}
