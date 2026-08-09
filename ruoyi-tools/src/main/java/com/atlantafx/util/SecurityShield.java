package com.atlantafx.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.system.SystemUtil;
import com.atlantafx.core.config.ConfigStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 综合安全护盾 - 集成设备指纹、加密、设备绑定
 */
public class SecurityShield {
    private static final Logger log = LoggerFactory.getLogger(SecurityShield.class);

    private static final String SALT_CONFIG_KEY = "security_salt";

    /**
     * 获取盐值：优先从配置文件读取，不存在则自动生成并持久化
     */
    private static String getSalt() {
        String salt = ConfigStore.get(SALT_CONFIG_KEY);
        if (salt != null && !salt.isBlank()) {
            return salt;
        }

        // 首次运行，自动生成随机盐值并保存到配置
        salt = UUID.randomUUID().toString().replace("-", "");
        ConfigStore.set(SALT_CONFIG_KEY, salt);
        ConfigStore.save(); // 持久化
        log.info("已生成并保存新的安全盐值");
        return salt;
    }

    /**
     * 1. 生成 32 位无横杠的唯一 ID (类似 UUID v7 思想)
     */
    public static String generateUniqueId() {
        return CompactUuidV7.generate32(); // Hutool 提供的 32 位无横杠 UUID
    }

    /**
     * 2. 获取当前设备的“指纹锁” (基于硬件信息)
     */
    private static AES getDeviceLock() {
        try {
            // 使用 DeviceIdUtil 获取可靠的设备指纹（已排除回环和虚拟网卡）
            String machineId = DeviceIdUtil.getMachineId();
            String raw = machineId + getSalt();

            // 将硬件信息哈希成标准 16 位 AES 密钥
            byte[] key = SecureUtil.md5(raw).substring(0, 16).getBytes();
            return SecureUtil.aes(key);
        } catch (Exception e) {
            // 备用方案：如果获取不到硬件，用用户名保底
            return SecureUtil.aes((SystemUtil.get("user.name") + "FIXED_SALT").substring(0, 16).getBytes());
        }
    }

    /**
     * 3. 【绑定】用设备指纹加密你的“业务密钥”
     *
     * @param realWorkKey 你真正用来加密数据库或配置的密钥
     * @return 加密后的字符串（可以安全地存在本地或发给后端）
     */
    public static String bindKeyToDevice(String realWorkKey) {
        return getDeviceLock().encryptBase64(realWorkKey);
    }

    /**
     * 4. 【解绑】用当前设备指纹尝试还原“业务密钥”
     *
     * @param encryptedKey 本地存的那个密文密钥
     * @return 还原后的明文密钥（如果换了机器，这里会报错或返回错误数据）
     */
    public static String unbindKeyFromDevice(String encryptedKey) {
        try {
            return getDeviceLock().decryptStr(encryptedKey);
        } catch (Exception e) {
            return null; // 解密失败说明不是同一台设备
        }
    }

    /**
     * 5. 常用的标准 AES 加密（用于加密普通业务数据）
     */
    public static String quickEncrypt(String key, String data) {
        return SecureUtil.aes(key.substring(0, 16).getBytes()).encryptBase64(data);
    }

    /**
     * 6. 常用的标准 AES 解密（用于解密普通业务数据）
     */
    public static String quickDecrypt(String key, String data) {
        return SecureUtil.aes(key.substring(0, 16).getBytes()).decryptStr(data);
    }
}