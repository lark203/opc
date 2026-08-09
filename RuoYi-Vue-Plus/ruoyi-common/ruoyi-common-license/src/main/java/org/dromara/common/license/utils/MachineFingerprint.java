package org.dromara.common.license.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

/**
 * 机器指纹工具。
 *
 * <p>基于本机非回环/非虚拟网卡的 MAC 地址、主机名、操作系统等信息，
 * 通过 SHA-256 生成稳定的十六进制指纹字符串，用于授权文件绑定。
 *
 * @author your-name
 */
public class MachineFingerprint {

    /**
     * 计算当前服务器机器指纹。
     *
     * @return 稳定的十六进制指纹字符串
     */
    public static String getFingerprint() {
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
            InetAddress local = InetAddress.getLocalHost();
            raw.append("host:").append(local.getHostName()).append("|");
        } catch (Exception ignored) {
            // 主机名采集失败不阻断指纹生成
        }
        raw.append("os:").append(System.getProperty("os.name")).append("|");
        raw.append("arch:").append(System.getProperty("os.arch"));

        return sha256Hex(raw.toString());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("生成机器指纹失败", e);
        }
    }

}
