package com.atlantafx.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 密钥生成工具。
 * <p>
 * RSA 密钥统一输出为「裸 Base64」单行字符串（无 PEM 头、不换行）：
 * 公钥 X.509 SubjectPublicKeyInfo、私钥 PKCS#8 PrivateKeyInfo。
 * RuoYi-Vue-Plus 的 LicenseVerifier 与 EncryptUtils 均按此格式直接 Base64 解码，
 * 带 PEM 头会导致解码失败、应用启动报错。
 */
public final class KeyPairUtils {

    /**
     * RuoYi EncryptUtils.MIN_RSA_KEY_SIZE，低于该位数后端会拒绝启动
     */
    public static final int MIN_RSA_KEY_SIZE = 1024;

    /**
     * 大写字母（去掉易混淆的 I O）
     */
    public static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 小写字母（去掉易混淆的 l o）
     */
    public static final String LOWER = "abcdefghijkmnpqrstuvwxyz";

    /**
     * 数字（去掉易混淆的 0 1）
     */
    public static final String DIGITS = "23456789";

    /**
     * 符号
     */
    public static final String SYMBOLS = "!@#$%";

    /**
     * RuoYi 用户导入时生成随机初始密码所用的字符集
     * 见 SysUserImportListener
     */
    public static final String RUOYI_ALPHABET = UPPER + LOWER + DIGITS + SYMBOLS;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private KeyPairUtils() {
    }

    /**
     * RSA 密钥对（裸 Base64）
     *
     * @param publicKey  X.509 公钥
     * @param privateKey PKCS#8 私钥
     */
    public record RsaKeys(String publicKey, String privateKey) {
    }

    /**
     * 生成 RSA 密钥对
     *
     * @param bits 密钥位数，如 1024 / 2048 / 4096
     * @return 裸 Base64 的公私钥
     */
    public static RsaKeys generateRsa(int bits) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(bits, RANDOM);
            KeyPair keyPair = generator.generateKeyPair();
            return new RsaKeys(ENCODER.encodeToString(keyPair.getPublic().getEncoded()),
                    ENCODER.encodeToString(keyPair.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new IllegalStateException("生成 RSA 密钥对失败: " + e.getMessage(), e);
        }
    }

    /**
     * 校验 RSA 公钥格式并返回位数，逻辑对应 EncryptUtils.validateRsaPublicKey
     *
     * @param publicKey 裸 Base64 公钥
     * @return 密钥位数
     */
    public static int validateRsaPublicKey(String publicKey) {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalArgumentException("请输入公钥");
        }
        try {
            RSAKey key = (RSAKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(DECODER.decode(publicKey.trim())));
            return checkSize(key);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA 公钥格式错误，需为 X.509 裸 Base64（无 PEM 头）", e);
        }
    }

    /**
     * 校验 RSA 私钥格式并返回位数，逻辑对应 EncryptUtils.validateRsaPrivateKey
     *
     * @param privateKey 裸 Base64 私钥
     * @return 密钥位数
     */
    public static int validateRsaPrivateKey(String privateKey) {
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalArgumentException("请输入私钥");
        }
        try {
            RSAKey key = (RSAKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(DECODER.decode(privateKey.trim())));
            return checkSize(key);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA 私钥格式错误，需为 PKCS#8 裸 Base64（无 PEM 头）", e);
        }
    }

    private static int checkSize(RSAKey key) {
        int bits = key.getModulus().bitLength();
        if (bits < MIN_RSA_KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度 " + bits + " 位，低于后端要求的 " + MIN_RSA_KEY_SIZE + " 位");
        }
        return bits;
    }

    /**
     * 生成指定长度的随机字符串
     * <p>
     * 后端 EncryptUtils 对 AES / SM4 秘钥是按「字符个数」校验的，
     * 因此这里只从 ASCII 字符集中取值，保证字符数与字节数一致。
     *
     * @param length   长度
     * @param alphabet 字符集
     * @return 随机字符串
     */
    public static String randomSecret(int length, String alphabet) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一种字符类型");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
