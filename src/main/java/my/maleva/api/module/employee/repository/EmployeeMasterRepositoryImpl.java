package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.module.employee.dto.EmployeeSearchRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class EmployeeMasterRepositoryImpl implements EmployeeMasterRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeMasterRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EmployeeAllDto> searchEmployees(EmployeeSearchRequest request) {
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("SELECT S.*, Ag.AccountCode ");
        sql.append("FROM EmployeeMaster S WITH (NOLOCK) ");
        sql.append("INNER JOIN AccountsGroupMaster Ag WITH (NOLOCK) ON Ag.Id = S.AccountRefid ");
        sql.append("WHERE S.CompanyRefId = :comid AND S.Active != 2 ");
        params.addValue("comid", request.getComid());

        appendTypeFilter(request.getType(), sql, params);

        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            sql.append("ORDER BY S.Id ");
            
            // Handle pagination defaults based on legacy logic
            int startIndex = request.getStartindex() != null ? request.getStartindex() : 0;
            int pageCount = request.getPageCount() != null ? request.getPageCount() : 10;
            
            if (startIndex == -1) {
                int count = countSearchEmployees(request);
                if (count > pageCount) {
                    startIndex = (count / pageCount) * pageCount;
                } else {
                    startIndex = 0;
                }
            }
            
            sql.append("OFFSET :startIndex ROWS FETCH NEXT :pageCount ROWS ONLY");
            params.addValue("startIndex", startIndex);
            params.addValue("pageCount", pageCount);
        } else {
            String column = request.getColumn();
            if ("EmployeeName".equalsIgnoreCase(column)) {
                sql.append("AND S.EmployeeName LIKE :keyword ");
                params.addValue("keyword", "%" + keyword + "%");
            } else if ("MobileNo".equalsIgnoreCase(column)) {
                sql.append("AND S.MobileNo LIKE :keyword ");
                params.addValue("keyword", "%" + keyword + "%");
            } else if ("Id".equalsIgnoreCase(column)) {
                sql.append("AND S.Id = :idKeyword ");
                params.addValue("idKeyword", keyword);
            } else if ("All".equalsIgnoreCase(column)) {
                // Legacy "All" condition has no keyword filtering, it just returns everything matching company + active + type
                // So no additional WHERE clauses are appended.
            } else {
                throw new IllegalArgumentException("Invalid column specified for search");
            }
        }

        return jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(EmployeeAllDto.class));
    }

    @Override
    public int countSearchEmployees(EmployeeSearchRequest request) {
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("SELECT COUNT(S.Id) ");
        sql.append("FROM EmployeeMaster S WITH (NOLOCK) ");
        sql.append("WHERE S.CompanyRefId = :comid AND S.Active != 2 ");
        params.addValue("comid", request.getComid());

        appendTypeFilter(request.getType(), sql, params);

        Integer count = jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
        return count != null ? count : 0;
    }

    private void appendTypeFilter(String type, StringBuilder sql, MapSqlParameterSource params) {
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND S.EmployeeType = :type ");
            params.addValue("type", type);
        }
    }
}
