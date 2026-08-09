package com.atlantafx.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public class CompactUuidV7 {
    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成 32 位无横杠的 UUID v7 字符串
     * 格式：48位时间戳 + 4位版本号(7) + 12位序列号/随机数 + 2位变体号 + 62位随机数
     */
    public static String generate32() {
        byte[] value = new byte[16];
        random.nextBytes(value);

        // 1. 注入当前毫秒时间戳 (占用前 48 bits)
        long timestamp = System.currentTimeMillis();
        value[0] = (byte) ((timestamp >> 40) & 0xFF);
        value[1] = (byte) ((timestamp >> 32) & 0xFF);
        value[2] = (byte) ((timestamp >> 24) & 0xFF);
        value[3] = (byte) ((timestamp >> 16) & 0xFF);
        value[4] = (byte) ((timestamp >> 8) & 0xFF);
        value[5] = (byte) (timestamp & 0xFF);

        // 2. 设置版本号为 7 (将第7个字节的高4位设为 0111)
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);

        // 3. 设置变体号 (将第9个字节的高2位设为 10)
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        // 4. 使用 HexFormat 直接转为 32 位字符串 (JDK 17+)
        return HexFormat.of().formatHex(value);
    }

    public static void main(String[] args) {
        // 输出示例：018e112d76f87b8ea29864275073f1d2
        System.out.println("生成的32位有序ID: " + generate32());
        System.out.println("生成的32位有序ID: " + generate32());
        System.out.println("生成的32位有序ID: " + generate32());
        System.out.println("生成的32位有序ID: " + generate32());
        System.out.println("生成的32位有序ID: " + generate32());
    }
}
