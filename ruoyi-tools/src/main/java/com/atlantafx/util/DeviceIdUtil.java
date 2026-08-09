package com.atlantafx.util;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 混淆后的设备识别工具
 * 目的：让反编译者难以一眼看出 ID 是如何生成的
 */
public class DeviceIdUtil {

    // 使用看似无关的字符串作为盐值
    private static final String S = "A1_B2_C3_99";

    public static String getMachineId() {
        try {
            // 1. 混合获取：系统变量 + 硬件特征
            // 不直接使用字符串，而是通过位运算或拼接
            String p1 = System.getProperty(new String(new byte[]{111, 115, 46, 97, 114, 99, 104})); // "os.arch"
            String p2 = System.getProperty(new String(new byte[]{117, 115, 101, 114, 46, 110, 97, 109, 101})); // "user.name"

            StringBuilder b = new StringBuilder();
            var ens = NetworkInterface.getNetworkInterfaces();

            while (ens.hasMoreElements()) {
                var n = ens.nextElement();
                // 排除虚拟网卡和回环地址
                if (n.isLoopback() || n.isVirtual()) continue;

                byte[] h = n.getHardwareAddress();
                if (h != null && h.length > 5) {
                    // 对 MAC 地址进行简单的位翻转混淆
                    for (int i = 0; i < h.length; i++) {
                        b.append(h[i] ^ 0x55);
                    }
                    break; // 只取第一个有效物理网卡
                }
            }

            // 2. 引入逻辑干扰
            long t = Runtime.getRuntime().availableProcessors() * 31L;
            String raw = b.toString() + p1 + S + p2 + t;

            // 3. 多重哈希
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] h1 = d.digest(raw.getBytes(StandardCharsets.UTF_8));

            // 再次混淆字节顺序
            for (int i = 0; i < h1.length / 2; i++) {
                byte tmp = h1[i];
                h1[i] = h1[h1.length - 1 - i];
                h1[h1.length - 1 - i] = tmp;
            }

            return Base64.getEncoder().encodeToString(h1)
                    .replaceAll("[^A-Z0-9]", "") // 只保留大写字母和数字
                    .substring(0, 16);           // 截取前16位作为最终机器码

        } catch (Exception e) {
            // 失败时返回一个基于当前环境的伪随机码，而不是固定值
            return "ERR" + System.getProperty("user.home").hashCode();
        }
    }

    public static void main(String[] args) {
        System.out.println(getMachineId());
    }
}