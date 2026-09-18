package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.entity.TruckMaster;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the truck mapper copies every field it is meant to, in every direction.
 *
 * <p>Written by reflection, like SupplierMapperTest, because the failure it
 * guards is a field nobody thought to check: with Lombok's builder in play
 * MapStruct dropped CNumber and CNumberDisplay from toDto and toEntity without
 * an error, while updateEntityFromDto mapped them - so a new truck reached the
 * NOT NULL CNumberDisplay column with nothing in it, and an edit reopened with
 * the numbering blank. A column added to the entity and the DTO later is covered
 * the day it is added.
 */
class TruckMasterMapperTest {

    private final TruckMasterMapper mapper = new TruckMasterMapperImpl();

    /**
     * Fields the DTO carries that no TruckMaster column answers: the account code
     * comes from AccountsGroupMaster and the driver from DriverMaster, both
     * filled in by the service after mapping.
     */
    private static final Set<String> DTO_ONLY = Set.of("accountCode", "driverRefId", "driverName");

    /**
     * Fields the entity carries that the truck form never posts. OrderableTruck
     * belongs to the Truck Order Calendar's fleet configuration; the truck save
     * must not be able to switch a truck out of the calendar by omission.
     */
    private static final Set<String> ENTITY_ONLY = Set.of("orderableTruck");

    @Test
    void entityAndDtoDeclareTheSameFieldsApartFromTheKnownExtras() {
        assertThat(minus(fieldNames(TruckMaster.class), ENTITY_ONLY))
                .containsExactlyInAnyOrderElementsOf(minus(fieldNames(TruckMasterDto.class), DTO_ONLY));
    }

    @Test
    void toDtoCopiesEveryField() throws Exception {
        TruckMaster entity = new TruckMaster();
        fillEveryField(entity);

        assertNoFieldDropped(entity, mapper.toDto(entity), ENTITY_ONLY);
    }

    @Test
    void toEntityCopiesEveryField() throws Exception {
        TruckMasterDto dto = new TruckMasterDto();
        fillEveryField(dto);

        assertNoFieldDropped(dto, mapper.toEntity(dto), DTO_ONLY);
    }

    @Test
    void updateCopiesEveryFieldOntoAnExistingEntity() throws Exception {
        TruckMasterDto dto = new TruckMasterDto();
        fillEveryField(dto);
        TruckMaster entity = new TruckMaster();

        mapper.updateEntityFromDto(dto, entity);

        assertNoFieldDropped(dto, entity, DTO_ONLY);
    }

    /** The two the builder dropped, named so a failure reads plainly. */
    @Test
    void theTruckNumberingSurvivesARoundTrip() {
        TruckMaster entity = new TruckMaster();
        entity.setCNumber(319);
        entity.setCNumberDisplay("T000000319");
        entity.setTruckNumber("JSL 1234");

        TruckMaster back = mapper.toEntity(mapper.toDto(entity));

        assertThat(back.getCNumber()).isEqualTo(319);
        assertThat(back.getCNumberDisplay()).isEqualTo("T000000319");
        assertThat(back.getTruckNumber()).isEqualTo("JSL 1234");
    }

    /** The null-ignoring update must not blank what the DTO leaves out. */
    @Test
    void updateLeavesFieldsTheDtoDoesNotCarry() {
        TruckMaster entity = new TruckMaster();
        entity.setCNumber(319);
        entity.setCNumberDisplay("T000000319");
        entity.setOrderableTruck(1);

        TruckMasterDto partial = new TruckMasterDto();
        partial.setTruckName("RENAMED");
        mapper.updateEntityFromDto(partial, entity);

        assertThat(entity.getTruckName()).isEqualTo("RENAMED");
        assertThat(entity.getCNumber()).isEqualTo(319);
        assertThat(entity.getCNumberDisplay()).isEqualTo("T000000319");
        assertThat(entity.getOrderableTruck()).isEqualTo(1);
    }

    // ─── reflection helpers ─────────────────────────────────────────────

    private static List<Field> instanceFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .peek(f -> f.setAccessible(true))
                .toList();
    }

    private static Set<String> fieldNames(Class<?> type) {
        return instanceFields(type).stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));
    }

    @SafeVarargs
    private static Set<String> minus(Set<String> names, Set<String>... removed) {
        Set<String> result = new java.util.TreeSet<>(names);
        Arrays.stream(removed).forEach(result::removeAll);
        return result;
    }

    /** A distinct, non-null value per field, so a swapped or dropped mapping cannot pass by accident. */
    private static void fillEveryField(Object target) throws IllegalAccessException {
        int seed = 1;
        for (Field field : instanceFields(target.getClass())) {
            Class<?> type = field.getType();
            Object value;
            if (type == String.class) {
                value = field.getName() + "-value";
            } else if (type == Integer.class) {
                value = seed;
            } else if (type == LocalDate.class) {
                value = LocalDate.of(2027, 1, 1).plusDays(seed);
            } else if (type == LocalDateTime.class) {
                value = LocalDateTime.of(2027, 1, 1, 0, 0).plusMinutes(seed);
            } else {
                throw new IllegalStateException("No test value for " + field.getName() + " of type " + type);
            }
            field.set(target, value);
            seed++;
        }
    }

    private static void assertNoFieldDropped(Object source, Object target, Set<String> notMapped)
            throws IllegalAccessException {
        Map<String, Field> targetFields = new TreeMap<>();
        for (Field field : instanceFields(target.getClass())) {
            targetFields.put(field.getName(), field);
        }

        List<String> dropped = new ArrayList<>();
        for (Field field : instanceFields(source.getClass())) {
            if (notMapped.contains(field.getName())) {
                continue;
            }
            Field counterpart = targetFields.get(field.getName());
            Object expected = field.get(source);
            Object actual = counterpart == null ? null : counterpart.get(target);
            if (!expected.equals(actual)) {
                dropped.add(field.getName() + " (expected " + expected + ", got " + actual + ")");
            }
        }
        assertThat(dropped).as("fields the mapper did not copy").isEmpty();
    }
}
