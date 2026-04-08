package com.legacy.report.dao;

import com.legacy.report.model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class ReportDao {
    
    @Autowired
    private JdbcTemplate jdbc;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // 业务逻辑在 DAO 里
    public List<Report> findAll() {
        // 没有注释，不知道这个SQL是干什么的
        String sql = "SELECT id, name, sql, description FROM report_config WHERE is_deleted = 0";
        return jdbc.query(sql, new ReportMapper());
    }
    
    public Report findById(Long id) {
        String sql = "SELECT id, name, sql, description FROM report_config WHERE id = ? AND is_deleted = 0";
        List<Report> results = jdbc.query(sql, new ReportMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }
    
    public List<Report> findAllByIds(Collection<Long> reportIds) {
        String sql = "SELECT id, name, sql, description FROM report_config WHERE is_deleted = 0 AND id IN (:reportIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("reportIds", reportIds);
        return namedParameterJdbcTemplate.query(sql, parameters, new ReportMapper());
    }
    
    // 直接执行传入的SQL，没有任何安全检查
    public List<Map<String, Object>> executeSql(String sql) {
        return jdbc.queryForList(sql);
    }
    
    public List<Map<String, Object>> executeSql(String sql, Map<String, Object> parameters) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        if (parameters != null) {
            parameters.forEach(parameterSource::addValue);
        }
        return namedParameterJdbcTemplate.queryForList(sql, parameterSource);
    }
    
    public void save(Report report) {
        // 硬编码的INSERT语句
        String sql = "INSERT INTO report_config (name, sql, description) VALUES (?, ?, ?)";
        jdbc.update(sql, report.getName(), report.getSql(), report.getDescription());
    }
    
    // update 和 delete 都没有
    
    private static class ReportMapper implements RowMapper<Report> {
        @Override
        public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
            Report r = new Report();
            r.setId(rs.getLong("id"));
            r.setName(rs.getString("name"));
            r.setSql(rs.getString("sql"));
            r.setDescription(rs.getString("description"));
            return r;
        }
    }
}