package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.DriverMasterDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
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
 * Proves the driver mapper copies every field it is meant to, in every direction.
 *
 * <p>Written by reflection, like TruckMasterMapperTest and SupplierMapperTest,
 * because the failure it guards is a field nobody thought to check: with
 * Lombok's builder in play MapStruct dropped CNumber and CNumberDisplay from
 * toDto and toEntity without an error, while updateFromDto mapped them. A column
 * added to the entity and the DTO later is covered the day it is added.
 */
class DriverMasterMapperTest {

    private final DriverMasterMapper mapper = new DriverMasterMapperImpl();

    /**
     * Fields the DTO carries that no DriverMaster column answers: the account
     * code comes from AccountsGroupMaster and the leaves from LeaveRequest, both
     * filled in by the service after mapping.
     */
    private static final Set<String> DTO_ONLY = Set.of("accountCode", "leaves");

    @Test
    void entityAndDtoDeclareTheSameFieldsApartFromTheKnownExtras() {
        assertThat(fieldNames(DriverMaster.class))
                .containsExactlyInAnyOrderElementsOf(minus(fieldNames(DriverMasterDto.class), DTO_ONLY));
    }

    @Test
    void toDtoCopiesEveryField() throws Exception {
        DriverMaster entity = new DriverMaster();
        fillEveryField(entity);

        assertNoFieldDropped(entity, mapper.toDto(entity), Set.of());
    }

    @Test
    void toEntityCopiesEveryField() throws Exception {
        DriverMasterDto dto = new DriverMasterDto();
        fillEveryField(dto);

        assertNoFieldDropped(dto, mapper.toEntity(dto), DTO_ONLY);
    }

    @Test
    void updateCopiesEveryFieldOntoAnExistingEntity() throws Exception {
        DriverMasterDto dto = new DriverMasterDto();
        fillEveryField(dto);
        DriverMaster entity = new DriverMaster();

        mapper.updateFromDto(dto, entity);

        assertNoFieldDropped(dto, entity, DTO_ONLY);
    }

    /** The two the builder dropped, named so a failure reads plainly. */
    @Test
    void theDriverNumberingSurvivesARoundTrip() {
        DriverMaster entity = new DriverMaster();
        entity.setCNumber(123);
        entity.setCNumberDisplay("D000000123");
        entity.setDriverName("RAJU");

        DriverMaster back = mapper.toEntity(mapper.toDto(entity));

        assertThat(back.getCNumber()).isEqualTo(123);
        assertThat(back.getCNumberDisplay()).isEqualTo("D000000123");
        assertThat(back.getDriverName()).isEqualTo("RAJU");
    }

    /** The null-ignoring update must not blank what the DTO leaves out. */
    @Test
    void updateLeavesFieldsTheDtoDoesNotCarry() {
        DriverMaster entity = new DriverMaster();
        entity.setCNumber(123);
        entity.setCNumberDisplay("D000000123");
        entity.setTruckRefId(11);

        DriverMasterDto partial = new DriverMasterDto();
        partial.setDriverName("RENAMED");
        mapper.updateFromDto(partial, entity);

        assertThat(entity.getDriverName()).isEqualTo("RENAMED");
        assertThat(entity.getCNumber()).isEqualTo(123);
        assertThat(entity.getCNumberDisplay()).isEqualTo("D000000123");
        assertThat(entity.getTruckRefId()).isEqualTo(11);
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

    private static Set<String> minus(Set<String> names, Set<String> removed) {
        Set<String> result = new java.util.TreeSet<>(names);
        result.removeAll(removed);
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
            } else if (type == List.class) {
                value = List.of();
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
