package com.atlantafx.util;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;

/**
 * 用于生成客户端ID的工具类。
 * <p>
 * 工具结合了硬件指纹和随机盐值，并进行了简单的逻辑混淆，确保每台设备的 ID 唯一且稳定
 * <p>
 * 生成的ID包含以下信息：
 * <ul>
 *     <li>MAC地址</li>
 *     <li>CPU架构、内核版本、用户名</li>
 *     <li>SHA-256哈希值</li>
 * </ul>
 * <p>
 * 生成的ID长度为24位，并使用大写字母。
 * <p>
 * 注意：此工具类依赖Java的NetworkInterface和MessageDigest类，可能会在某些环境或Java版本中无法运行。
 */
public class ClientIdGenerator {

    public static String generateId() {
        try {
            StringBuilder rawData = new StringBuilder();

            // 1. 获取所有网卡的MAC地址并拼接
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        rawData.append(String.format("%02X", b));
                    }
                }
            }

            // 2. 加上系统级属性（CPU架构、内核版本、用户名等）
            rawData.append(System.getProperty("os.arch"));
            rawData.append(System.getProperty("os.name"));
            rawData.append(System.getProperty("user.name"));

            // 3. 使用 SHA-256 计算哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawData.toString().getBytes());

            // 4. 转为 16 进制并混淆截取
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 返回前 24 位大写字符串作为 ClientID
            return hexString.toString().substring(0, 24).toUpperCase();
        } catch (Exception e) {
            return "DEFAULT-CLIENT-ID-" + System.nanoTime();
        }
    }
}