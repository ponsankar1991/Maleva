package my.maleva.api.module.qutation.service;

import my.maleva.api.module.qutation.dto.CustomerQuotationDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.qutation.mapper.CustomerQuotationMapper;
import my.maleva.api.module.qutation.dto.CustomerDetailsDto;
import my.maleva.api.common.dto.ResponseViewModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.http.HttpStatus;
import my.maleva.api.module.qutation.entity.CustomerQuotation;
import my.maleva.api.module.qutation.repository.CustomerQuotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerQuotationService {

    private final CustomerQuotationRepository repository;
    private final CustomerQuotationMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    public CustomerQuotationService(CustomerQuotationRepository repository, CustomerQuotationMapper mapper, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CustomerQuotationDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerQuotationDto getById(Integer id) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CustomerQuotationDto create(CustomerQuotationDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CustomerQuotation ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CustomerQuotation saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerQuotationDto update(Integer id, CustomerQuotationDto dto) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CustomerQuotation saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerQuotation ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerQuotation not found: " + id));
        repository.delete(ent);
    }

    /**
     * Equivalent of C# SelectCustomerQuotationFormat2 - returns customer quotation details
     */
    public ResponseViewModel selectCustomerQuotationFormat2(Integer id, Integer jobId, Integer comid, String port, Integer quantity) {
        ResponseViewModel ro = new ResponseViewModel();
        try {
            String sql = "SELECT JD.Id as id, JD.JobMasterRefId as jobMasterRefId, JD.CustomerMasterRefId as customerMasterRefId, " +
                    "JD.ItemMasterRefId as itemMasterRefId, JM.Name as jobName, IM.PName as productName, IM.Prod_Code as productCode, " +
                    "CM.CustomerName as customerName, JD.MRP as mrp, JD.PurchaseRate as purchaseRate, JD.LandingCost as landingCost, " +
                    "JD.SalesRate as salesRate, JD.Active as active, JD.start as start, JD.ends as ends, JD.uom as uom, JD.port as port, JD.IsTransport as isTransport " +
                    "FROM CustomerQuotation JD " +
                    "LEFT JOIN JobTypeMaster JM ON JM.id = JD.JobMasterRefId " +
                    "LEFT JOIN ItemMaster IM ON IM.id = JD.ItemMasterRefId " +
                    "LEFT JOIN Customer CM ON CM.id = JD.CustomerMasterRefId " +
                    "WHERE JD.CompanyRefId = ? AND JD.JobMasterRefId = ? AND JD.CustomerMasterRefId = ? AND JD.Port = ? AND JD.Active = 1";

            RowMapper<CustomerDetailsDto> rowMapper = (rs, rowNum) -> CustomerDetailsDto.builder()
                    .id(rs.getInt("id"))
                    .jobMasterRefId(rs.getInt("jobMasterRefId"))
                    .customerMasterRefId(rs.getInt("customerMasterRefId"))
                    .itemMasterRefId(rs.getInt("itemMasterRefId"))
                    .jobName(rs.getString("jobName"))
                    .productName(rs.getString("productName"))
                    .productCode(rs.getString("productCode"))
                    .customerName(rs.getString("customerName"))
                    .mrp(rs.getFloat("mrp"))
                    .purchaseRate(rs.getFloat("purchaseRate"))
                    .landingCost(rs.getFloat("landingCost"))
                    .salesRate(rs.getFloat("salesRate"))
                    .active(rs.getInt("active"))
                    .isTransport(rs.getInt("isTransport"))
                    .start(rs.getInt("start"))
                    .ends(rs.getInt("ends"))
                    .uom(rs.getString("uom"))
                    .port(rs.getString("port"))
                    .build();

            List<CustomerDetailsDto> list = jdbcTemplate.query(sql, rowMapper, comid, jobId, id, port);

            ro.setSuccess(true);
            ro.setStatusCode(HttpStatus.OK.value());
            ro.setMessage("Success");
            ro.setData1(list);
        } catch (Exception ex) {
            ro.setSuccess(false);
            ro.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            ro.setMessage(ex.getMessage() != null ? ex.getMessage() : "Error");
        }
        return ro;
    }
}
