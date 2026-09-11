package my.maleva.api.module.ir.specification;

import jakarta.persistence.criteria.Predicate;
import my.maleva.api.module.ir.dto.IrSearchRequest;
import my.maleva.api.module.ir.entity.IrMaster;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the WHERE clause of the IR list.
 *
 * <p>Everything is a bound criteria predicate rather than concatenated text, so
 * a vessel name with an apostrophe is a value and never syntax.
 */
public final class IrMasterSpecification {

    private IrMasterSpecification() {
    }

    public static Specification<IrMaster> from(IrSearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("companyRefId"), request.getCompanyRefId()));
            predicates.add(builder.notEqual(root.get("active"), IrMaster.DELETED));

            // Half-open range. A BETWEEN on a datetime column drops anything
            // stamped after midnight on the last day.
            if (request.getFromDate() != null) {
                LocalDateTime start = request.getFromDate().atStartOfDay();
                predicates.add(builder.greaterThanOrEqualTo(root.get("irDate"), start));
            }
            if (request.getToDate() != null) {
                LocalDateTime end = request.getToDate().plusDays(1).atStartOfDay();
                predicates.add(builder.lessThan(root.get("irDate"), end));
            }

            addIdFilter(predicates, builder, root.get("irStatusRefId"), request.getIrStatusRefId());
            addIdFilter(predicates, builder, root.get("departmentRefId"), request.getDepartmentRefId());
            addIdFilter(predicates, builder, root.get("truckRefId"), request.getTruckRefId());
            addIdFilter(predicates, builder, root.get("employeeRefId"), request.getEmployeeRefId());
            addIdFilter(predicates, builder, root.get("driverRefId"), request.getDriverRefId());

            addContains(predicates, builder, root.get("departmentName"), request.getDepartmentName());
            addContains(predicates, builder, root.get("vesselName"), request.getVesselName());
            addContains(predicates, builder, root.get("truckNo"), request.getTruckNo());

            // "Still open" excludes the finished statuses by id. The service
            // resolves the codes to ids, so a re-seeded lookup table still works.
            List<Integer> excluded = request.getExcludeStatusRefIds();
            if (excluded != null && !excluded.isEmpty()) {
                predicates.add(builder.not(root.get("irStatusRefId").in(excluded)));
            }

            String search = trimToNull(request.getSearch());
            if (search != null) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(builder.or(
                        like(builder, root.get("description"), pattern),
                        like(builder, root.get("reason"), pattern),
                        like(builder, root.get("vesselName"), pattern),
                        like(builder, root.get("truckNo"), pattern),
                        like(builder, root.get("driverName"), pattern),
                        like(builder, root.get("employeeName"), pattern)));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** 0 means "all" on every dropdown in this system, so it is not a filter. */
    private static void addIdFilter(List<Predicate> predicates,
                                    jakarta.persistence.criteria.CriteriaBuilder builder,
                                    jakarta.persistence.criteria.Path<Integer> path,
                                    Integer value) {
        if (value != null && value != 0) {
            predicates.add(builder.equal(path, value));
        }
    }

    private static void addContains(List<Predicate> predicates,
                                    jakarta.persistence.criteria.CriteriaBuilder builder,
                                    jakarta.persistence.criteria.Path<String> path,
                                    String value) {
        String trimmed = trimToNull(value);
        if (trimmed != null) {
            predicates.add(like(builder, path, "%" + trimmed.toLowerCase() + "%"));
        }
    }

    private static Predicate like(jakarta.persistence.criteria.CriteriaBuilder builder,
                                  jakarta.persistence.criteria.Path<String> path,
                                  String pattern) {
        return builder.like(builder.lower(builder.coalesce(path, "")), pattern);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
