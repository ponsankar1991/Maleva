package my.maleva.api.module.saleorder.specification;

import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.master.entity.RulesTypeMaster;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SaleOrderSpecification - Dynamic JPA Specification for filtering SaleOrderMaster
 * Provides type-safe query building to prevent SQL injection
 * Replaces the .NET string concatenation logic
 */
public class SaleOrderSpecification {

    /**
     * Build comprehensive filter specification for SaleOrder search
     * Implements all the filtering logic from the .NET SelectSaleOrder method
     */
    public static Specification<SaleOrderMaster> buildFilter(Integer companyId, Integer customerId,
            Integer jobId, Integer employeeId, Integer dashboardStatus, String statusList, Integer statusId,
            Boolean completeStatusNotShow, Integer remarks, String offVesselName, String loadingVesselName,
            String search, Boolean invoice, Boolean eta, Integer etaType, LocalDate fromDate,
            LocalDate toDate, Boolean pickup, Boolean invoiceCheck) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by company and active status
            predicates.add(cb.equal(root.get("companyRefId"), companyId));
            predicates.add(cb.equal(root.get("active"), 1));

            // Filter by customer ID if provided
            if (customerId != null && customerId != 0) {
                predicates.add(cb.equal(root.get("customerRefId"), customerId));
            }

            // Filter by job ID if provided
            if (jobId != null && jobId != 0) {
                predicates.add(cb.equal(root.get("jobMasterRefId"), jobId));
            }

            // Filter by employee ID with RulesTypeMaster subquery logic
            if (employeeId != null && employeeId != 0) {
                if (dashboardStatus != null && dashboardStatus == 2) {
                    // Subquery: IN (select SubEmployeeId from RulesTypeMaster ...) UNION ALL select employeeId
                    Subquery<Integer> subquery = query.subquery(Integer.class);
                    Root<RulesTypeMaster> rulesRoot = subquery.from(RulesTypeMaster.class);
                    subquery.select(rulesRoot.get("subEmployeeId"))
                            .where(cb.and(
                                    cb.equal(rulesRoot.get("masterEmployeeId"), employeeId),
                                    cb.equal(rulesRoot.get("active"), 1),
                                    cb.equal(rulesRoot.get("companyRefId"), companyId)
                            ));

                    predicates.add(
                            cb.or(
                                    root.get("employeeRefId").in(subquery),
                                    cb.equal(root.get("employeeRefId"), employeeId)
                            )
                    );
                } else {
                    // Simple employee filter
                    predicates.add(cb.equal(root.get("employeeRefId"), employeeId));
                }
            }

            // Filter by status list (prioritized) OR single status ID
            if (statusList != null && !statusList.trim().isEmpty()) {
                // Parse comma-separated status list
                List<Integer> statusIds = parseIntegerList(statusList);

                // Subquery: JStatus IN (select Id from JobStatusMaster where Mid in (...))
                Subquery<Integer> statusSubquery = query.subquery(Integer.class);
                Root<JobStatusMaster> jobStatusRoot = statusSubquery.from(JobStatusMaster.class);
                statusSubquery.select(jobStatusRoot.get("id"))
                        .where(jobStatusRoot.get("mid").in(statusIds));

                predicates.add(
                        cb.or(
                                root.get("jStatus").in(statusSubquery),
                                root.get("jStatus").in(statusIds)
                        )
                );
            } else if (statusId != null && statusId != 0) {
                // Single status ID filter
                Subquery<Integer> statusSubquery = query.subquery(Integer.class);
                Root<JobStatusMaster> jobStatusRoot = statusSubquery.from(JobStatusMaster.class);
                statusSubquery.select(jobStatusRoot.get("id"))
                        .where(cb.equal(jobStatusRoot.get("mid"), statusId));

                predicates.add(
                        cb.or(
                                root.get("jStatus").in(statusSubquery),
                                cb.equal(root.get("jStatus"), statusId)
                        )
                );
            }

            // Exclude completed status (status != 8)
            if (completeStatusNotShow != null && completeStatusNotShow) {
                predicates.add(cb.notEqual(root.get("jStatus"), 8));
            }

            // Filter by remarks
            if (remarks != null) {
                if (remarks == 1) {
                    // Remarks not empty
                    predicates.add(cb.and(
                            cb.isNotNull(root.get("remarks")),
                            cb.notEqual(root.get("remarks"), "")
                    ));
                } else if (remarks == 2) {
                    // Remarks empty
                    predicates.add(
                            cb.or(
                                    cb.isNull(root.get("remarks")),
                                    cb.equal(root.get("remarks"), "")
                            )
                    );
                }
            }

            // Filter by loading vessel name (LIKE search)
            if (offVesselName != null && !offVesselName.trim().isEmpty()) {
                predicates.add(cb.like(root.get("offvesselname"), "%" + offVesselName + "%"));
            }

            // Filter by offloading vessel name (LIKE search)
            if (loadingVesselName != null && !loadingVesselName.trim().isEmpty()) {
                predicates.add(cb.like(root.get("loadingvesselname"), "%" + loadingVesselName + "%"));
            }

            // Search filter (overrides date filters)
            if (search != null && !search.trim().isEmpty()) {
                if (invoice != null && invoice) {
                    // Search in SaleMaster.CNumberDisplay
                    Subquery<Integer> invoiceSubquery = query.subquery(Integer.class);
                    Root<SaleMaster> saleRoot = invoiceSubquery.from(SaleMaster.class);
                    invoiceSubquery.select(saleRoot.get("id"))
                            .where(cb.like(saleRoot.get("cNumberDisplay"), "%" + search + "%"));
                    predicates.add(root.get("invoiceNo").in(invoiceSubquery));
                } else {
                    // Search in SaleOrderMaster.CNumberDisplay
                    predicates.add(cb.like(root.get("cNumberDisplay"), "%" + search + "%"));
                }
            } else {
                // Date range filters (only applied when search is empty)
                if (fromDate != null && toDate != null) {
                    LocalDateTime startDateTime = LocalDateTime.of(fromDate, LocalTime.MIN);
                    LocalDateTime endDateTime = LocalDateTime.of(toDate, LocalTime.MAX);

                    if (eta != null && eta) {
                        // ETA filter with type selection
                        if (etaType != null) {
                            if (etaType == 1) {
                                // OETA between dates
                                predicates.add(cb.between(root.get("oeta"), startDateTime, endDateTime));
                            } else if (etaType == 2) {
                                // ETA between dates
                                predicates.add(cb.between(root.get("eta"), startDateTime, endDateTime));
                            } else {
                                // ETA OR OETA between dates
                                predicates.add(cb.or(
                                        cb.between(root.get("eta"), startDateTime, endDateTime),
                                        cb.between(root.get("oeta"), startDateTime, endDateTime)
                                ));
                            }
                        }
                    } else if (pickup != null && pickup) {
                        // Pickup date filter
                        predicates.add(cb.between(root.get("pickupDate"), startDateTime, endDateTime));
                    } else {
                        // Default: SaleDate filter
                        predicates.add(cb.between(root.get("saleDate"), startDateTime, endDateTime));
                    }
                }
            }

            // Invoice check filter (invoiceNo = 0)
            if (invoiceCheck != null && invoiceCheck) {
                predicates.add(cb.equal(root.get("invoiceNo"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Parse comma-separated integer string to list
     */
    private static List<Integer> parseIntegerList(String commaSeparatedIds) {
        return Arrays.stream(commaSeparatedIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}

