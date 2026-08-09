package org.dromara.common.license.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.dromara.common.license.config.properties.LicenseProperties;
import org.dromara.common.license.utils.MachineFingerprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 授权校验器。
 *
 * <p>负责加载验签公钥、读取并校验授权文件、维护内存中的授权状态。
 * 授权文件格式为单行文本：{@code base64url(payloadJson)::base64url(RSA-SHA256签名)}。
 *
 * @author your-name
 */
@Component
public class LicenseVerifier {

    private static final Logger log = LoggerFactory.getLogger(LicenseVerifier.class);
    private static final String LICENSE_SEPARATOR = "::";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LicenseProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PublicKey publicKey;
    private volatile LicenseState state = new LicenseState();

    public LicenseVerifier(LicenseProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化时加载公钥并做一次校验。
     */
    @PostConstruct
    public void init() {
        loadPublicKey();
        reload();
    }

    /**
     * 从配置的公钥资源路径加载 RSA 公钥。
     *
     * <p>支持 {@code classpath:} 与 {@code file:} 两种前缀，缺省按 classpath 处理。
     */
    private void loadPublicKey() {
        try (InputStream in = openResource(properties.getPublicKeyPath())) {
            String keyBase64 = StreamUtils.copyToString(in, StandardCharsets.UTF_8).trim();
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("加载授权公钥失败", e);
        }
    }

    /**
     * 按前缀解析资源位置，返回可读的输入流。
     *
     * @param location 资源位置，可带 {@code classpath:} / {@code file:} 前缀
     * @return 资源输入流
     * @throws Exception 资源不存在或打开失败时抛出
     */
    private InputStream openResource(String location) throws Exception {
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring("classpath:".length())).getInputStream();
        }
        if (location.startsWith("file:")) {
            return new FileInputStream(location.substring("file:".length()));
        }
        // 缺省按 classpath 资源处理
        return new ClassPathResource(location).getInputStream();
    }

    /**
     * 重新读取并校验授权文件，刷新内存状态。
     */
    public void reload() {
        LicenseState next = new LicenseState();
        next.setLastChecked(LocalDateTime.now());
        try {
            Path path = Paths.get(properties.getLicensePath());
            if (!Files.exists(path)) {
                next.setValid(false);
                next.setMessage("未找到授权文件：" + properties.getLicensePath());
                this.state = next;
                return;
            }
            byte[] bytes = Files.readAllBytes(path);
            this.state = validate(bytes);
        } catch (Exception e) {
            next.setValid(false);
            next.setMessage(e.getMessage());
            log.error("授权校验失败：{}", e.getMessage());
            this.state = next;
        }
    }

    /**
     * 校验授权文件内容（不读取磁盘文件、不修改内存状态）。
     *
     * <p>供上传接口在落盘前预校验使用：校验通过的字节才可写回授权文件，
     * 从而避免用无效/伪造的 .lic 覆盖当前有效的授权，导致管理员被锁死。
     *
     * @param contentBytes 授权文件原始字节
     * @return 校验结果状态
     */
    public LicenseState validate(byte[] contentBytes) {
        LicenseState next = new LicenseState();
        next.setLastChecked(LocalDateTime.now());
        try {
            String content = new String(contentBytes, StandardCharsets.UTF_8).trim();
            int idx = content.indexOf(LICENSE_SEPARATOR);
            if (idx < 0) {
                throw new IllegalStateException("授权文件格式错误");
            }
            byte[] payload = Base64.getUrlDecoder().decode(content.substring(0, idx));
            byte[] signature = Base64.getUrlDecoder().decode(content.substring(idx + LICENSE_SEPARATOR.length()));

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(payload);
            if (!verifier.verify(signature)) {
                throw new IllegalStateException("授权文件签名校验失败");
            }

            JsonNode node = objectMapper.readTree(payload);
            String fingerprint = node.path("fingerprint").asText(null);
            String expireAt = node.path("expireAt").asText(null);
            String issuedAt = node.path("issuedAt").asText(null);
            String version = node.path("version").asText(null);
            String type = node.path("type").asText(null);

            if (expireAt == null) {
                throw new IllegalStateException("授权文件缺少过期时间");
            }
            LocalDateTime expire = LocalDateTime.parse(expireAt, ISO_FMT);
            if (expire.isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("授权已过期，有效期至 " + expireAt);
            }

            if (properties.isBindFingerprint()) {
                String localFingerprint = MachineFingerprint.getFingerprint();
                if (!localFingerprint.equals(fingerprint)) {
                    throw new IllegalStateException("机器指纹不匹配，授权文件不可用于当前服务器");
                }
            }

            next.setValid(true);
            next.setFingerprint(fingerprint);
            next.setExpireAt(expireAt);
            next.setIssuedAt(issuedAt);
            next.setVersion(version);
            next.setType(type);
            next.setMessage("授权有效");
        } catch (Exception e) {
            next.setValid(false);
            next.setMessage(e.getMessage());
            log.error("授权校验失败：{}", e.getMessage());
        }
        return next;
    }

    /**
     * 获取当前授权状态。
     *
     * @return 授权状态
     */
    public LicenseState getState() {
        return state;
    }

}
