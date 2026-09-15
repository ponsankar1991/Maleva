package my.maleva.api.module.supplier.mapper;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.entity.Supplier;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the supplier mapper copies EVERY field, in every direction.
 *
 * <p>Written by reflection rather than field by field, because the failure it
 * guards is a field nobody thought to check: MapStruct dropped ten columns
 * without an error (see SupplierMapper). Every field on both classes is given a
 * distinct value and compared by name after mapping, so a column added to the
 * entity and DTO later is covered the day it is added.
 */
class SupplierMapperTest {

    private final SupplierMapper mapper = new SupplierMapperImpl();

    @Test
    void entityAndDtoDeclareTheSameFields() {
        assertThat(fieldNames(SupplierDto.class)).isEqualTo(fieldNames(Supplier.class));
    }

    @Test
    void toDtoCopiesEveryField() throws Exception {
        Supplier entity = new Supplier();
        fillEveryField(entity);

        assertNoFieldDropped(entity, mapper.toDto(entity));
    }

    @Test
    void toEntityCopiesEveryField() throws Exception {
        SupplierDto dto = new SupplierDto();
        fillEveryField(dto);

        assertNoFieldDropped(dto, mapper.toEntity(dto));
    }

    @Test
    void updateCopiesEveryFieldOntoAnExistingEntity() throws Exception {
        SupplierDto dto = new SupplierDto();
        fillEveryField(dto);
        Supplier entity = new Supplier();

        mapper.updateEntityFromDto(dto, entity);

        assertNoFieldDropped(dto, entity);
    }

    /** The six boxes whose loss erased real data, named so a failure reads plainly. */
    @Test
    void theLegacyRightHandBoxesSurviveARoundTrip() {
        Supplier entity = new Supplier();
        entity.setAEmail1("C2584563222");   // TIN NO
        entity.setAPhone("W10-1808");       // SST REG NO
        entity.setOEmail("49301");          // MSIC CODE
        entity.setOEmail1("02");            // SERVICE TAX TYPE
        entity.setOName("MAYBANK");         // BANK NAME
        entity.setOPhone("514011223344");   // ACCOUNT NUMBER
        entity.setCNumberDisplay("SU000000319");

        Supplier back = mapper.toEntity(mapper.toDto(entity));

        assertThat(back.getAEmail1()).isEqualTo("C2584563222");
        assertThat(back.getAPhone()).isEqualTo("W10-1808");
        assertThat(back.getOEmail()).isEqualTo("49301");
        assertThat(back.getOEmail1()).isEqualTo("02");
        assertThat(back.getOName()).isEqualTo("MAYBANK");
        assertThat(back.getOPhone()).isEqualTo("514011223344");
        assertThat(back.getCNumberDisplay()).isEqualTo("SU000000319");
    }

    /** The mapper's null-ignoring update must not blank what the DTO leaves out. */
    @Test
    void updateLeavesFieldsTheDtoDoesNotCarry() {
        Supplier entity = new Supplier();
        entity.setOName("MAYBANK");
        entity.setCNumber(319);

        SupplierDto partial = new SupplierDto();
        partial.setSupplierName("RENAMED");
        mapper.updateEntityFromDto(partial, entity);

        assertThat(entity.getSupplierName()).isEqualTo("RENAMED");
        assertThat(entity.getOName()).isEqualTo("MAYBANK");
        assertThat(entity.getCNumber()).isEqualTo(319);
    }

    // ─── reflection helpers ─────────────────────────────────────────────

    private static List<Field> instanceFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .peek(f -> f.setAccessible(true))
                .toList();
    }

    private static Set<String> fieldNames(Class<?> type) {
        return instanceFields(type).stream().map(Field::getName).collect(Collectors.toCollection(java.util.TreeSet::new));
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
            } else if (type == BigDecimal.class) {
                value = BigDecimal.valueOf(seed, 2);
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

    private static void assertNoFieldDropped(Object source, Object target) throws IllegalAccessException {
        Map<String, Field> targetFields = new TreeMap<>();
        for (Field field : instanceFields(target.getClass())) {
            targetFields.put(field.getName(), field);
        }

        List<String> dropped = new ArrayList<>();
        for (Field field : instanceFields(source.getClass())) {
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
