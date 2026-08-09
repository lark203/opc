package com.atlantafx.features.model;

import com.atlantafx.core.db.Table;

@Table("fx_test_users") // 对应数据库表名
public class UserEntity {
    private Long id;
    private String name;
    private String email;

    // 必须有无参构造函数 (Sql2o 需要)
    public UserEntity() {
    }

    public UserEntity(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}