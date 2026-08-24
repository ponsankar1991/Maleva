package my.maleva.api.module.fleet.specification;

import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;
import my.maleva.api.module.fleet.entity.PassEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE clause for the truck pass lists.
 *
 * Shared by levi and auto pass entries: the two tables carry the same columns,
 * so the same predicates apply to both. The legacy services each assembled this
 * string by concatenation, so a document number or an enter/exit value
 * containing an apostrophe changed the statement rather than filtering it.
 */
public final class PassEntrySpecification {

    private static final Integer ACTIVE = 1;

    private PassEntrySpecification() {
    }

    public static <T extends PassEntry> Specification<T> from(PassEntrySearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("companyRefId"), request.getCompanyRefId()));
            predicates.add(builder.equal(root.get("active"), ACTIVE));

            if (isSearchingByNumber(request)) {
                // The legacy code reset its WHERE string when a number was typed,
                // so the number wins over every other filter. Kept, because the
                // screen relies on it to find an entry outside the date range.
                predicates.add(builder.equal(root.get("cNumberDisplay"), request.getSearch().trim()));
                return builder.and(predicates.toArray(new Predicate[0]));
            }

            if (isSet(request.getTruckRefId())) {
                predicates.add(builder.equal(root.get("truckRefid"), request.getTruckRefId()));
            }
            if (isSet(request.getDriverRefId())) {
                predicates.add(builder.equal(root.get("driverRefId"), request.getDriverRefId()));
            }
            if (isSet(request.getRtiRefId())) {
                predicates.add(builder.equal(root.get("rtiRefId"), request.getRtiRefId()));
            }
            if (isSet(request.getEmployeeRefId())) {
                predicates.add(builder.equal(root.get("employeeRefId"), request.getEmployeeRefId()));
            }
            if (hasText(request.getEnterLink())) {
                predicates.add(builder.equal(root.get("enterLink"), request.getEnterLink().trim()));
            }
            if (hasText(request.getExitLink())) {
                predicates.add(builder.equal(root.get("exitLink"), request.getExitLink().trim()));
            }

            if (request.getFromDate() != null && request.getToDate() != null) {
                // Half-open range: SaleDate is a datetime, and the legacy BETWEEN
                // dropped anything stamped after midnight on the last day.
                LocalDateTime start = request.getFromDate().atStartOfDay();
                LocalDateTime end = request.getToDate().plusDays(1).atStartOfDay();
                predicates.add(builder.greaterThanOrEqualTo(root.get("saleDate"), start));
                predicates.add(builder.lessThan(root.get("saleDate"), end));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static boolean isSearchingByNumber(PassEntrySearchRequest request) {
        return hasText(request.getSearch());
    }

    private static boolean isSet(Integer value) {
        return value != null && value != 0;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
