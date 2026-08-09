package com.atlantafx.core.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

public class SqlGenerator {

    /**
     * 判断字段是否为持久化字段：排除 id、static、transient、synthetic 字段
     */
    private static final Predicate<Field> PERSISTABLE = f ->
            !f.getName().equals("id")
                    && !Modifier.isStatic(f.getModifiers())
                    && !Modifier.isTransient(f.getModifiers())
                    && !f.isSynthetic();

    /**
     * 判断字段是否为表字段：包含 id，排除 static、transient、synthetic 字段
     */
    private static final Predicate<Field> TABLE_FIELD = f ->
            !Modifier.isStatic(f.getModifiers())
                    && !Modifier.isTransient(f.getModifiers())
                    && !f.isSynthetic();

    private static String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).value();
        }
        // 默认类名小写作为表名
        return clazz.getSimpleName().toLowerCase();
    }

    /**
     * 根据 @Table 实体类自动生成 CREATE TABLE IF NOT EXISTS 语句
     * <p>
     * 约定：Integer id 字段为 PRIMARY KEY AUTOINCREMENT，其他字段根据 Java 类型映射为 SQLite 类型。
     * 字段名会自动从驼峰命名转换为下划线命名。
     */
    public static String buildCreateTable(Class<?> clazz) {
        String tableName = getTableName(clazz);
        StringJoiner columns = new StringJoiner(", ");

        for (Field f : clazz.getDeclaredFields()) {
            if (!TABLE_FIELD.test(f)) continue;
            String columnName = f.getName();
            String columnDef = columnName + " " + toSqliteType(f.getType());

            // id 字段作为主键自增
            if ("id".equals(f.getName())) {
                columnDef += " PRIMARY KEY AUTOINCREMENT";
            }
            columns.add(columnDef);
        }

        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")";
    }

    /**
     * Java 类型 -> SQLite 类型映射
     */
    private static String toSqliteType(Class<?> javaType) {
        if (javaType == Integer.class || javaType == int.class
                || javaType == Long.class || javaType == long.class) {
            return "INTEGER";
        }
        if (javaType == Double.class || javaType == double.class
                || javaType == Float.class || javaType == float.class) {
            return "REAL";
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return "INTEGER";
        }
        return "TEXT";
    }

    /**
     * 生成 INSERT 语句: INSERT INTO table (col1, col2) VALUES (:col1, :col2)
     * 字段名会自动从驼峰命名转换为下划线命名。
     *
     * @param clazz
     * @return
     */
    public static String buildInsert(Class<?> clazz) {
        StringJoiner cols = new StringJoiner(",");
        StringJoiner vals = new StringJoiner(",");
        for (Field f : clazz.getDeclaredFields()) {
            if (!PERSISTABLE.test(f)) continue;
            String columnName = f.getName();
            cols.add(columnName);
            vals.add(":" + columnName);
        }
        return "INSERT INTO " + getTableName(clazz) + " (" + cols + ") VALUES (" + vals + ")";
    }

    /**
     * 构建 UPDATE 语句: UPDATE table SET col1=:col1, col2=:col2 WHERE id=:id
     * 字段名会自动从驼峰命名转换为下划线命名。
     *
     * @param clazz
     * @return
     */
    public static String buildUpdate(Class<?> clazz) {
        StringJoiner set = new StringJoiner(",");
        for (Field f : clazz.getDeclaredFields()) {
            if (!PERSISTABLE.test(f)) continue;
            String columnName = f.getName();
            set.add(columnName + "=:" + columnName);
        }
        return "UPDATE " + getTableName(clazz) + " SET " + set + " WHERE id=:id";
    }

    /**
     * 构建 SELECT 语句: SELECT * FROM table WHERE col1=:col1 AND col2=:col2
     * 字段名会自动从驼峰命名转换为下划线命名。
     *
     * @param clazz
     * @param conditionCols 条件列名列表（已经是下划线命名）
     * @return
     */
    public static String buildSelect(Class<?> clazz, List<String> conditionCols) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + getTableName(clazz));
        if (!conditionCols.isEmpty()) {
            sql.append(" WHERE ");
            StringJoiner where = new StringJoiner(" AND ");
            // conditionCols 已经是下划线命名，直接使用
            conditionCols.forEach(c -> where.add(c + "=:" + c));
            sql.append(where);
        }
        return sql.toString();
    }

    /**
     * 构建带排序的 SELECT 语句
     *
     * @param clazz          实体类
     * @param conditionCols  条件列名列表
     * @param orderByClauses 排序子句列表，每个元素格式为 "column ASC" 或 "column DESC"
     * @return SQL 语句
     */
    public static String buildSelect(Class<?> clazz, List<String> conditionCols, List<String> orderByClauses) {
        String baseSql = buildSelect(clazz, conditionCols);

        if (orderByClauses != null && !orderByClauses.isEmpty()) {
            StringBuilder sql = new StringBuilder(baseSql);
            sql.append(" ORDER BY ");
            StringJoiner orderBy = new StringJoiner(", ");
            orderByClauses.forEach(orderBy::add);
            sql.append(orderBy);
            return sql.toString();
        }

        return baseSql;
    }

    /**
     * 构建 DELETE 语句: DELETE FROM table WHERE id=:id
     *
     * @param clazz
     * @return
     */
    public static String buildDelete(Class<?> clazz) {
        return "DELETE FROM " + getTableName(clazz) + " WHERE id=:id";
    }

    /**
     * 构建带条件的 DELETE 语句: DELETE FROM table WHERE col1=:col1 AND col2=:col2
     *
     * @param clazz         实体类
     * @param whereColumns  条件列名列表（已经是下划线命名）
     * @return SQL 语句
     */
    public static String buildDeleteWithConditions(Class<?> clazz, List<String> whereColumns) {
        StringBuilder sql = new StringBuilder("DELETE FROM " + getTableName(clazz));
        if (whereColumns != null && !whereColumns.isEmpty()) {
            sql.append(" WHERE ");
            StringJoiner where = new StringJoiner(" AND ");
            whereColumns.forEach(c -> where.add(c + "=:" + c));
            sql.append(where);
        }
        return sql.toString();
    }
}
