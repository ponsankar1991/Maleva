package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.mapper.TruckMasterMapper;
import my.maleva.api.module.fleet.mapper.TruckMasterMapperImpl;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
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
 * Saving a truck from the Add / Edit Truck screen (POST /process).
 *
 * <p>The screen posts Id 0 for a new truck, the convention the legacy SP_Truck
 * payload used. Kept honest here because a non-null 0 made Spring Data read the
 * truck as an existing row and issue an UPDATE against Id 0, which matches
 * nothing: every insert failed with "Row was already updated or deleted by
 * another transaction ... with id '0'".
 *
 * <p>The real mapper is used rather than a mock - the numbering the insert
 * depends on travels through it.
 */
@ExtendWith(MockitoExtension.class)
class TruckMasterSaveTest {

    private static final Integer COMPANY = 6;

    @Mock private TruckMasterRepository repository;
    @Mock private DriverMasterRepository driverMasterRepository;
    @Spy private TruckMasterMapper mapper = new TruckMasterMapperImpl();

    @InjectMocks private TruckMasterServiceImpl service;

    /** What the Add Truck form posts: no id yet, and numbering left to the server. */
    private TruckMasterDto newTruckForm() {
        return TruckMasterDto.builder()
                .id(0)
                .companyRefId(COMPANY)
                .truckName("jsl 1234")
                .truckNumber("jsl 1234")
                .truckType("prime mover")
                .cNumber(0)
                .cNumberDisplay("AUTO")
                .active(1)
                .modifiedBy("KARTHICK")
                .build();
    }

    /** The row as it was handed to save(), and the id it carried at that moment. */
    private TruckMaster savedRow;
    private Integer idAtSave;

    /** Stands in for the IDENTITY column: records the row, then stamps the new id on it. */
    private void answerSaveWithId(int newId) {
        when(repository.save(any(TruckMaster.class))).thenAnswer(call -> {
            TruckMaster row = call.getArgument(0);
            savedRow = row;
            idAtSave = row.getId();
            row.setId(newId);
            return row;
        });
    }

    @Test
    void aNewTruckIsInsertedRatherThanUpdatedAgainstIdZero() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(7);
        answerSaveWithId(42);

        TruckMasterDto saved = service.processTruck(newTruckForm(), COMPANY);

        // Null id at save time is the whole fix: it is what makes this an INSERT.
        assertThat(idAtSave).isNull();
        assertThat(saved.getId()).isEqualTo(42);
        verify(repository, never()).findById(any());
    }

    @Test
    void aNewTruckIsGivenTheNextCNumberAndItsDisplay() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(7);
        answerSaveWithId(42);

        service.processTruck(newTruckForm(), COMPANY);

        assertThat(savedRow.getCNumber()).isEqualTo(8);
        assertThat(savedRow.getCNumberDisplay()).isEqualTo("T000000008");
    }

    /** The first truck of a company: nothing to count from. */
    @Test
    void theFirstTruckOfACompanyIsNumberOne() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(0);
        answerSaveWithId(1);

        service.processTruck(newTruckForm(), COMPANY);

        assertThat(savedRow.getCNumber()).isEqualTo(1);
        assertThat(savedRow.getCNumberDisplay()).isEqualTo("T000000001");
    }

    /**
     * Columns whose database default only applies when the column is left out of
     * the INSERT - which Hibernate never does. Without them a new truck is
     * invisible to the maintenance dashboard and the Truck Order Calendar.
     */
    @Test
    void aNewTruckIsOwnedOrderableAndActiveByDefault() {
        when(repository.findMaxCNumber(COMPANY)).thenReturn(7);
        answerSaveWithId(42);

        service.processTruck(newTruckForm(), COMPANY);

        TruckMaster row = savedRow;
        assertThat(row.getMalevaTruck()).isEqualTo(1);
        assertThat(row.getOrderableTruck()).isEqualTo(1);
        assertThat(row.getTruckStatus()).isEqualTo("ACTIVE");
        assertThat(row.getActive()).isEqualTo(1);
        assertThat(row.getCreatedDate()).isNotNull();
        assertThat(row.getModifiedDate()).isNotNull();
        assertThat(row.getModifiedBy()).isEqualTo("KARTHICK");
        assertThat(row.getTruckName()).isEqualTo("JSL 1234");
    }

    /**
     * An edit must keep the truck's number. The form posts 0 / "AUTO" whenever it
     * did not load the stored numbering, and that used to move the truck onto a
     * brand new number on every save.
     */
    @Test
    void anEditKeepsTheStoredNumbering() {
        TruckMaster stored = TruckMaster.builder()
                .id(9).companyRefId(COMPANY)
                .truckName("JSL 1234").truckNumber("JSL 1234").truckType("PRIME MOVER")
                .cNumber(3).cNumberDisplay("T000000003")
                .active(1).orderableTruck(1)
                .build();
        when(repository.findById(9)).thenReturn(Optional.of(stored));
        when(repository.save(any(TruckMaster.class))).thenAnswer(call -> call.getArgument(0));

        TruckMasterDto edit = newTruckForm();
        edit.setId(9);
        edit.setTruckName("jsl 9999");
        service.processTruck(edit, COMPANY);

        assertThat(stored.getCNumber()).isEqualTo(3);
        assertThat(stored.getCNumberDisplay()).isEqualTo("T000000003");
        assertThat(stored.getTruckName()).isEqualTo("JSL 9999");
        assertThat(stored.getOrderableTruck()).isEqualTo(1);
        verify(repository, never()).findMaxCNumber(any());
    }

    /** A truck that does carry its number keeps it, display and all. */
    @Test
    void anEditThatCarriesTheNumberingWritesItBackUnchanged() {
        TruckMaster stored = TruckMaster.builder()
                .id(9).companyRefId(COMPANY)
                .truckName("JSL 1234").truckNumber("JSL 1234").truckType("PRIME MOVER")
                .cNumber(3).cNumberDisplay("T000000003").active(1)
                .build();
        when(repository.findById(9)).thenReturn(Optional.of(stored));
        when(repository.save(any(TruckMaster.class))).thenAnswer(call -> call.getArgument(0));

        TruckMasterDto edit = newTruckForm();
        edit.setId(9);
        edit.setCNumber(3);
        edit.setCNumberDisplay("T000000003");
        service.processTruck(edit, COMPANY);

        assertThat(stored.getCNumber()).isEqualTo(3);
        assertThat(stored.getCNumberDisplay()).isEqualTo("T000000003");
        verify(repository, never()).findMaxCNumber(any());
    }
}
