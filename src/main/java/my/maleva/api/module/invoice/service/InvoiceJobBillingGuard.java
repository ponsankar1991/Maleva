package my.maleva.api.module.invoice.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * One job, one invoice.
 *
 * <p>{@link InvoiceSaveGuard} stops the <em>same</em> Save arriving twice. It
 * cannot stop a second, different save of a job that is already billed: a
 * second tab, the page reloaded after a save that seemed to hang, the job
 * picked again from a list loaded before the first save finished, two people
 * billing the same job, or any save after a backend restart emptied the
 * guard's memory. Every one of those produced a second invoice with its own
 * number for a job that already had one. The database is the only place that
 * sees all of them, so the rule lives here, inside the save transaction.
 *
 * <p><b>How it holds under concurrency.</b> The sale order rows are locked
 * first ({@code UPDLOCK, HOLDLOCK}, in id order so two multi-job saves cannot
 * deadlock), and the lock is kept until the save commits. A second save of the
 * same job therefore waits for the first to finish, and then reads the links
 * the first one committed and is refused. A plain "check, then write" would let
 * both saves pass the check before either wrote.
 *
 * <p><b>What counts as billed</b> is the same three links
 * {@code SaleOrderInvoiceLinkService} uses for the sale order screen:
 * {@code SaleOrderMaster.InvoiceNo}, a {@code SaleMasterReference} row, or a
 * {@code SaleDetails} line, on an invoice that is still active. InvoiceNo alone
 * misses jobs that legacy linked only through lines or references.
 *
 * <p><b>Edits.</b> A job already on the invoice being edited stays allowed even
 * if some other invoice also claims it: those historic duplicates exist, and
 * refusing would make the invoice uneditable. Only a job <em>added</em> by the
 * edit has to be free.
 */
@Component
public class InvoiceJobBillingGuard {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceJobBillingGuard.class);

    private final NamedParameterJdbcTemplate jdbc;

    public InvoiceJobBillingGuard(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** An invoice that already bills one of the requested jobs. */
    record Link(int saleOrderId, String jobNo, int invoiceId, String invoiceNo) {
    }

    /**
     * Locks the jobs this save bills and refuses it when any of them is already
     * billed by another active invoice.
     *
     * <p>Must run inside the save transaction and before anything is written:
     * the lock has to outlive the write, and an edit clears its own links when
     * it writes.
     *
     * @throws InvalidRequestException naming each job and the invoice that has it
     */
    public void lockAndRequireUnbilled(SaleInvoiceRequestDTO request,
                                       List<SaleInvoiceDetailRequestDTO> lines,
                                       Integer companyId) {
        Set<Integer> jobIds = jobsBilledBy(request, lines);
        if (jobIds.isEmpty()) {
            return;
        }
        int invoiceId = request.getId() == null ? 0 : request.getId();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", new ArrayList<>(jobIds))
                .addValue("comid", companyId);

        // Held until commit. A concurrent save of any of these jobs queues here.
        jdbc.queryForList("SELECT Id FROM SaleOrderMaster WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE CompanyRefId = :comid AND Id IN (:ids) ORDER BY Id", params, Integer.class);

        // No NOLOCK: this must see what a save that just released the lock committed.
        List<Link> links = jdbc.query("""
                SELECT L.SaleOrderId, ISNULL(SO.CNumberDisplay, '') AS JobNo,
                       SM.Id AS InvoiceId, ISNULL(SM.CNumberDisplay, '') AS InvoiceNo
                FROM (
                    SELECT Id AS SaleOrderId, InvoiceNo AS InvoiceId FROM SaleOrderMaster
                    WHERE CompanyRefId = :comid AND Id IN (:ids) AND ISNULL(InvoiceNo, 0) <> 0
                    UNION
                    SELECT SaleOrderMasterRefId, SaleMasterRefId FROM SaleMasterReference
                    WHERE SaleOrderMasterRefId IN (:ids)
                    UNION
                    SELECT SaleOrderMasterRefId, SaleMasterRefId FROM SaleDetails
                    WHERE SaleOrderMasterRefId IN (:ids)
                ) L
                JOIN SaleMaster SM ON SM.Id = L.InvoiceId AND SM.CompanyRefId = :comid AND SM.Active = 1
                JOIN SaleOrderMaster SO ON SO.Id = L.SaleOrderId
                ORDER BY SO.CNumberDisplay, SM.Id
                """, params, (rs, i) -> new Link(rs.getInt("SaleOrderId"), rs.getString("JobNo").trim(),
                rs.getInt("InvoiceId"), rs.getString("InvoiceNo").trim()));

        List<Link> conflicts = conflicts(links, invoiceId);
        if (conflicts.isEmpty()) {
            return;
        }
        logger.warn("Refused {} of invoice {} for company {}: jobs already billed {}",
                invoiceId == 0 ? "a create" : "an edit", invoiceId, companyId, conflicts);
        throw new InvalidRequestException(message(conflicts));
    }

    /**
     * Every job the save would link, by any of the routes the writer stamps:
     * the header's single sale order, the reference list, and the lines.
     */
    static Set<Integer> jobsBilledBy(SaleInvoiceRequestDTO request, List<SaleInvoiceDetailRequestDTO> lines) {
        List<Integer> candidates = new ArrayList<>();
        candidates.add(request.getSaleOrderMasterNo());
        if (request.getSaleOrderRefIds() != null) {
            candidates.addAll(request.getSaleOrderRefIds());
        }
        lines.stream().filter(Objects::nonNull)
                .map(SaleInvoiceDetailRequestDTO::getSaleOrderMasterRefId)
                .forEach(candidates::add);
        // Sorted, so the rows are locked in id order. A TreeSet refuses null,
        // hence the filter before anything goes in.
        Set<Integer> ids = new TreeSet<>();
        candidates.stream().filter(id -> id != null && id > 0).forEach(ids::add);
        return ids;
    }

    /**
     * The links that block this save: another invoice holding a job that this
     * invoice does not already hold.
     */
    static List<Link> conflicts(List<Link> links, int invoiceId) {
        Set<Integer> alreadyOnThisInvoice = links.stream()
                .filter(link -> invoiceId > 0 && link.invoiceId() == invoiceId)
                .map(Link::saleOrderId)
                .collect(Collectors.toSet());
        Map<String, Link> distinct = new LinkedHashMap<>();
        for (Link link : links) {
            if (link.invoiceId() != invoiceId && !alreadyOnThisInvoice.contains(link.saleOrderId())) {
                distinct.putIfAbsent(link.saleOrderId() + ":" + link.invoiceId(), link);
            }
        }
        return new ArrayList<>(distinct.values());
    }

    static String message(List<Link> conflicts) {
        String each = conflicts.stream()
                .map(link -> "job " + label(link.jobNo(), link.saleOrderId())
                        + " is already on invoice " + label(link.invoiceNo(), link.invoiceId()))
                .collect(Collectors.joining("; "));
        return "Not saved: " + each + ". Open that invoice to change it instead of creating another.";
    }

    private static String label(String display, int id) {
        return display == null || display.isBlank() ? String.valueOf(id) : display;
    }
}
