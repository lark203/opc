package com.atlantafx.features.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 统一 API 返回格式包装类
 */
public record Result<T>(
        int code,
        String msg,
        T data
) {
    // 快捷判断是否成功
    @JsonIgnore
    public boolean isSuccess() {
        return code == 200 || code == 0; // 根据你司接口规范调整
    }
}