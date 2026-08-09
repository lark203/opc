package com.atlantafx.core.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库静态网关：DB.from(User.class).where(...).find(...)
 */
public class DB {
    private static final Map<Class<?>, UniversalRepository<?>> REPO_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> UniversalRepository<T> from(Class<T> entityClass) {
        return (UniversalRepository<T>) REPO_CACHE.computeIfAbsent(entityClass,
                k -> new UniversalRepository<>(entityClass));
    }
}