package my.maleva.api.module.fleet.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.fleet.dto.request.TruckOrderSearchRequest;
import my.maleva.api.module.fleet.entity.TruckOrder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            // An equality on TruckRefId never matches NULL, so a truck filter
            // leaves out OUTSIDE orders by itself; without one they are listed.
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
            if (hasText(request.getOrigin())) {
                predicates.add(placeMatches(builder, root.get("origin"), request.getOrigin()));
            }
            if (hasText(request.getDestination())) {
                predicates.add(placeMatches(builder, root.get("destination"), request.getDestination()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * "Contains", ignoring case and spaces on both sides.
     *
     * <p>Place names are typed by hand - {@code WEST PORT} and {@code WESTPORT},
     * {@code PASIR GUDANG} and {@code PASIRGUDANG} are all in the data - so the
     * stored value has its spaces squeezed out before it is compared. The typed
     * text is escaped, so a {@code %} or {@code _} in it is matched literally
     * rather than acting as a wildcard.
     */
    private static Predicate placeMatches(CriteriaBuilder builder,
                                          Expression<String> column,
                                          String wanted) {
        String key = wanted.replace(" ", "").toUpperCase(Locale.ENGLISH)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        Expression<String> squeezed = builder.upper(
                builder.function("REPLACE", String.class, column, builder.literal(" "), builder.literal("")));
        return builder.like(squeezed, "%" + key + "%", '\\');
    }
}
