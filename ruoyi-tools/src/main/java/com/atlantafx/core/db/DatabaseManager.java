package com.atlantafx.core.db;

import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.atlantafx.core.util.AppClassScanner;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile Sql2o sql2o;
    private static final String DB_NAME = "app_data.db";

    /**
     * 数据库写操作互斥锁，防止SQLite并发写冲突
     */
    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();

    /**
     * SQLite BUSY 错误码重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * SQLite BUSY 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 200;

    /**
     * 需要自动建表的实体类列表，新增表只需在此添加
     */
    private static final List<Class<?>> TABLE_ENTITIES = new ArrayList<>();

    // =========================================================================
    // 静态管线生命周期：利用 ClassGraph 拦截包含 @Table 注解的实体类 facts
    // =========================================================================
    static {
        long startTime = System.currentTimeMillis();
        log.info("开始执行底层数据仓库 [ @Table ] 实体智能自动扫描...");

        // 复用全局 ClassGraph 扫描结果，避免与页面扫描重复全量扫描
        try {
            ScanResult scanResult = AppClassScanner.get();

            // 抓取全量带有 com.atlantafx.core.db.Table 注解的类
            ClassInfoList tableClasses = scanResult.getClassesWithAnnotation(Table.class.getName());
            List<Class<?>> loadedClasses = tableClasses.loadClasses();

            if (loadedClasses != null && !loadedClasses.isEmpty()) {
                TABLE_ENTITIES.addAll(loadedClasses);
                log.info("数据仓库扫描清算完成，成功捕获实体数: [ {} 个 ], 耗时: {} ms",
                        TABLE_ENTITIES.size(), (System.currentTimeMillis() - startTime));

                // 打印捕获的实体日志以便逆向核对
                TABLE_ENTITIES.forEach(clazz -> log.debug("自动注册 ORM 实体 -> {}", clazz.getName()));
            } else {
                log.warn("警告：数据仓库未在指定包下捕获到任何带有 @Table 注解的实体类！");
            }
        } catch (Exception e) {
            log.error("核心错误：执行 @Table 自动化类路径扫描阶段发生致命崩溃！", e);
        }
    }

    public static synchronized void init() {
        // 1. 确定数据库文件路径
        Path dbPath = Path.of("data", DB_NAME).toAbsolutePath();
        File dbFile = dbPath.toFile();

        // 确保目录存在
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        sql2o = new Sql2o("jdbc:sqlite:" + dbPath + "?busy_timeout=3000", null, null);

        // 3. 根据 @Table 实体类自动建表
        createTablesIfNotExist();
    }

    private static void createTablesIfNotExist() {
        try (Connection con = sql2o.open()) {
            for (Class<?> entityClass : TABLE_ENTITIES) {
                String sql = SqlGenerator.buildCreateTable(entityClass);
                con.createQuery(sql).executeUpdate();
                log.debug("自动建表: {}", sql);
            }
            log.info("所有数据库表初始化成功。");
        } catch (Exception e) {
            log.error("数据库初始化失败：{}", e.getMessage());
        }
    }

    /**
     * 获取全局 Sql2o 实例，用于执行 CRUD
     */
    public static Sql2o db() {
        if (sql2o == null) {
            synchronized (DatabaseManager.class) {
                if (sql2o == null) init();
            }
        }
        return sql2o;
    }

    /**
     * 获取数据库写操作锁
     */
    public static void lockWrite() {
        WRITE_LOCK.lock();
    }

    /**
     * 释放数据库写操作锁
     */
    public static void unlockWrite() {
        if (WRITE_LOCK.isHeldByCurrentThread()) {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * 执行带重试机制的数据库写操作
     *
     * @param operation 数据库操作（返回受影响的行数或结果）
     * @param <T>       返回类型
     * @return 操作结果
     * @throws RuntimeException 重试失败后抛出异常
     */
    public static <T> T executeWithRetry(DatabaseOperation<T> operation) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                lockWrite();
                try (Connection con = db().open()) {
                    return operation.execute(con);
                } finally {
                    unlockWrite();
                }
            } catch (Exception e) {
                lastException = e;
                if (isSqliteBusyError(e)) {
                    retryCount++;
                    log.warn("SQLite数据库忙，正在重试 ({}/{}): {}", retryCount, MAX_RETRY_COUNT, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("数据库操作被中断", ie);
                    }
                } else {
                    throw new RuntimeException("数据库操作失败", e);
                }
            }
        }

        throw new RuntimeException("数据库操作重试" + MAX_RETRY_COUNT + "次后仍然失败: " +
                (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }

    /**
     * 判断异常是否为SQLite BUSY错误
     */
    private static boolean isSqliteBusyError(Exception e) {
        String message = e.getMessage();
        return message != null && (message.contains("SQLITE_BUSY") || message.contains("database is locked"));
    }

    /**
     * 数据库操作接口
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute(Connection connection) throws Exception;
    }

    public static void close() {
        // Sql2o 对象本身不需要 close，但我们需要确保所有 Connection 都已通过 try-with-resources 关闭。
        // 如果你使用了数据源（DataSource），可以在这里关闭它。
        log.info("本地数据库资源已安全释放。");
    }
}