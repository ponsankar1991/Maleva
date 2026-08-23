package my.maleva.api.module.fleet.specification;

import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.fleet.dto.request.TollEntrySearchRequest;
import my.maleva.api.module.fleet.entity.TollEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE clause of the toll entry list.
 *
 * Same shape as {@link FuelEntrySpecification}: the legacy service concatenated
 * these conditions into the statement text, which is what made Search
 * injectable.
 */
public final class TollEntrySpecification {

    private static final Integer ACTIVE = 1;

    private TollEntrySpecification() {
    }

    public static Specification<TollEntry> from(TollEntrySearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("companyRefId"), request.getCompanyRefId()));
            predicates.add(builder.equal(root.get("active"), ACTIVE));

            if (request.getTruckRefId() != null && request.getTruckRefId() != 0) {
                predicates.add(builder.equal(root.get("truckRefid"), request.getTruckRefId()));
            }
            if (request.getEmployeeRefId() != null && request.getEmployeeRefId() != 0) {
                predicates.add(builder.equal(root.get("employeeRefId"), request.getEmployeeRefId()));
            }

            boolean searchingByNumber = request.getSearch() != null && !request.getSearch().isBlank();
            if (searchingByNumber) {
                predicates.add(builder.equal(root.get("cNumberDisplay"), request.getSearch().trim()));
            } else if (request.getFromDate() != null && request.getToDate() != null) {
                // Half-open range: the legacy BETWEEN on a datetime column dropped
                // anything stamped after midnight on the last day.
                LocalDateTime start = request.getFromDate().atStartOfDay();
                LocalDateTime end = request.getToDate().plusDays(1).atStartOfDay();
                predicates.add(builder.greaterThanOrEqualTo(root.get("saleDate"), start));
                predicates.add(builder.lessThan(root.get("saleDate"), end));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
