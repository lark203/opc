package com.atlantafx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public class SingleInstanceService {
    private static final Logger log = LoggerFactory.getLogger(SingleInstanceService.class);
    private static FileLock lock;
    private static FileChannel channel;
    // 定义锁文件存放路径（用户目录下）
    private static final String LOCK_FILE_NAME = ".atlantafx_app.lock";

    public static boolean checkAndLock() {
        try {
            Path lockPath = Path.of("data", "locks", LOCK_FILE_NAME).toAbsolutePath();
            File file = lockPath.toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 如果文件不存在则创建
            channel = new RandomAccessFile(file, "rw").getChannel();
            // 尝试获取排他锁（非阻塞）
            lock = channel.tryLock();

            if (lock != null) {
                // 成功获取锁，注册退出钩子释放资源
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (lock != null) lock.release();
                        channel.close();
                    } catch (Exception ignored) {
                        log.error("锁释放失败");
                    }
                }));
                return true;
            }
        } catch (Exception e) {
            log.error("锁获取失败", e);
        }
        return false; // 获取锁失败，说明已有实例运行
    }
}