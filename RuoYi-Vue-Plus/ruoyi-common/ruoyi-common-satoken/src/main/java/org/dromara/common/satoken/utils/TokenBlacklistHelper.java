package org.dromara.common.satoken.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import org.dromara.common.redis.utils.RedisUtils;

import java.time.Duration;

public class TokenBlacklistHelper {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "token:user_blacklist:";

    public static void addToBlacklist(String token) {
        if (StrUtil.isNotEmpty(token)) {
            long timeout = StpUtil.getTokenTimeout();
            if (timeout > 0) {
                String blacklistKey = BLACKLIST_PREFIX + token;
                RedisUtils.setCacheObject(blacklistKey, true, Duration.ofSeconds(timeout));
            }
        }
    }

    public static void kickoutUser(Long userId) {
        String userKey = USER_BLACKLIST_PREFIX + userId;
        RedisUtils.setCacheObject(userKey, true, Duration.ofSeconds(3600));
    }

    public static void clearKickoutUser(Long userId) {
        String userKey = USER_BLACKLIST_PREFIX + userId;
        RedisUtils.deleteObject(userKey);
    }

    public static boolean isUserKickedOut(Long userId) {
        String userKey = USER_BLACKLIST_PREFIX + userId;
        return Boolean.TRUE.equals(RedisUtils.getCacheObject(userKey));
    }

    public static void removeFromBlacklist(String token) {
        if (StrUtil.isNotEmpty(token)) {
            String blacklistKey = BLACKLIST_PREFIX + token;
            RedisUtils.deleteObject(blacklistKey);
        }
    }
}