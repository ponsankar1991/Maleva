package my.maleva.api.module.zbentry.mapper;

import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveRequest;
import my.maleva.api.module.zbentry.entity.ZbEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "spring")
public interface ZbEntryMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mapping(target = "entryDate", source = "entryDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "stringToBigDecimal")
    ZbEntryResponse toDto(ZbEntry entity);

    // Keep fields as String matching the Entity definitions
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyRefId", ignore = true)
    ZbEntry toEntity(ZbEntrySaveRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyRefId", ignore = true)
    void updateEntity(@MappingTarget ZbEntry entity, ZbEntrySaveRequest request);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Logically gracefully handling corrupted legacy data
            return null;
        }
    }

    @Named("stringToBigDecimal")
    default BigDecimal stringToBigDecimal(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(amountStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
