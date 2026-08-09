package com.atlantafx.core.db;

import com.atlantafx.util.TaskRunner;
import org.sql2o.Connection;

import java.util.*;
import java.util.function.Consumer;

public class UniversalRepository<T> {
    protected final Class<T> type;
    // 不可变条件副本，每次 where() 生成新对象时携带
    private final Map<String, Object> conditions;
    // 不可变排序子句列表，每次 orderBy() 生成新对象时携带
    private final List<String> orderByClauses;

    public UniversalRepository(Class<T> type) {
        this.type = type;
        this.conditions = Map.of();
        this.orderByClauses = List.of();
    }

    private UniversalRepository(Class<T> type, Map<String, Object> conditions, List<String> orderByClauses) {
        this.type = type;
        this.conditions = conditions;
        this.orderByClauses = orderByClauses;
    }

    /**
     * --- 链式构造器（不可变，每次返回新实例） ---
     **/

    /**
     * 添加查询条件
     *
     * @param column 字段名（驼峰命名，会自动转换为下划线）
     * @param value  条件值
     * @return 新的 UniversalRepository 实例
     */
    public UniversalRepository<T> where(String column, Object value) {
        Map<String, Object> newConditions = new LinkedHashMap<>(this.conditions);
        newConditions.put(column, value);
        return new UniversalRepository<>(type, Collections.unmodifiableMap(newConditions), this.orderByClauses);
    }

    /**
     * 添加排序规则（升序）
     *
     * @param column 排序字段名
     * @return 新的 UniversalRepository 实例
     */
    public UniversalRepository<T> orderBy(String column) {
        return orderBy(column, true);
    }

    /**
     * 添加排序规则
     *
     * @param column    排序字段名（驼峰命名，会自动转换为下划线）
     * @param ascending true 为升序(ASC)，false 为降序(DESC)
     * @return 新的 UniversalRepository 实例
     */
    public UniversalRepository<T> orderBy(String column, boolean ascending) {
        List<String> newOrderByClauses = new ArrayList<>(this.orderByClauses);
        String direction = ascending ? "ASC" : "DESC";
        newOrderByClauses.add(column + " " + direction);
        return new UniversalRepository<>(type, this.conditions, Collections.unmodifiableList(newOrderByClauses));
    }

    /**
     * --- 异步执行器 (查询) ---
     **/

    public void find(Consumer<List<T>> onSuccess) {
        String sql = SqlGenerator.buildSelect(type, new ArrayList<>(conditions.keySet()), orderByClauses);

        TaskRunner.buildSimple(() -> {
            try (Connection con = DatabaseManager.db().open()) {
                var query = con.createQuery(sql);
                conditions.forEach(query::addParameter);
                return query.executeAndFetch(type);
            }
        }).onSuccess(onSuccess).run();
    }

    /**
     * 同步查询所有记录（无过滤条件）
     * 适用于简单场景，注意不要在 UI 线程调用
     */
    public List<T> findAll() {
        String sql = SqlGenerator.buildSelect(type, new ArrayList<>(conditions.keySet()), orderByClauses);
        try (Connection con = DatabaseManager.db().open()) {
            var query = con.createQuery(sql);
            conditions.forEach(query::addParameter);
            return query.executeAndFetch(type);
        }
    }

    /**
     * --- 异步执行器 (写操作) ---
     **/

    /**
     * 异步插入实体，成功后返回生成的 ID
     *
     * @param entity    要插入的实体
     * @param onSuccess 成功回调，参数为生成的 ID（Long 类型）
     */
    public void insertAsync(T entity, Consumer<Long> onSuccess) {
        String sql = SqlGenerator.buildInsert(type);
        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            con.createQuery(sql).bind(entity).executeUpdate();
            Number key = con.createQuery("SELECT last_insert_rowid()").executeScalar(Number.class);
            return key != null ? key.longValue() : null;
        })).onSuccess(onSuccess).run();
    }

    public void updateAsync(T entity, Runnable onSuccess) {
        String sql = SqlGenerator.buildUpdate(type);
        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            con.createQuery(sql).bind(entity).executeUpdate();
            return true;
        })).onSuccess(r -> {
            if (onSuccess != null) onSuccess.run();
        }).run();
    }

    public void deleteByIdAsync(Object id, Runnable onSuccess) {
        String sql = SqlGenerator.buildDelete(type);
        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
            return true;
        })).onSuccess(r -> {
            if (onSuccess != null) onSuccess.run();
        }).run();
    }

    /**
     * --- 批量操作 (写操作) ---
     **/

    /**
     * 异步批量插入实体列表
     *
     * @param entities  要插入的实体列表
     * @param onSuccess 成功回调，参数为成功插入的数量
     */
    public void batchInsertAsync(List<T> entities, Consumer<Integer> onSuccess) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            int count = 0;
            String sql = SqlGenerator.buildInsert(type);
            for (T entity : entities) {
                con.createQuery(sql).bind(entity).executeUpdate();
                count++;
            }
            return count;
        })).onSuccess(onSuccess).run();
    }

    /**
     * 异步批量更新实体列表
     *
     * @param entities  要更新的实体列表
     * @param onSuccess 成功回调，参数为成功更新的数量
     */
    public void batchUpdateAsync(List<T> entities, Consumer<Integer> onSuccess) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            int count = 0;
            String sql = SqlGenerator.buildUpdate(type);
            for (T entity : entities) {
                con.createQuery(sql).bind(entity).executeUpdate();
                count++;
            }
            return count;
        })).onSuccess(onSuccess).run();
    }

    /**
     * 异步批量删除实体列表（基于ID）
     *
     * @param ids       要删除的ID列表
     * @param onSuccess 成功回调，参数为成功删除的数量
     */
    public void batchDeleteByIdsAsync(List<Object> ids, Consumer<Integer> onSuccess) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            int count = 0;
            String sql = SqlGenerator.buildDelete(type);
            for (Object id : ids) {
                con.createQuery(sql).addParameter("id", id).executeUpdate();
                count++;
            }
            return count;
        })).onSuccess(onSuccess).run();
    }

    /**
     * 异步根据条件批量删除
     * <p>
     * 注意：此方法会删除所有满足 where() 条件的记录，请谨慎使用
     *
     * @param onSuccess 成功回调，参数为成功删除的数量
     */
    public void batchDeleteByConditionAsync(Consumer<Integer> onSuccess) {
        if (conditions.isEmpty()) {
            throw new IllegalStateException("批量删除必须指定条件，避免误删所有数据");
        }

        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            List<String> whereColumns = new ArrayList<>(conditions.keySet());
            String sql = SqlGenerator.buildDeleteWithConditions(type, whereColumns);
            var query = con.createQuery(sql);
            conditions.forEach(query::addParameter);
            return query.executeUpdate().getResult();
        })).onSuccess(onSuccess).run();
    }
}