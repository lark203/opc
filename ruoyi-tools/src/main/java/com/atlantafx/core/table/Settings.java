package com.atlantafx.core.table;

import com.atlantafx.core.db.Table;

@Table("fx_settings")
public class Settings {
    private Long id;
    private String key;
    private String value;

    public Settings() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
