package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.DriverMasterDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.mapper.DriverMasterMapper;
import my.maleva.api.module.fleet.mapper.DriverMasterMapperImpl;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.leave.mapper.LeaveRequestMapper;
import my.maleva.api.module.leave.repository.LeaveRequestRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Saving a driver from the Add / Edit Driver screen (POST /process).
 *
 * <p>The same rules as TruckMasterSaveTest, on the same code shape: the screen
 * posts Id 0 for a new driver, and a non-null 0 made Spring Data read it as an
 * existing row and issue an UPDATE against Id 0, which matches nothing - "Row
 * was already updated or deleted by another transaction ... with id '0'".
 *
 * <p>The real mapper is used rather than a mock: the numbering the insert
 * depends on travels through it, and it is where the fields were being lost.
 */
@ExtendWith(MockitoExtension.class)
class DriverMasterSaveTest {

    private static final Integer COMPANY = 6;

    @Mock private DriverMasterRepository repository;
    @Mock private EntityManager entityManager;
    @Mock private AccountsGroupMasterRepository accountsGroupMasterRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveRequestMapper leaveRequestMapper;
    @Spy private DriverMasterMapper mapper = new DriverMasterMapperImpl();

    @InjectMocks private DriverMasterServiceImpl service;

    /** What the Add Driver form posts: no id yet, and numbering left to the server. */
    private DriverMasterDto newDriverForm() {
        return DriverMasterDto.builder()
                .id(0)
                .companyRefId(COMPANY)
                .driverName("RAJU")
                .cNumber(0)
                .cNumberDisplay("AUTO")
                .accountRefid(1)
                .active(1)
                .modifiedBy("KARTHICK")
                .build();
    }

    /** The row as it was handed to save(), and the id it carried at that moment. */
    private DriverMaster savedRow;
    private Integer idAtSave;

    /** Stands in for the IDENTITY column: records the row, then stamps the new id on it. */
    private void answerSaveWithId(int newId) {
        when(repository.save(any(DriverMaster.class))).thenAnswer(call -> {
            DriverMaster row = call.getArgument(0);
            savedRow = row;
            idAtSave = row.getId();
            row.setId(newId);
            return row;
        });
    }

    @Test
    void aNewDriverIsInsertedRatherThanUpdatedAgainstIdZero() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(122);
        answerSaveWithId(42);

        DriverMasterDto saved = service.processDriver(newDriverForm(), COMPANY);

        // Null id at save time is the whole fix: it is what makes this an INSERT.
        assertThat(idAtSave).isNull();
        assertThat(saved.getId()).isEqualTo(42);
        verify(repository, never()).findById(any());
    }

    @Test
    void aNewDriverIsGivenTheNextCNumberAndItsDisplay() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(122);
        answerSaveWithId(42);

        DriverMasterDto saved = service.processDriver(newDriverForm(), COMPANY);

        assertThat(savedRow.getCNumber()).isEqualTo(123);
        assertThat(savedRow.getCNumberDisplay()).isEqualTo("D000000123");
        // And it comes back to the screen, which is what the header shows.
        assertThat(saved.getCNumberDisplay()).isEqualTo("D000000123");
    }

    /** The first driver of a company: nothing to count from. */
    @Test
    void theFirstDriverOfACompanyIsNumberOne() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(0);
        answerSaveWithId(1);

        service.processDriver(newDriverForm(), COMPANY);

        assertThat(savedRow.getCNumber()).isEqualTo(1);
        assertThat(savedRow.getCNumberDisplay()).isEqualTo("D000000001");
    }

    @Test
    void aNewDriverIsActiveAndStampedByDefault() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(122);
        answerSaveWithId(42);

        DriverMasterDto form = newDriverForm();
        form.setActive(null);
        form.setAccountRefid(null);
        form.setModifiedBy(null);
        service.processDriver(form, COMPANY);

        assertThat(savedRow.getActive()).isEqualTo(1);
        assertThat(savedRow.getAccountRefid()).isEqualTo(1);
        assertThat(savedRow.getModifiedBy()).isEqualTo("SYSTEM");
        assertThat(savedRow.getCreatedDate()).isNotNull();
        assertThat(savedRow.getModifiedDate()).isNotNull();
    }

    /**
     * An edit must keep the driver's number. The form posts 0 / "AUTO" whenever
     * it did not load the stored numbering, and that used to move the driver onto
     * a brand new number on every save.
     */
    @Test
    void anEditKeepsTheStoredNumbering() {
        DriverMaster stored = DriverMaster.builder()
                .id(9).companyRefId(COMPANY).driverName("RAJU")
                .cNumber(3).cNumberDisplay("D000000003")
                .truckRefId(11).active(1)
                .build();
        when(repository.findById(9)).thenReturn(Optional.of(stored));
        when(repository.save(any(DriverMaster.class))).thenAnswer(call -> call.getArgument(0));

        DriverMasterDto edit = newDriverForm();
        edit.setId(9);
        edit.setDriverName("RAJU KUMAR");
        service.processDriver(edit, COMPANY);

        assertThat(stored.getCNumber()).isEqualTo(3);
        assertThat(stored.getCNumberDisplay()).isEqualTo("D000000003");
        assertThat(stored.getDriverName()).isEqualTo("RAJU KUMAR");
        // The truck pairing lives on this row and no driver save may disturb it.
        assertThat(stored.getTruckRefId()).isEqualTo(11);
        verify(repository, never()).findMaxCNumber(any());
    }

    /** A driver that does carry its number keeps it, display and all. */
    @Test
    void anEditThatCarriesTheNumberingWritesItBackUnchanged() {
        DriverMaster stored = DriverMaster.builder()
                .id(9).companyRefId(COMPANY).driverName("RAJU")
                .cNumber(3).cNumberDisplay("D000000003").active(1)
                .build();
        when(repository.findById(9)).thenReturn(Optional.of(stored));
        when(repository.save(any(DriverMaster.class))).thenAnswer(call -> call.getArgument(0));

        DriverMasterDto edit = newDriverForm();
        edit.setId(9);
        edit.setCNumber(3);
        edit.setCNumberDisplay("D000000003");
        service.processDriver(edit, COMPANY);

        assertThat(stored.getCNumber()).isEqualTo(3);
        assertThat(stored.getCNumberDisplay()).isEqualTo("D000000003");
        verify(repository, never()).findMaxCNumber(any());
    }
}
