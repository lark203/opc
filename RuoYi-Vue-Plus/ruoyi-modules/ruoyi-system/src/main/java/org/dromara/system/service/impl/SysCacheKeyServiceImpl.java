package org.dromara.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.system.domain.bo.CacheKeyBo;
import org.dromara.system.domain.vo.CacheKeyDetailVo;
import org.dromara.system.domain.vo.CacheKeyVo;
import org.dromara.system.domain.vo.RedisInfoVo;
import org.dromara.system.service.ISysCacheKeyService;
import org.redisson.api.RScript;
import org.redisson.api.RType;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 缓存键监控服务实现
 *
 * @author JunoYi
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysCacheKeyServiceImpl implements ISysCacheKeyService {

    private final RedissonConnectionFactory connectionFactory;

    /**
     * 获取 Redis 服务器信息。
     *
     * @return Redis 信息
     */
    @Override
    public RedisInfoVo getRedisInfo() {
        Properties info;
        Long dbSize;
        RedisConnection connection = connectionFactory.getConnection();
        try {
            info = connection.commands().info();
            dbSize = connection.commands().dbSize();
        } finally {
            // 归还连接给连接池
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
        if (info == null) {
            info = new Properties();
        }

        RedisInfoVo vo = new RedisInfoVo();
        // Server 信息
        vo.setVersion(info.getProperty("redis_version"));
        vo.setMode(info.getProperty("redis_mode"));
        vo.setUptimeInSeconds(parseLong(info.getProperty("uptime_in_seconds")));

        // Clients 信息
        vo.setConnectedClients(parseInt(info.getProperty("connected_clients")));

        // Memory 信息
        vo.setUsedMemory(info.getProperty("used_memory"));
        vo.setUsedMemoryHuman(info.getProperty("used_memory_human"));
        vo.setUsedMemoryPeak(info.getProperty("used_memory_peak"));
        vo.setUsedMemoryPeakHuman(info.getProperty("used_memory_peak_human"));

        // Keyspace 信息
        vo.setDbSize(dbSize);

        // Stats 信息
        vo.setKeyspaceHits(parseLong(info.getProperty("keyspace_hits")));
        vo.setKeyspaceMisses(parseLong(info.getProperty("keyspace_misses")));
        vo.setInstantaneousOpsPerSec(parseLong(info.getProperty("instantaneous_ops_per_sec")));
        vo.setTotalNetInputBytes(formatBytes(parseLong(info.getProperty("total_net_input_bytes"))));
        vo.setTotalNetOutputBytes(formatBytes(parseLong(info.getProperty("total_net_output_bytes"))));

        // 计算命中率
        long hits = ObjectUtil.defaultIfNull(vo.getKeyspaceHits(), 0L);
        long misses = ObjectUtil.defaultIfNull(vo.getKeyspaceMisses(), 0L);
        if (hits + misses > 0) {
            vo.setHitRate(String.format("%.2f%%", (double) hits / (hits + misses) * 100));
        } else {
            vo.setHitRate("0.00%");
        }
        return vo;
    }

    /**
     * 分页查询缓存键列表。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<CacheKeyVo> getCacheKeyList(CacheKeyBo bo, PageQuery pageQuery) {
        String pattern = StringUtils.isNotBlank(bo.getPattern()) ? bo.getPattern() : "*";
        Collection<String> allKeys = RedisUtils.keys(pattern);

        // 按类型过滤
        List<String> filteredKeys = allKeys.stream()
            .filter(key -> StringUtils.isBlank(bo.getType()) || bo.getType().equalsIgnoreCase(getType(key)))
            .sorted()
            .toList();

        int pageNum = ObjectUtil.defaultIfNull(pageQuery.getPageNum(), PageQuery.DEFAULT_PAGE_NUM);
        int pageSize = ObjectUtil.defaultIfNull(pageQuery.getPageSize(), PageQuery.DEFAULT_PAGE_SIZE);
        if (pageNum <= 0) {
            pageNum = PageQuery.DEFAULT_PAGE_NUM;
        }
        List<CacheKeyVo> rows = filteredKeys.stream()
            .skip((long) (pageNum - 1) * pageSize)
            .limit(pageSize)
            .map(this::buildCacheKeyVo)
            .toList();

        return PageResult.build(rows, filteredKeys.size());
    }

    /**
     * 查询缓存键详情（含值内容）。
     *
     * @param key 键名
     * @return 缓存详情，键不存在时返回 null
     */
    @Override
    public CacheKeyDetailVo getCacheKeyDetail(String key) {
        if (!RedisUtils.hasKey(key)) {
            return null;
        }
        CacheKeyDetailVo vo = new CacheKeyDetailVo();
        vo.setKey(key);
        vo.setType(getType(key));
        vo.setTtl(getTtl(key));
        vo.setMemoryUsage(getMemoryUsage(key));
        vo.setSize(getSize(key));
        vo.setValue(getValue(key));
        return vo;
    }

    /**
     * 删除指定缓存键。
     *
     * @param key 键名
     * @return 是否删除成功
     */
    @Override
    public boolean deleteCacheKey(String key) {
        return RedisUtils.deleteObject(key);
    }

    /**
     * 批量删除缓存键。
     *
     * @param keys 键名列表
     */
    @Override
    public void deleteCacheBatch(List<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            RedisUtils.deleteObject(keys);
        }
    }

    /**
     * 清空当前库的所有缓存。
     */
    @Override
    public void clearAllCache() {
        RedisUtils.getClient().getKeys().flushdb();
    }

    /**
     * 构建缓存键视图对象。
     *
     * @param key 键名
     * @return 缓存键视图对象
     */
    private CacheKeyVo buildCacheKeyVo(String key) {
        CacheKeyVo vo = new CacheKeyVo();
        vo.setKey(key);
        vo.setType(getType(key));
        vo.setTtl(getTtl(key));
        vo.setMemoryUsage(getMemoryUsage(key));
        vo.setSize(getSize(key));
        return vo;
    }

    /**
     * 获取键的数据类型。
     *
     * @param key 键名
     * @return 类型小写名称，键不存在返回 none
     */
    private String getType(String key) {
        RType type = RedisUtils.getClient().getKeys().getType(key);
        return type == null ? "none" : type.name().toLowerCase();
    }

    /**
     * 获取键的剩余存活时间。
     *
     * @param key 键名
     * @return 剩余秒数，-1 永不过期，-2 不存在
     */
    private Long getTtl(String key) {
        long ttl = RedisUtils.getClient().getBucket(key).remainTimeToLive();
        return ttl < 0 ? ttl : ttl / 1000;
    }

    /**
     * 获取键的内存占用（MEMORY USAGE）。
     *
     * @param key 键名
     * @return 字节数，命令不支持时返回 null
     */
    private Long getMemoryUsage(String key) {
        try {
            return RedisUtils.getClient().getScript().eval(
                RScript.Mode.READ_ONLY,
                "return redis.call('MEMORY', 'USAGE', KEYS[1])",
                RScript.ReturnType.LONG,
                List.of(key));
        } catch (Exception e) {
            log.debug("获取缓存键内存占用失败: {}", key, e);
            return null;
        }
    }

    /**
     * 获取键的元素数量。
     *
     * @param key 键名
     * @return 元素数量，无法统计时返回 null
     */
    private Long getSize(String key) {
        RedissonClient client = RedisUtils.getClient();
        RType type = client.getKeys().getType(key);
        if (type == null) {
            return null;
        }
        try {
            return switch (type) {
                case OBJECT -> (long) client.getBucket(key).size();
                case LIST -> (long) client.getList(key).size();
                case SET -> (long) client.getSet(key).size();
                case ZSET -> (long) client.getScoredSortedSet(key).size();
                case MAP -> (long) client.getMap(key).size();
                default -> null;
            };
        } catch (Exception e) {
            log.debug("获取缓存键大小失败: {}", key, e);
            return null;
        }
    }

    /**
     * 获取键的值内容。
     *
     * @param key 键名
     * @return 值对象，无法读取时返回提示文本
     */
    private Object getValue(String key) {
        RedissonClient client = RedisUtils.getClient();
        RType type = client.getKeys().getType(key);
        if (type == null) {
            return null;
        }
        try {
            return switch (type) {
                case OBJECT -> client.getBucket(key).get();
                case LIST -> client.getList(key).readAll();
                case SET -> client.getSet(key).readAll();
                case ZSET -> client.getScoredSortedSet(key).readAll();
                case MAP -> client.getMap(key).readAllMap();
                default -> null;
            };
        } catch (Exception e) {
            // 部分键由其他组件以不同序列化方式写入, 无法用统一编解码器读取
            log.debug("读取缓存键值失败: {}", key, e);
            return "当前值无法解析(序列化方式不兼容)";
        }
    }

    /**
     * 字符串转 Long。
     *
     * @param value 字符串
     * @return 转换结果，失败返回 null
     */
    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转 Integer。
     *
     * @param value 字符串
     * @return 转换结果，失败返回 null
     */
    private Integer parseInt(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字节数格式化为可读文本。
     *
     * @param bytes 字节数
     * @return 可读文本
     */
    private String formatBytes(Long bytes) {
        if (bytes == null) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

}
