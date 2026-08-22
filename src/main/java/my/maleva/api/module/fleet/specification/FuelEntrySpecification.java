package my.maleva.api.module.fleet.specification;

import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.fleet.dto.request.FuelEntrySearchRequest;
import my.maleva.api.module.fleet.entity.FuelEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE clause of the fuel entry list.
 *
 * The legacy service assembled the same conditions by string concatenation,
 * which is what made {@code Search} injectable. Each condition here is a bound
 * parameter, and a filter that was not supplied simply contributes nothing.
 */
public final class FuelEntrySpecification {

    private static final Integer ACTIVE = 1;

    private FuelEntrySpecification() {
    }

    public static Specification<FuelEntry> from(FuelEntrySearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("companyRefId"), request.getCompanyRefId()));
            predicates.add(builder.equal(root.get("active"), ACTIVE));

            if (request.getTruckRefId() != null && request.getTruckRefId() != 0) {
                predicates.add(builder.equal(root.get("truckRefid"), request.getTruckRefId()));
            }
            if (request.getDriverRefId() != null && request.getDriverRefId() != 0) {
                predicates.add(builder.equal(root.get("driverRefId"), request.getDriverRefId()));
            }
            if (request.getEmployeeRefId() != null && request.getEmployeeRefId() != 0) {
                predicates.add(builder.equal(root.get("employeeRefId"), request.getEmployeeRefId()));
            }

            boolean searchingByNumber = request.getSearch() != null && !request.getSearch().isBlank();
            if (searchingByNumber) {
                // Legacy behaviour: a fuel number wins over every other filter,
                // including the date range, so a document can be found by number
                // without knowing when it was raised.
                predicates.add(builder.equal(root.get("cNumberDisplay"), request.getSearch().trim()));
            } else if (request.getFromDate() != null && request.getToDate() != null) {
                // Half-open range on a datetime column: >= day start, < next day
                // start. The legacy BETWEEN on a datetime silently dropped every
                // entry stamped later than midnight on the last day.
                LocalDateTime start = request.getFromDate().atStartOfDay();
                LocalDateTime end = request.getToDate().plusDays(1).atStartOfDay();
                predicates.add(builder.greaterThanOrEqualTo(root.get("saleDate"), start));
                predicates.add(builder.lessThan(root.get("saleDate"), end));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
