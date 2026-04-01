package my.maleva.api.module.customer.repository;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.customer.dto.request.CustomerSelectRequest;
import my.maleva.api.module.customer.dto.response.CustomerJobNotifySelectDto;
import my.maleva.api.module.customer.dto.response.CustomerSelectDto;
import my.maleva.api.module.customer.entity.Customer;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomerQueryRepository {

    private final EntityManager em;

    public long countCustomers(CustomerSelectRequest req) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(s.id) FROM Customer s WHERE s.companyRefId = :companyId AND s.active != 2");

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", req.getCompanyId());

        appendKeywordFilterJpa(jpql, params, req);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);
        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    public List<CustomerSelectDto> findCustomers(CustomerSelectRequest req) {
        StringBuilder jpql = new StringBuilder();

        // Select the entity and the few joined display values — we'll map in Java to avoid constructor projection issues
        jpql.append("SELECT s, sm.sName, pt.termsName, ag.accountCode, cm.country ")
            .append("FROM Customer s ")
            .append("LEFT JOIN SymbolMaster sm ON s.symbolRefid = sm.id ")
            .append("LEFT JOIN PaymentTermsMaster pt ON s.paymentTermsRefid = pt.id ")
            .append("LEFT JOIN AccountsGroupMaster ag ON ag.id = s.accountRefid ")
            .append("LEFT JOIN CountryMaster cm ON s.countryId = cm.id ")
            .append("WHERE s.companyRefId = :companyId AND s.active != 2");

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", req.getCompanyId());

        appendKeywordFilterJpa(jpql, params, req);

        jpql.append(" ORDER BY s.id");

        TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
        params.forEach(query::setParameter);

        int start = Math.max(req.getStartIndex(), 0);
        query.setFirstResult(start);
        query.setMaxResults(req.getPageCount());

        List<Object[]> rows = query.getResultList();
        List<CustomerSelectDto> results = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            Customer s = (Customer) row[0];
            String sName = row[1] != null ? (String) row[1] : null;
            String termsName = row[2] != null ? (String) row[2] : null;
            String accountCode = row[3] != null ? (String) row[3] : null;
            String country = row[4] != null ? (String) row[4] : null;

            CustomerSelectDto dto = CustomerSelectDto.builder()
                    .id(s.getId())
                    .companyRefId(s.getCompanyRefId())
                    .cNumberDisplay(s.getCNumberDisplay())
                    .cNumber(s.getCNumber())
                    .customerName(s.getCustomerName())

                    .address1(s.getAddress1())
                    .address2(s.getAddress2())
                    .address3(s.getAddress3())
                    .city(s.getCity())
                    .state(s.getState())
                    .zipcode(s.getZipcode())
                    .countryId(s.getCountryId())
                    .email(s.getEmail())
                    .mobileNo(s.getMobileNo())
                    .userName(s.getUserName())
                    .password(s.getPassword())
                    .latitude(s.getLatitude())
                    .longitude(s.getLongitude())
                    .gstNo(s.getGstNo())
                    .tinNo(s.getTinNo())
                    .sstNo(s.getSstNo())
                    .tinType(s.getTintype())
                    .customerTin(s.getCustomerTin())
                    .bankName(s.getBankName())
                    .accountNo(s.getAccountNo())
                    .active(s.getActive())
                    .createdDate(s.getCreatedDate())
                    .modifiedDate(s.getModifiedDate())
                    .sName(sName)
                    .termsName(termsName)
                    .accountCode(accountCode)
                    .cmName(country)
                    .personId(s.getPersonId())
                    .tokenId(s.getTokenId())
                    .oEmail(s.getOEmail())
                    .oEmail1(s.getOEmail1())
                    .oName(s.getOName())
                    .oPhone(s.getOPhone())
                    .aEmail(s.getAEmail())
                    .aEmail1(s.getAEmail1())
                    .aName(s.getAName())
                    .aPhone(s.getAPhone())
                    .companyCode(s.getCompanyCode())
                    .expiryDate(s.getExpiryDate() != null ? s.getExpiryDate().toString() : null)
                    .updateId(s.getUpdateId())
                    .customerCity(s.getCustomerCity())
                    .serviceTaxType(s.getServiceTaxType())
                    .msicCode(s.getMsicCode())
                    .registrationNo(s.getRegistrationNo())
                    .exemptionNo(s.getExemptionNo())
                    .exemptionDetails(s.getExemptionDetails())
                    .symbolRefId(s.getSymbolRefid())
                    .paymentTermsRefId(s.getPaymentTermsRefid())
                    .eInvoice(s.getEInvoice())
                    .build();

            results.add(dto);
        }

        return results;
    }

    public List<CustomerJobNotifySelectDto> findCustomerJobNotifications(Integer customerMasterRefId, Integer saleOrderRefId) {
        String sql = """
                SELECT
                    COALESCE(CJ.Id, 0) AS Id,
                    CN.Name,
                    CN.Id AS CustomerDetailRefId,
                    COALESCE(CJ.Whatsapp, 0) AS Whatsapp,
                    COALESCE(CJ.Phone, '0') AS Phone,
                    COALESCE(CJ.Email, 0) AS Email,
                    CN.Whatsapp AS WhatsappDisplay,
                    CN.Whatsapp AS PhoneDisplay,
                    CN.Email AS EmailDisplay
                FROM CustomerNotifyDetails CN
                OUTER APPLY (
                    SELECT TOP 1
                        CJ1.Id,
                        CJ1.Whatsapp,
                        CJ1.Phone,
                        CJ1.Email
                    FROM CustomerJobNotify CJ1
                    WHERE CJ1.CustomerDetailRefId = CN.Id
                      AND :saleOrderRefId > 0
                      AND CJ1.SaleOrderRefId = :saleOrderRefId
                    ORDER BY CJ1.Id DESC
                ) CJ
                WHERE CN.CustomerMasterRefId = :customerMasterRefId
                ORDER BY CN.Id
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("customerMasterRefId", customerMasterRefId);
        query.setParameter("saleOrderRefId", saleOrderRefId != null ? saleOrderRefId : 0);

        List<Object[]> rows = query.getResultList();
        List<CustomerJobNotifySelectDto> results = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            CustomerJobNotifySelectDto dto = CustomerJobNotifySelectDto.builder()
                    .id(toInteger(row[0]))
                    .name(toStringValue(row[1]))
                    .customerDetailRefId(toInteger(row[2]))
                    .whatsapp(toInteger(row[3]))
                    .phone(toInteger(row[4]))
                    .email(toInteger(row[5]))
                    .whatsappDisplay(toStringValue(row[6]))
                    .phoneDisplay(toStringValue(row[7]))
                    .emailDisplay(toStringValue(row[8]))
                    .build();

            results.add(dto);
        }

        return results;
    }

    private void appendKeywordFilterJpa(StringBuilder jpql, Map<String, Object> params, CustomerSelectRequest req) {
        if (req.getKeyword() == null || req.getKeyword().isBlank() || req.getColumn() == null) {
            return;
        }

        String keyword = req.getKeyword();
        switch (req.getColumn()) {
            case "CustomerName" -> {
                jpql.append(" AND LOWER(s.customerName) LIKE :keyword");
                params.put("keyword", "%" + keyword.toLowerCase() + "%");
            }
            case "MobileNo" -> {
                jpql.append(" AND s.mobileNo LIKE :keyword");
                params.put("keyword", "%" + keyword + "%");
            }
            case "Id" -> {
                try {
                    int id = Integer.parseInt(keyword);
                    jpql.append(" AND s.id = :id");
                    params.put("id", id);
                } catch (NumberFormatException ex) {
                    // skip invalid id
                }
            }
            default -> {
                // unknown column - ignore
            }
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Integer.parseInt(text);
    }

    private String toStringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
