### 问题1：超时退出登录配置

超时时间配置在 sys_client 表中（数据库初始化数据）：

字段 值 含义 active_timeout 1800秒（30分钟） 活跃超时时间，用户操作后会自动续期 timeout 604800秒（7天） 固定超时时间，无论是否活跃都会过期

配置传递路径：

1. 数据库 ： ry_vue.sql 中 sys_client 表的初始数据
2. 构建参数 ： IAuthStrategy.java 的 buildLoginParameter() 方法
3. 传递给 Sa-Token ：
   动态续期配置 ： common-satoken.yml 中启用了 dynamic-active-timeout: true ，表示用户操作后会自动续期。

### 问题2：Sa-Token 是否使用 Redis 缓存

是的！

PlusSaTokenDao.java 实现了 SaTokenDaoBySessionFollowObject 接口，采用 Caffeine + Redis 多级缓存 ：

```
public class PlusSaTokenDao implements 
SaTokenDaoBySessionFollowObject {
    private static final Cache<String, Object> CAFFEINE = Caffeine.
    newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .maximumSize(1000)
        .build();
    
    public String get(String key) {
        return getCacheValue(key);  // 先查Caffeine，再查Redis
    }
    
    public void set(String key, String value, long timeout) {
        writeValue(key, value, timeout);  // 写入Redis，清除Caffeine
    }
}
```

### 关键发现

虽然 Sa-Token 使用 Redis 存储数据，但项目使用的是 StpLogicJwtForSimple （JWT 简单模式） ，这种模式下：

操作 行为 Token 验证 只验证 JWT 签名，不查询 Redis kickout() 删除 Redis 中的 Session，但 不影响已签发的 JWT Token 其他终端
仍然持有有效的 JWT Token，可以正常请求

这就是为什么 StpUtil.kickout() 对其他终端无效的根本原因！我们之前实现的黑名单机制是正确的解决方案。