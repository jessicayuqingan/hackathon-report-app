package com.legacy.report.model;

import java.util.List;
import java.util.Map;

public class Report {
    private Long id;
    private String name;
    private String sql;
    private String description;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}