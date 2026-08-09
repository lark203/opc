package com.atlantafx.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密解密工具类
 */
public class CryptoUtils {
    // 建议从环境变量或混淆后的字符串中获取，不要直接写死明文
    private static final String ALGORITHM = "AES";
    private static final byte[] KEY = "A1B2C3D4E5F6G7H8A1B2C3D4E5F6G7H8".getBytes(StandardCharsets.UTF_8); // 32位对应AES-256

    /**
     * 加密
     */
    public static String encrypt(String data) {
        try {
            SecretKeySpec spec = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密
     */
    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec spec = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成 256 位 Key 的方法
     * 注意：GCM 模式还需要一个 IV（初始化向量），每次加密都要随机生成
     *
     * @return
     * @throws Exception
     */
    public static byte[] generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey().getEncoded();
    }
}