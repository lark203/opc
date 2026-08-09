package com.atlantafx.features.virtuallist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 虚拟列表演示用的样例数据模型（普通 POJO，不依赖 JavaFX 属性）。
 * 提供静态方法快速生成十万级测试数据。
 */
public final class DemoRecord {

    private static final String[] SURNAMES = {"王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
            "徐", "孙", "马", "朱", "胡", "林", "郭", "何", "高", "罗"};
    private static final String[] GIVEN = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "文博", "雨欣"};
    private static final String[] DEPARTMENTS = {"研发部", "产品部", "设计部", "市场部", "销售部", "财务部",
            "人力资源", "运维部", "质量保障", "客户成功"};
    private static final String[] STATUS = {"在职", "试用", "休假", "离职"};

    /** 数据总量（供演示面板展示） */
    public final int seq;
    public final String id;
    public final String name;
    public final String department;
    public final String status;
    public final int score;
    public final double amount;
    public final String createdAt;

    public DemoRecord(int seq, String id, String name, String department,
                      String status, int score, double amount, String createdAt) {
        this.seq = seq;
        this.id = id;
        this.name = name;
        this.department = department;
        this.status = status;
        this.score = score;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    /**
     * 生成指定数量的演示数据。使用固定种子的伪随机，保证每次生成结果一致，便于对比。
     *
     * @param count 数据条数（支持十万级、百万级）
     * @return 数据列表
     */
    public static List<DemoRecord> generate(int count) {
        List<DemoRecord> list = new ArrayList<>(count);
        Random rnd = new Random(20260805L);
        for (int i = 0; i < count; i++) {
            int seq = i + 1;
            String id = "EMP" + String.format("%06d", seq);
            String name = SURNAMES[rnd.nextInt(SURNAMES.length)] + GIVEN[rnd.nextInt(GIVEN.length)];
            String department = DEPARTMENTS[rnd.nextInt(DEPARTMENTS.length)];
            // 让"在职"占比更高，更贴近真实
            String status;
            int s = rnd.nextInt(100);
            if (s < 68) status = "在职";
            else if (s < 82) status = "试用";
            else if (s < 92) status = "休假";
            else status = "离职";
            int score = 60 + rnd.nextInt(41);                 // 60 ~ 100
            double amount = 5000 + rnd.nextDouble() * 45000;  // 5k ~ 50k
            int year = 2018 + rnd.nextInt(8);                 // 2018 ~ 2025
            int month = 1 + rnd.nextInt(12);
            int day = 1 + rnd.nextInt(28);
            String createdAt = String.format("%d-%02d-%02d", year, month, day);
            list.add(new DemoRecord(seq, id, name, department, status, score, amount, createdAt));
        }
        return list;
    }

    /** 状态对应的文本颜色（在深浅主题下都清晰） */
    public static String statusColor(String status) {
        return switch (status) {
            case "在职" -> "#3fb950";
            case "试用" -> "#d29922";
            case "休假" -> "#58a6ff";
            case "离职" -> "#f85149";
            default -> "-color-fg-default";
        };
    }
}
