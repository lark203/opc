package com.atlantafx.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

/**
 * RuoYi-Vue-Plus 授权文件（license）签发与校验工具。
 * <p>
 * 算法与 keygen/SignLicense.java、ruoyi-common-license 的 LicenseVerifier 完全一致：
 * <pre>
 * 内容格式 : base64url(payloadJson) :: base64url(RSA-SHA256 签名)
 * payload  : {"fingerprint":"..","issuedAt":"..","expireAt":"..","version":"..","type":".."}
 * 签名对象 : payload 的原始 UTF-8 字节（不是 base64 之后的串）
 * 密钥编码 : 标准 Base64（非 URL-safe），无 PEM 头，私钥 PKCS#8、公钥 X.509
 * </pre>
 */
public final class LicenseUtils {

    /**
     * 授权内容中 payload 与签名的分隔符
     */
    public static final String SEPARATOR = "::";

    /**
     * 界面输入的过期时间格式
     */
    public static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * payload 内部时间格式，必须与服务端 LicenseVerifier 保持一致
     */
    public static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Base64.Decoder STD_DECODER = Base64.getDecoder();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private LicenseUtils() {
    }

    /**
     * 授权文件解析结果
     *
     * @param fingerprint    机器指纹
     * @param issuedAt       签发时间
     * @param expireAt       过期时间
     * @param version        版本
     * @param type           类型
     * @param payload        payload 明文 JSON
     * @param signatureValid 签名是否有效
     * @param expired        是否已过期
     */
    public record LicenseInfo(String fingerprint, String issuedAt, String expireAt, String version,
                              String type, String payload, boolean signatureValid, boolean expired) {
    }

    /**
     * 签发授权文件内容
     *
     * @param privateKeyBase64 PKCS#8 标准 Base64 私钥（无 PEM 头）
     * @param fingerprint      目标机器指纹
     * @param expire           过期时间
     * @param version          版本，如 1.0.0
     * @param type             类型，如 trial / dev / official
     * @return 授权文件内容（单行）
     */
    public static String sign(String privateKeyBase64, String fingerprint, LocalDateTime expire,
                              String version, String type) {
        try {
            String payload = "{\"fingerprint\":\"" + fingerprint
                    + "\",\"issuedAt\":\"" + LocalDateTime.now().format(ISO_FMT)
                    + "\",\"expireAt\":\"" + expire.format(ISO_FMT)
                    + "\",\"version\":\"" + version
                    + "\",\"type\":\"" + type + "\"}";

            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(STD_DECODER.decode(privateKeyBase64.trim())));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(payloadBytes);

            return URL_ENCODER.encodeToString(payloadBytes) + SEPARATOR + URL_ENCODER.encodeToString(signature.sign());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("私钥格式错误，需为 PKCS#8 标准 Base64（无 PEM 头）", e);
        } catch (Exception e) {
            throw new IllegalStateException("签发授权文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 校验授权文件内容
     *
     * @param content         授权文件内容
     * @param publicKeyBase64 X.509 标准 Base64 公钥（无 PEM 头）
     * @return 解析结果
     */
    public static LicenseInfo verify(String content, String publicKeyBase64) {
        String trimmed = content.trim();
        int idx = trimmed.indexOf(SEPARATOR);
        if (idx < 0) {
            throw new IllegalArgumentException("授权文件格式错误：缺少 " + SEPARATOR + " 分隔符");
        }
        String payload;
        byte[] signatureBytes;
        try {
            payload = new String(URL_DECODER.decode(trimmed.substring(0, idx)), StandardCharsets.UTF_8);
            signatureBytes = URL_DECODER.decode(trimmed.substring(idx + SEPARATOR.length()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("授权文件内容不是合法的 URL-safe Base64", e);
        }

        boolean valid;
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(STD_DECODER.decode(publicKeyBase64.trim())));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            valid = signature.verify(signatureBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("公钥格式错误，需为 X.509 标准 Base64（无 PEM 头）", e);
        } catch (Exception e) {
            throw new IllegalStateException("验签失败: " + e.getMessage(), e);
        }

        String expireAt = field(payload, "expireAt");
        boolean expired = true;
        try {
            expired = LocalDateTime.parse(expireAt, ISO_FMT).isBefore(LocalDateTime.now());
        } catch (Exception ignored) {
            // 过期时间缺失或不可解析时按已过期处理，与服务端一致
        }

        return new LicenseInfo(field(payload, "fingerprint"), field(payload, "issuedAt"), expireAt,
                field(payload, "version"), field(payload, "type"), payload, valid, expired);
    }

    /**
     * 从 payload JSON 中取出字符串字段，逻辑与 keygen/VerifyLicense 保持一致
     */
    private static String field(String json, String key) {
        String tag = "\"" + key + "\":";
        int i = json.indexOf(tag);
        if (i < 0) {
            return null;
        }
        int s = json.indexOf('"', i + tag.length());
        if (s < 0) {
            return null;
        }
        int e = json.indexOf('"', s + 1);
        return e < 0 ? null : json.substring(s + 1, e);
    }

    /**
     * 计算本机机器指纹，算法与服务端 MachineFingerprint.getFingerprint() 完全一致
     *
     * @return 64 位小写十六进制指纹
     */
    public static String machineFingerprint() {
        Set<String> macs = new TreeSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isLoopback() || nif.isVirtual() || !nif.isUp()) {
                    continue;
                }
                byte[] hardwareAddress = nif.getHardwareAddress();
                if (hardwareAddress != null && hardwareAddress.length > 0) {
                    macs.add(bytesToHex(hardwareAddress));
                }
            }
        } catch (Exception ignored) {
            // 网卡信息采集失败不阻断指纹生成
        }

        StringBuilder raw = new StringBuilder();
        for (String mac : macs) {
            raw.append("mac:").append(mac).append("|");
        }
        try {
            raw.append("host:").append(InetAddress.getLocalHost().getHostName()).append("|");
        } catch (Exception ignored) {
            // 主机名采集失败不阻断指纹生成
        }
        raw.append("os:").append(System.getProperty("os.name")).append("|");
        raw.append("arch:").append(System.getProperty("os.arch"));

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("生成机器指纹失败", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
