package com.atlantafx.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * 应用级 ClassGraph 扫描结果缓存。
 * <p>
 * 原本 DatabaseManager（扫 {@code @Table}）与 ViewFactory（扫 {@code @Page}）各自对 {@code com.atlantafx}
 * 整包做全量扫描，启动期会扫描两次。这里合并为一次扫描并缓存 {@link ScanResult}，供多处复用，降低冷启动开销。
 * <p>
 * 注意：返回的 {@link ScanResult} 为进程级单例，<b>禁止</b>对其调用 {@code close()}，否则会影响后续使用者。
 */
public final class AppClassScanner {

    private static final ScanResult SCAN_RESULT = new ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .acceptPackages("com.atlantafx")
            .scan();

    private AppClassScanner() {
    }

    public static ScanResult get() {
        return SCAN_RESULT;
    }
}
