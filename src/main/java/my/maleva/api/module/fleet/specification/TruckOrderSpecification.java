package my.maleva.api.module.fleet.specification;

import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.entity.TruckOrder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE clause of the truck order calendar.
 *
 * Same shape as {@link TollEntrySpecification}. Two legacy behaviours are
 * corrected here rather than reproduced:
 *
 * <ul>
 *   <li>the status filter is a list. The legacy combo was multi-select but its
 *       value reached the server as {@code "Pending,Confirmed"} and was compared
 *       with {@code =}, so ticking two statuses always returned nothing;</li>
 *   <li>rows with {@code Active = 2} are excluded. The legacy query said
 *       {@code Active != 2}, which is the same thing today only because nothing
 *       could write any other value.</li>
 * </ul>
 */
public final class TruckOrderSpecification {

    private static final Integer ACTIVE = 1;

    private TruckOrderSpecification() {
    }
    public static Specification<TruckOrder> from(TruckOrderSearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("companyRefId"), request.getCompanyRefId()));
            predicates.add(builder.equal(root.get("active"), ACTIVE));
            if (request.getFromDate() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("orderDate"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("orderDate"), request.getToDate()));
            }
            if (request.getTruckRefId() != null && request.getTruckRefId() != 0) {
                predicates.add(builder.equal(root.get("truckRefId"), request.getTruckRefId()));
            }
            List<String> statuses = request.getStatuses();
            if (statuses != null && !statuses.isEmpty()) {
                List<String> wanted = statuses.stream()
                        .filter(status -> status != null && !status.isBlank())
                        .map(String::trim)
                        .toList();
                if (!wanted.isEmpty()) {
                    predicates.add(root.get("status").in(wanted));
                }
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
