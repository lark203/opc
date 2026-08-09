package com.atlantafx;

import com.atlantafx.util.SingleInstanceService;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // 1. 物理检查：单实例
        if (!SingleInstanceService.checkAndLock()) {
            // 这里不能用 JavaFX Alert，因为 Toolkit 还没启动
            // 建议使用原生 OS 对话框或简单退出
            // 尝试使用系统原生样式的弹窗提示用户
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JOptionPane.showMessageDialog(
                        null,
                        "程序已经在运行中，请勿重复启动。",
                        "提示",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (Exception e) {
                // 如果系统不支持 AWT 弹窗，则降级打印控制台
                System.err.println("程序已经在运行中，请勿重复启动。");
            }
            System.exit(0);
        }
        AppLauncher.main(args);
    }
}
