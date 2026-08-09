package com.atlantafx.features.settings;

import atlantafx.base.controls.ToggleSwitch;
import com.atlantafx.AppContext;
import com.atlantafx.AppLauncher;
import com.atlantafx.components.base.*;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.config.ConfigStore;
import com.atlantafx.core.theme.Styles;
import com.atlantafx.core.theme.ThemeManager;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.util.TaskRunner;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Page(id = "settings", name = "设置", icon = "mdi2c-cog", order = 999, level = 1, isHidden = true, lazyLoad = false)
public class SettingsView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(SettingsView.class);
    private final FXVBox mainLayout = FXVBox.create(24);
    private final AppState state = AppState.getInstance();

    @Override
    protected void onPageCreated() {
        log.info("用户设置中台加载就绪。");
    }

    @Override
    protected Node onPageInit() {
        mainLayout.add(createProjectSection());
        // 组装四大现代分栏设置卡片
        mainLayout.add(createAppearanceSection());
        mainLayout.add(createGlobalSwitchesSection());
        mainLayout.add(createPrivacySection());
        mainLayout.add(createMaintenanceSection());

        // 核心 facts：激活双向数据监听管线，任何微小属性变动秒级自动落盘写进 config.json
        setupAutomaticPersistencePipelines();

        return mainLayout;
    }

    private FXVBox createProjectSection() {
        FXGridPane grid = FXGridPane.create().gap(30, 16).align(Pos.CENTER_LEFT);

        // 项目名称
        FXLabel projectNameLabel = FXLabel.create("项目名称").bold();
        FXCustomTextField projectNameField = FXCustomTextField.create().text(state.getProjectName()).rightIcon(MaterialDesignK.KEYBOARD_RETURN).width(220);
        projectNameField.onEnter(event -> state.setProjectName(projectNameField.getText()));

        FXLabel projectVersion = FXLabel.create("项目版本").bold();
        FXLabel projectVersionLabel = FXLabel.create("1.0.0").bold();

        FXLabel projectAuthor = FXLabel.create("项目作者").bold();
        FXLabel projectAuthorLabel = FXLabel.create("wangss").bold();

        FXLabel projectDescription = FXLabel.create("项目描述").bold();
        FXLabel projectDescriptionLabel = FXLabel.create("这是一个基于 JavaFX + Atlantafx 的桌面应用开发框架，用于快速开发桌面应用。").bold();

        // 关于我们
        Hyperlink aboutUs = new Hyperlink("关于我们");
        aboutUs.setOnAction(event -> {
            // 1. 构建弹框的主体内容 (Body)
            FXVBox bodyLayout = FXVBox.create(12).align(Pos.CENTER_LEFT);
            bodyLayout.setFillWidth(true);

            // 软件名称与版本
            FXLabel appNameLabel = FXLabel.create("我的应用软件").h4(); // 使用大标题
            FXLabel versionLabel = FXLabel.create("版本: v1.0.0 (Build 20260616)").muted();

            // 软件简要描述
            FXLabel descLabel = FXLabel.create("这是一款基于 JavaFX 与 AtlantaFX 打造的现代化桌面客户端程序。致力于为用户提供极致流畅的交互体验与优雅的视觉界面。");
            descLabel.setPrefHeight(80);
            descLabel.setWrapText(true);

            // 版权信息
            Label copyrightLabel = FXLabel.create("Copyright © 2026 Wang ss. All Rights Reserved.").muted();

            // 将组件组装到主体布局中
            bodyLayout.add(
                    appNameLabel,
                    versionLabel,
                    FXRegion.create(), // 纵向间距
                    descLabel,
                    FXRegion.create(),
                    copyrightLabel
            );

            // 2. 创建并配置 FXCustomDialog 弹框
            FXCustomDialog dialog = FXCustomDialog.create("关于我们").setBody(bodyLayout);

            // 3. 添加底部的操作按钮 (例如一个确定关闭按钮)
            FXButton confirmBtn = FXButton.create("确定").accent(); // 配合项目封装的 FXButton 或原生 Button
            confirmBtn.setOnAction(e -> {
                // 触发对话框自带的关闭按钮逻辑以销毁遮罩
                dialog.getCloseButton().fire();
            });
            dialog.addAction(confirmBtn);

            // 4. 关键步骤：调用 show() 方法激活 ModalManager 弹出界面
            dialog.show();
        });

        grid.add(projectNameLabel, 0, 0);
        grid.add(projectNameField, 1, 0);
        grid.add(projectVersion, 0, 1);
        grid.add(projectVersionLabel, 1, 1);
        grid.add(projectAuthor, 0, 2);
        grid.add(projectAuthorLabel, 1, 2);
        grid.add(projectDescription, 0, 3);
        grid.add(projectDescriptionLabel, 1, 3);
        grid.add(aboutUs, 0, 4);

        return createSettingsCard("项目信息", "mdi2p-pokeball", grid);
    }


    /**
     * 1. 界面外观卡片区块
     */
    private FXVBox createAppearanceSection() {
        FXGridPane grid = FXGridPane.create().gap(30, 16).align(Pos.CENTER_LEFT);

        // 主题切换
        FXLabel themeLabel = FXLabel.create("应用系统皮肤").bold();
        FXComboBox<String> themeCombo = FXComboBox.<String>create().add(FXCollections.observableArrayList(
                "Primer Light", "Primer Dark", "Nord Light", "Nord Dark", "Cupertino Light", "Cupertino Dark", "Dracula"
        )).select(state.getThemeName()).width(220);
        // 主题即时刷新渲染
        themeCombo.onSelect(nv -> {
            state.setThemeName(nv);
            Styles.applyTheme(Objects.requireNonNull(ThemeManager.getTheme()));
        });

        // 字体缩放
        FXLabel fontLabel = FXLabel.create("全局字体排版大小").bold();
        FXComboBox<Integer> fontCombo = FXComboBox.<Integer>create().add(FXCollections.observableArrayList(12, 13, 14, 15, 16, 18, 20)).width(220);
        fontCombo.setValue(state.getFontSize());
        fontCombo.onSelect(nv -> {
            if (nv != null) {
                state.setFontSize(nv);
                AppContext.applyFontSize();
            }
        });

        grid.add(themeLabel, 0, 0);
        grid.add(themeCombo, 1, 0);
        grid.add(fontLabel, 0, 1);
        grid.add(fontCombo, 1, 1);

        // 背景图片
        FXLabel backgroundImageLabel = FXLabel.create("背景图片").bold();
        FXCustomTextField backgroundImageField = FXCustomTextField.create().text(state.getBackgroundImageUrl()).width(220).disabled(true);
        FXButton backgroundImageBtn = FXButton.create("选择图片").accent().width(80).onAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择背景图片");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(AppContext.getPrimaryStage());
            if (file != null) {
                try {
                    backgroundImageField.setText(file.getAbsolutePath());
                    state.setBackgroundImageUrl(file.getAbsolutePath());
                } catch (Exception ex) {
                    log.error("Error loading background image: ", ex);
                }
            }
        });
        FXButton backgroundImageClearBtn = FXButton.create("恢复默认").danger().width(80).onAction(e -> {
            backgroundImageField.setText("/assets/images/default_bg.png");
            state.setBackgroundImageUrl("/assets/images/default_bg.png");
            state.setSideBarOpacity(0.95);
            state.setContentOpacity(0.9);
        });
        grid.add(backgroundImageLabel, 0, 2);
        grid.add(backgroundImageField, 1, 2);
        grid.add(backgroundImageBtn, 2, 2);
        grid.add(backgroundImageClearBtn, 3, 2);

        // 调整菜单栏透明度
        FXLabel menuBarOpacityLabel = FXLabel.create("菜单栏透明度").bold();
        FXSlider menuBarOpacitySlider = FXSlider.create().min(0.5).max(1).value(0.5).width(220).onValueChanged(state::setSideBarOpacity);
        grid.add(menuBarOpacityLabel, 0, 3);
        grid.add(menuBarOpacitySlider, 1, 3);

        // 调整内容栏透明度
        FXLabel contentBarOpacityLabel = FXLabel.create("内容区域透明度").bold();
        FXSlider contentBarOpacitySlider = FXSlider.create().min(0.5).max(1).value(0.5).width(220).onValueChanged(state::setContentOpacity);
        grid.add(contentBarOpacityLabel, 0, 4);
        grid.add(contentBarOpacitySlider, 1, 4);

        return createSettingsCard("界面外观视效", "mdi2p-palette", grid);
    }

    /**
     * 2. 全局基础性能技术开关卡片
     */
    private FXVBox createGlobalSwitchesSection() {
        FXVBox box = FXVBox.create(14);

        // 硬件加速开关
        box.add(createSwitchItem("强制启用 GPU 硬件加速 (Prism)", "显著提高密集动画和图形交互图层的渲染速度（重启生效）",
                state.hardwareAccelerationProperty()));

        // 脏区渲染优化
        box.add(createSwitchItem("开启界面脏区重绘优化 (DirtyOpts)", "按需刷新像素发生变化的区域，大幅降低能耗（重启生效）",
                state.showDirtyOptsProperty()));

        box.add(createSwitchItem("开启资源使用监控", "实时监控系统资源使用情况，用于系统健康检查（即时生效）", state.monitoringProperty()));

        return createSettingsCard("底层渲染性能控制", "mdi2c-cpu-64-bit", box);
    }

    /**
     * 3. 数据与隐私维护开关卡片
     */
    private FXVBox createPrivacySection() {
        FXVBox box = FXVBox.create(14);

        box.add(createSwitchItem("预加载系统核心页面", "在应用冷启动时提前实例化高频页面，提升响应速度（重启生效）",
                state.preloadMessagesProperty()));

        box.add(createSpinnerSection("设置后台页面销毁时间 (分钟)", "页面切到后台后指定时间后销毁，时间设置小于等于5分钟默认不销毁（即时生效）", state.idleTimeProperty()));

        box.add(createSwitchItem("允许匿名向服务中台发送崩溃日志", "遇到致命崩溃时自动脱敏上传错误，帮助优化软件健壮性（重启生效）",
                state.autoReportCrashProperty()));

        return createSettingsCard("数据与隐私交互规范", "mdi2s-shield-check", box);
    }

    /**
     * 4. 故障维护与系统硬重启卡片
     */
    private FXVBox createMaintenanceSection() {
        FXHBox layout = FXHBox.create(16).align(Pos.CENTER_LEFT);

        FXButton restartBtn = FXButton.create("一键安全重启系统")
                .accent()
                .icon(MaterialDesignR.RESTART)
                .onAction(e -> executeApplicationHardRestart());

        FXLabel desc = FXLabel.create("重启将重新加载底层 JVM 进程并强制冲刷 config.json 缓冲区文件数据").subTitle();
        layout.add(restartBtn);
        layout.add(desc);

        return createSettingsCard("高级故障排查与维护", "mdi2w-wrench", layout);
    }

    /**
     * 核心 facts：建立拦截总线，一旦属性有变动，自动映射同步并完成永久持久化
     */
    private void setupAutomaticPersistencePipelines() {
        state.getPersistedProperties().forEach((key, property) -> {
            property.addListener((obs, oldVal, newVal) -> {
                log.info("检测到配置变化 facts -> 键 [ {} ] 值由 [ {} ] 改为 [ {} ], 正在秒级自动增量落盘...", key, oldVal, newVal);
                ConfigStore.save(key, newVal);
            });
        });
    }

    /**
     * 工具方法：装配一体化开关组件
     */
    private FXHBox createSwitchItem(String title, String description, StringProperty bindProperty) {
        FXHBox layout = FXHBox.create(0).align(Pos.CENTER_LEFT);

        FXVBox textGroup = FXVBox.create(4);
        textGroup.add(FXLabel.create(title).bold());
        textGroup.add(FXLabel.create(description).subTitle());

        ToggleSwitch toggleSwitch = new ToggleSwitch();

        // 数据绑定与初始化清算
        toggleSwitch.setSelected(Boolean.parseBoolean(bindProperty.get()));
        toggleSwitch.selectedProperty().addListener((obs, old, isSelected) -> {
            bindProperty.set(String.valueOf(isSelected));
        });

        layout.add(textGroup);
        // 弹性撑开填充空间 facts
        HBox.setHgrow(textGroup, javafx.scene.layout.Priority.ALWAYS);
        layout.add(toggleSwitch);
        return layout;
    }

    /**
     * 5. 配置项滑块卡片
     */
    private FXHBox createSpinnerSection(String title, String description, StringProperty bindProperty) {
        FXHBox layout = FXHBox.create(0).align(Pos.CENTER_LEFT);

        FXVBox textGroup = FXVBox.create(4);
        textGroup.add(FXLabel.create(title).bold());
        textGroup.add(FXLabel.create(description).subTitle());

        FXSpinner spinner = FXSpinner.create()
                .value(Integer.parseInt(bindProperty.get()))
                .min(0)
                .max(60)
                .step(5)
                .onValueChange(time -> bindProperty.set(String.valueOf(time)));

        layout.add(textGroup);
        // 弹性撑开填充空间 facts
        HBox.setHgrow(textGroup, javafx.scene.layout.Priority.ALWAYS);
        layout.add(spinner);
        return layout;
    }

    /**
     * UI 卡片骨架高阶组装
     */
    private FXVBox createSettingsCard(String sectionTitle, String iconCode, Node content) {
        FXVBox card = FXVBox.create(12);

        FXHBox header = FXHBox.create(10).align(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(18);
        header.add(icon);
        header.add(FXLabel.create(sectionTitle).h4());

        card.add(header);
        card.add(new Separator());
        card.add(content);
        return card;
    }

    /**
     * 核心操作系统交互 facts：安全完成多平台 JVM 硬重启
     */
    /**
     * 核心操作系统交互 facts：安全完成多平台（IDEA开发/JAR包/EXE原生封装） JVM 硬重启
     */
    private void executeApplicationHardRestart() {
        log.warn("正在安全挂起底层组件，准备探测当前运行拓扑结构，执行应用强制硬重启...");
        try {
            List<String> command = new ArrayList<>();

            // 核心 1：抓取当前代码源的物理路径资产
            var codeSourceUri = SettingsView.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File currentSource = new File(codeSourceUri);
            String sourceName = currentSource.getName().toLowerCase();

            // 核心 2：多环境特征全量清算与自适应管线匹配
            if (sourceName.endsWith(".exe")) {
                // =========================================================================
                // 【场景 A】原生二进制 .exe 封装环境（如 jpackage 编译的固定拓扑）
                // =========================================================================
                log.info("探测到当前处于原生封装环境：[ EXE 二进制模式 ] -> {}", currentSource.getAbsolutePath());
                command.add(currentSource.getAbsolutePath());

            } else if (sourceName.endsWith(".jar")) {
                // =========================================================================
                // 【场景 B】独立可执行生产级 JAR 包环境
                // =========================================================================
                log.info("探测到当前处于生产分发环境：[ 独立可执行 JAR 模式 ]");
                String javaHome = System.getProperty("java.home");
                String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

                command.add(javaBin);
                appendJvmPrismOptions(command); // 挂载响应式内核系统级参数
                command.add("-jar");
                command.add(currentSource.getAbsolutePath());

            } else {
                // =========================================================================
                // 【场景 C】IDE 开发测试环境（如 IntelliJ IDEA / Trae）
                // =========================================================================
                log.info("探测到当前处于研发生态环境：[ IDE 源码本地 Debug/Run 模式 ]");
                String javaHome = System.getProperty("java.home");
                String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

                command.add(javaBin);
                appendJvmPrismOptions(command); // 挂载响应式内核系统级参数
                command.add("-cp");
                command.add(System.getProperty("java.class.path"));
                command.add(AppLauncher.class.getName()); // 动态指回引导中台入口
            }

            // 核心 3：派生克隆全新的操作系统子进程，并剥离父进程关联
            log.info("正在执行底层进程派生，最终组装命令是 -> {}", String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder(command);

            // 关键防护：允许子进程作为独立进程组运行，不因父进程 System.exit 而连带阵亡
            pb.start();

            // 核心 4：干净利落地抹除退出当前旧窗口与进程，杜绝僵尸进程常驻
            TaskRunner.runInFx(() -> {
                log.info("新进程派生成立，正在有序冲刷当前旧 JVM 实例的缓冲区...");
                System.exit(0);
            });

        } catch (Exception ex) {
            log.error("核心错误：执行系统多环境自适应硬重启指令时遭遇致命阻塞！", ex);
        }
    }

    /**
     * 辅助抽离：向 JVM 命令管线追加当前个性化设置的硬件加速和重绘控制参数
     */
    private void appendJvmPrismOptions(List<String> command) {
        if (state.isHardwareAcceleration()) {
            command.add("-Dprism.forceGPU=true");
        } else {
            command.add("-Dprism.forceGPU=false");
        }

        if (state.isShowDirtyOpts()) {
            command.add("-Dprism.dirtyopts=true");
        } else {
            command.add("-Dprism.dirtyopts=false");
        }
    }

    @Override
    protected void onPageDispose() {
        mainLayout.getChildren().clear();
    }
}