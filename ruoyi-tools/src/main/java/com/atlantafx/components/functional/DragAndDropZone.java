package com.atlantafx.components.functional;

import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXFontIcon;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignU;

import java.io.File;
import java.util.function.Consumer;

/**
 * 通用拖拽区域组件
 * <p>
 * 支持拖拽文件或目录，可配置回调函数处理不同场景：
 * - Git 项目扫描导入
 * - 文件上传
 * - 数据导入
 * <p>
 * 使用示例：
 * <pre>
 * // Git 项目扫描导入
 * DragAndDropZone gitDropZone = new DragAndDropZone()
 *     .title("拖拽文件夹到此处扫描导入")
 *     .description("支持递归扫描 .git 目录")
 *     .icon(MaterialDesignF.FOLDER_DOWNLOAD)
 *     .directoryOnly(true)
 *     .onDirectoryDrop(dirPath -> ProjectScanService.scanAndImportAsync(dirPath, 3, null, null));
 *
 * // 文件上传
 * DragAndDropZone fileDropZone = new DragAndDropZone()
 *     .title("拖拽文件到此处上传")
 *     .icon(MaterialDesignU.UPLOAD)
 *     .directoryOnly(false)
 *     .onFileDrop(file -> uploadFile(file));
 * </pre>
 */
public class DragAndDropZone extends FXVBox {

    private String titleText = "拖拽文件到此处";
    private String descriptionText = "支持文件和文件夹";
    private Ikon iconCode = MaterialDesignU.UPLOAD;
    private boolean directoryOnly = false;
    private boolean recursiveScan = false;
    private int maxScanDepth = 3;

    private Consumer<File> onFileDrop;
    private Consumer<String> onDirectoryDrop;
    private Consumer<java.util.List<File>> onMultipleFilesDrop;

    private Label titleLabel;
    private Label descriptionLabel;
    private FontIcon iconView;

    public DragAndDropZone() {
        this.setSpacing(10);
        padding(20);
        align(Pos.CENTER);
        stylesClass(Styles.BORDER_DEFAULT, Styles.ROUNDED);
        minHeight(150);

        // 初始化 UI 组件
        iconView = FXFontIcon.create(iconCode);
        titleLabel = FXLabel.create(titleText).stylesClass(Styles.TEXT_MUTED).h4();
        descriptionLabel = FXLabel.create(descriptionText).stylesClass(Styles.TEXT_MUTED);

        this.add(iconView, titleLabel, descriptionLabel);

        setupDragAndDrop();
    }

    /**
     * 设置拖拽事件处理
     */
    private void setupDragAndDrop() {
        // 拖拽进入
        this.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                background("-color-accent-subtle");
            }
            event.consume();
        });

        // 拖拽离开
        this.setOnDragExited(event -> {
            background("transparent");
        });

        // 放置文件
        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                var files = db.getFiles();
                success = true;

                if (files.size() == 1) {
                    File singleFile = files.getFirst();
                    handleSingleFileDrop(singleFile);
                } else {
                    handleMultipleFilesDrop(files);
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * 处理单个文件/目录拖拽
     */
    private void handleSingleFileDrop(File file) {
        if (file.isDirectory()) {
            if (directoryOnly && onDirectoryDrop != null) {
                // 目录拖拽回调
                onDirectoryDrop.accept(file.getAbsolutePath());
                updateUI("已选择目录: " + file.getName(), Styles.SUCCESS);
            } else if (onDirectoryDrop != null) {
                onDirectoryDrop.accept(file.getAbsolutePath());
                updateUI("已选择目录: " + file.getName(), Styles.SUCCESS);
            } else {
                updateUI("目录: " + file.getName(), Styles.TEXT_MUTED);
            }
        } else {
            if (directoryOnly) {
                AppContext.showNotification("请拖入文件夹，而不是文件", NotificationLevel.WARNING);
                updateUI("请拖入文件夹", Styles.WARNING);
            } else if (onFileDrop != null) {
                onFileDrop.accept(file);
                updateUI("已选择文件: " + file.getName(), Styles.SUCCESS);
            } else {
                updateUI("文件: " + file.getName(), Styles.TEXT_MUTED);
            }
        }
    }

    /**
     * 处理多个文件拖拽
     */
    private void handleMultipleFilesDrop(java.util.List<File> files) {
        if (onMultipleFilesDrop != null) {
            onMultipleFilesDrop.accept(files);
            updateUI("已选择 " + files.size() + " 个文件", Styles.SUCCESS);
        } else {
            // 默认处理：逐个处理
            for (File file : files) {
                handleSingleFileDrop(file);
            }
        }
    }

    /**
     * 更新 UI 状态
     */
    private void updateUI(String message, String styleClass) {
        TaskRunner.runInFx(() -> {
            titleLabel.setText(message);
            titleLabel.getStyleClass().removeAll(Styles.TEXT_MUTED, Styles.SUCCESS, Styles.WARNING, Styles.DANGER);
            titleLabel.getStyleClass().add(styleClass);
        });
    }

    // ==================== 链式配置方法 ====================

    /**
     * 设置标题文本
     */
    public DragAndDropZone title(String text) {
        this.titleText = text;
        if (titleLabel != null) {
            titleLabel.setText(text);
        }
        return this;
    }

    /**
     * 设置描述文本
     */
    public DragAndDropZone description(String text) {
        this.descriptionText = text;
        if (descriptionLabel != null) {
            descriptionLabel.setText(text);
        }
        return this;
    }

    /**
     * 设置图标
     */
    public DragAndDropZone icon(Ikon icon) {
        this.iconCode = icon;
        if (iconView != null) {
            iconView.setIconCode(icon);
        }
        return this;
    }

    /**
     * 设置是否仅接受目录
     */
    public DragAndDropZone directoryOnly(boolean only) {
        this.directoryOnly = only;
        return this;
    }

    /**
     * 设置是否递归扫描（用于 Git 项目扫描）
     */
    public DragAndDropZone recursiveScan(boolean recursive) {
        this.recursiveScan = recursive;
        return this;
    }

    /**
     * 设置最大扫描深度
     */
    public DragAndDropZone maxScanDepth(int depth) {
        this.maxScanDepth = depth;
        return this;
    }

    /**
     * 设置文件拖拽回调
     */
    public DragAndDropZone onFileDrop(Consumer<File> handler) {
        this.onFileDrop = handler;
        return this;
    }

    /**
     * 设置目录拖拽回调
     */
    public DragAndDropZone onDirectoryDrop(Consumer<String> handler) {
        this.onDirectoryDrop = handler;
        return this;
    }

    /**
     * 设置多文件拖拽回调
     */
    public DragAndDropZone onMultipleFilesDrop(Consumer<java.util.List<File>> handler) {
        this.onMultipleFilesDrop = handler;
        return this;
    }

    /**
     * 重置 UI 状态
     */
    public void reset() {
        updateUI(titleText, Styles.TEXT_MUTED);
        if (descriptionLabel != null) {
            descriptionLabel.setText(descriptionText);
        }
    }
}
