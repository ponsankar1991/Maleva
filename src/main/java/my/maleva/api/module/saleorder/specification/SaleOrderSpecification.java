package my.maleva.api.module.saleorder.specification;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.master.entity.RulesTypeMaster;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
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
     *
     * Takes the filter DTO directly: the previous 21-positional-argument signature
     * had four adjacent Boolean parameters (invoice / eta / pickup / invoiceCheck)
     * that any re-ordering would have silently swapped at the call site.
     */
    public static Specification<SaleOrderMaster> buildFilter(SaleOrderFilterDTO filter) {
        Integer companyId = filter.getComid();
        Integer customerId = filter.getId();
        Integer saleOrderId = filter.getSaleOrderId();
        Integer jobId = filter.getJId();
        Integer employeeId = filter.getEmployeeid();
        Integer dashboardStatus = filter.getDashboardStatus();
        String statusList = filter.getStatusList();
        Integer statusId = filter.getStatusid();
        Boolean completeStatusNotShow = filter.getCompletestatusnotshow();
        Integer remarks = filter.getRemarks();
        String offVesselName = filter.getOffvesselname();
        String loadingVesselName = filter.getLoadingvesselname();
        String search = filter.getSearch();
        Boolean invoice = filter.getInvoice();
        Boolean eta = filter.getEta();
        Integer etaType = filter.getEtaType();
        LocalDate fromDate = filter.getFromdate();
        LocalDate toDate = filter.getTodate();
        Boolean pickup = filter.getPickup();
        Boolean invoiceCheck = filter.getInvoicecheck();
        String portName = filter.getPortName();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by company and active status
            predicates.add(cb.equal(root.get("companyRefId"), companyId));
            predicates.add(cb.equal(root.get("active"), SaleOrderApiConstants.ACTIVE_STATUS));

            // Filter by customer ID if provided
            if (customerId != null && customerId != 0) {
                predicates.add(cb.equal(root.get("customerRefId"), customerId));
            }

            // Filter by the sale order row itself. The Push PO hand-off knows the
            // exact id, so it does not have to hope the job number is filled in
            // and unique; when it is set the row is identified outright.
            if (saleOrderId != null && saleOrderId != 0) {
                predicates.add(cb.equal(root.get("id"), saleOrderId));
            }

            // Filter by job ID if provided
            if (jobId != null && jobId != 0) {
                predicates.add(cb.equal(root.get("jobMasterRefId"), jobId));
            }

            // Filter by employee ID with RulesTypeMaster subquery logic (ignored if searching by job number)
            if (employeeId != null && employeeId != 0 && (search == null || search.trim().isEmpty())) {
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
                        .where(jobStatusRoot.get("mId").in(statusIds));

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
                        .where(cb.equal(jobStatusRoot.get("mId"), statusId));

                predicates.add(
                        cb.or(
                                root.get("jStatus").in(statusSubquery),
                                cb.equal(root.get("jStatus"), statusId)
                        )
                );
            }

            // Exclude completed status (status != 8)
            if (completeStatusNotShow != null && completeStatusNotShow) {
                predicates.add(cb.notEqual(root.get("jStatus"), SaleOrderApiConstants.COMPLETED_JOB_STATUS));
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
            String normalizedSearch = search != null ? search.trim().toLowerCase() : null;
            if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                String likeSearch = "%" + normalizedSearch + "%";
                if (invoice != null && invoice) {
                    // Search in SaleMaster.CNumberDisplay using either invoice id linkage or SaleOrderMasterNo.
                    Subquery<Integer> invoiceSubquery = query.subquery(Integer.class);
                    Root<SaleMaster> saleRoot = invoiceSubquery.from(SaleMaster.class);

                    Predicate invoiceDisplayMatches = cb.like(
                            cb.lower(saleRoot.get("cNumberDisplay")),
                            likeSearch
                    );

                    Predicate invoiceIdMatches = cb.and(
                            cb.isNotNull(root.get("invoiceNo")),
                            cb.greaterThan(root.get("invoiceNo"), 0),
                            cb.equal(saleRoot.get("id"), root.get("invoiceNo"))
                    );

                    Predicate saleOrderLinkMatches = cb.equal(
                            saleRoot.get("saleOrderMasterNo"),
                            root.get("id")
                    );

                    invoiceSubquery.select(saleRoot.get("id"))
                            .where(cb.and(
                                    cb.equal(saleRoot.get("companyRefId"), companyId),
                                    cb.equal(saleRoot.get("active"), SaleOrderApiConstants.ACTIVE_STATUS),
                                    invoiceDisplayMatches,
                                    cb.or(invoiceIdMatches, saleOrderLinkMatches)
                            ));

                    predicates.add(cb.exists(invoiceSubquery));
                } else {
                    // Search in SaleOrderMaster.CNumberDisplay
                    predicates.add(cb.like(cb.lower(root.get("cNumberDisplay")), likeSearch));
                }
            } else {
                // Date range filters (only applied when search is empty).
                //
                // Skipped outright when a sale order id was given: that addresses one
                // row, and SaleOrderFilterHelper.validateFilter defaults a missing range
                // to TODAY - so an id lookup for any order raised before today would be
                // filtered down to nothing and read as "not found".
                boolean addressesOneRow = saleOrderId != null && saleOrderId != 0;
                if (fromDate != null && toDate != null && !addressesOneRow) {
                    LocalDateTime startDateTime = LocalDateTime.of(fromDate, LocalTime.MIN);
                    LocalDateTime endDateTime = LocalDateTime.of(toDate, LocalTime.of(23, 59, 59));

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
                predicates.add(
                        cb.or(
                                cb.equal(root.get("invoiceNo"), 0),
                                cb.isNull(root.get("invoiceNo"))
                        )
                );
            }

            // Port Name filter (SPort OR OPort)
            if (portName != null && !portName.trim().isEmpty()) {
                predicates.add(cb.or(
                        cb.equal(root.get("sPort"), portName),
                        cb.equal(root.get("oPort"), portName)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Parse comma-separated integer string to list
     */
    private static List<Integer> parseIntegerList(String commaSeparatedIds) {
        try {
            return Arrays.stream(commaSeparatedIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_STATUS_LIST_INVALID, exception);
        }
    }
}


