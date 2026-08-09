package com.atlantafx.util;

import com.atlantafx.AppContext;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件选择与文本读写工具
 */
public final class FileChooserUtils {

    private FileChooserUtils() {
    }

    /**
     * 打开文件选择框
     *
     * @param title       标题
     * @param initialFile 初始定位的文件，可为 null
     * @param filterName  过滤器名称
     * @param extensions  过滤器扩展名，如 *.key
     * @return 选中的文件，取消时返回 null
     */
    public static File chooseOpen(String title, File initialFile, String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        applyInitial(chooser, initialFile);
        addFilters(chooser, filterName, extensions);
        return chooser.showOpenDialog(AppContext.getPrimaryStage());
    }

    /**
     * 打开文件保存框
     *
     * @param title       标题
     * @param defaultName 默认文件名
     * @param filterName  过滤器名称
     * @param extensions  过滤器扩展名
     * @return 选中的文件，取消时返回 null
     */
    public static File chooseSave(String title, String defaultName, String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(defaultName);
        addFilters(chooser, filterName, extensions);
        return chooser.showSaveDialog(AppContext.getPrimaryStage());
    }

    /**
     * 读取文本文件内容（UTF-8）
     *
     * @param file 文件
     * @return 文件内容
     */
    public static String readText(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("读取文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 写入文本文件（UTF-8）
     *
     * @param file    文件
     * @param content 内容
     */
    public static void writeText(File file, String content) {
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("写入文件失败: " + file.getAbsolutePath(), e);
        }
    }

    private static void applyInitial(FileChooser chooser, File initialFile) {
        if (initialFile == null) {
            return;
        }
        File dir = initialFile.isDirectory() ? initialFile : initialFile.getParentFile();
        if (dir != null && dir.isDirectory()) {
            chooser.setInitialDirectory(dir);
        }
        if (initialFile.isFile()) {
            chooser.setInitialFileName(initialFile.getName());
        }
    }

    private static void addFilters(FileChooser chooser, String filterName, String... extensions) {
        if (extensions != null && extensions.length > 0) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        }
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件", "*.*"));
    }
}
