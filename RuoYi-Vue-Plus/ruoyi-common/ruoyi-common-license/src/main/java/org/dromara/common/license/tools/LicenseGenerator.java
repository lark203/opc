package org.dromara.common.license.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 授权签发工具（厂商侧使用）。
 *
 * <p>用法：
 * <pre>
 *   # 生成 RSA 公私钥对（公钥交给本模块资源，私钥自行保管）
 *   java -cp ... org.dromara.common.license.tools.LicenseGenerator genkey
 *
 *   # 签发授权文件
 *   java -cp ... org.dromara.common.license.tools.LicenseGenerator sign \
 *       license_private.key &lt;机器指纹&gt; "2026-12-31 23:59:59" license.lic [版本] [类型]
 * </pre>
 *
 * @author your-name
 */
public class LicenseGenerator {

    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        switch (args[0]) {
            case "genkey" -> genkey();
            case "sign" -> sign(args);
            default -> printUsage();
        }
    }

    private static void genkey() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        String pub = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
        String pri = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
        Files.writeString(new File("license_public.key").toPath(), pub);
        Files.writeString(new File("license_private.key").toPath(), pri);
        System.out.println("公钥已写入 license_public.key");
        System.out.println("私钥已写入 license_private.key（请妥善保管，切勿泄露）");
        System.out.println("----- 公钥 -----");
        System.out.println(pub);
    }

    private static void sign(String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("用法: sign <私钥文件> <机器指纹> <过期时间(yyyy-MM-dd HH:mm:ss)> <输出文件> [版本] [类型]");
            return;
        }
        String privateKeyFile = args[1];
        String fingerprint = args[2];
        String expireAtInput = args[3];
        String outputFile = args[4];
        String version = args.length > 5 ? args[5] : "1.0.0";
        String type = args.length > 6 ? args[6] : "trial";

        LocalDateTime expire = LocalDateTime.parse(expireAtInput, INPUT_FMT);
        String expireAt = expire.format(ISO_FMT);
        String issuedAt = LocalDateTime.now().format(ISO_FMT);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("fingerprint", fingerprint);
        node.put("issuedAt", issuedAt);
        node.put("expireAt", expireAt);
        node.put("version", version);
        node.put("type", type);
        String payloadJson = mapper.writeValueAsString(node);

        byte[] priBytes = Base64.getDecoder().decode(Files.readString(new File(privateKeyFile).toPath()).trim());
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(priBytes));

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(payloadJson.getBytes(StandardCharsets.UTF_8));
        byte[] signature = sig.sign();

        String content = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
            + "::" + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        Files.writeString(new File(outputFile).toPath(), content);
        System.out.println("授权文件已生成：" + outputFile + "，有效期至 " + expireAt);
    }

    private static void printUsage() {
        System.out.println("用法:");
        System.out.println("  genkey                                     生成 RSA 公私钥对");
        System.out.println("  sign <私钥文件> <机器指纹> <过期时间> <输出文件> [版本] [类型]");
    }

}
