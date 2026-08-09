package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * FXWizard - 基于 AtlantaFX 风格的现代化高级分步表单向导组件
 * 继承自 JavaFX BorderPane，实现 IFXNode 接口支持全量链式调用
 * 优化细节：重写步骤指示器为顶部横向流（Top Horizontal Steps），彻底修复原代码物理切页不刷新的硬伤，
 * 引入行业主流的“数字圈+逻辑线”状态矩阵，完美对齐企业级向导卡片标准。
 */
public class FXWizard extends BorderPane implements IFXNode<FXWizard> {

    // 核心数据模型轨道
    private final List<Node> steps = new ArrayList<>();
    private final List<String> stepTitles = new ArrayList<>();

    // 现代化顶部步骤条外壳
    private final HBox stepIndicatorBar;

    // 状态机指标
    private int currentStep = 0;
    private Consumer<Integer> onStepChangeCallback;
    private Runnable onFinishCallback;

    /**
     * 构造函数私有化，强制通过静态工厂方法 create() 进行流式实例化
     */
    private FXWizard() {
        super();

        // 1. 初始化顶部现代化横向步骤条（移除了原左侧死板的灰色硬编码面板）
        this.stepIndicatorBar = new HBox();
        this.stepIndicatorBar.setAlignment(Pos.CENTER);
        this.stepIndicatorBar.setSpacing(0); // 依靠连线撑开间距
        this.stepIndicatorBar.setPadding(new Insets(20, 15, 20, 15));
        // 使用 AtlantaFX 的次级底色变量使顶部视觉更为高级
        this.stepIndicatorBar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; -fx-border-width: 0 0 1px 0;");

        // 将步骤条挂载至顶部槽位
        setTop(this.stepIndicatorBar);
    }

    /**
     * 创建一个全新的 FXWizard 向导实例
     *
     * @return FXWizard 实例（链式调用入口）
     */
    public static FXWizard create() {
        return new FXWizard();
    }

    // ==================== 步骤注册与流式切页核心状态机 ====================

    /**
     * 向向导面板中追加注册一个独立的步骤卡片
     *
     * @param title   该步骤在顶部指示条中呈现的短标题说明（如：1. 身份认证）
     * @param content 该步骤对应的 JavaFX 核心业务布局节点
     * @return FXWizard 实例（链式调用）
     */
    public FXWizard addStep(String title, Node content) {
        if (content != null) {
            this.steps.add(content);
            this.stepTitles.add(title != null ? title : "步骤 " + (steps.size()));

            // 首次注册步骤时，默认挂载第一页作为首屏激活资产
            if (this.steps.size() == 1) {
                renderCurrentStep();
            } else {
                rebuildIndicatorUi(); // 刷新顶部步骤条
            }
        }
        return this;
    }

    /**
     * 状态机指令：物理驱动向导跳转至特定的绝对索引页
     *
     * @param index 目标页下标（从 0 开始计数）
     * @return FXWizard 实例（链式调用）
     */
    public FXWizard selectStep(int index) {
        if (index >= 0 && index < steps.size() && index != currentStep) {
            this.currentStep = index;
            renderCurrentStep();
        }
        return this;
    }

    /**
     * 状态机指令：向前推进（进入下一步骤）
     * 彻底修复原代码点击无法触发下一步执行的硬伤
     */
    public void next() {
        if (currentStep < steps.size() - 1) {
            currentStep++;
            renderCurrentStep();
        } else if (currentStep == steps.size() - 1 && onFinishCallback != null) {
            // 已达最后一页，触发完成终点钩子
            onFinishCallback.run();
        }
    }

    /**
     * 状态机指令：向后回滚（返回上一步骤）
     */
    public void prev() {
        if (currentStep > 0) {
            currentStep--;
            renderCurrentStep();
        }
    }

    // ==================== 内部核心重绘渲染核心 ====================

    /**
     * 执行真机视口切换与指示器重绘
     * 核心优化点：强制调用 setCenter 刷新内容区，并实时计算响应式样式矩阵
     */
    private void renderCurrentStep() {
        if (steps.isEmpty()) return;

        // 1. 物理劫持并切换中心视口区域（彻底解决不切换问题）
        Node currentNode = steps.get(currentStep);
        setCenter(currentNode);

        // 2. 刷新顶部步骤条的激活状态样式
        rebuildIndicatorUi();

        // 3. 异步驱动外部事件监听钩子
        if (onStepChangeCallback != null) {
            onStepChangeCallback.accept(currentStep);
        }
    }

    /**
     * 重构指示器 UI：参考 Element Plus 规范实现“数字圆圈 + 逻辑连线”组合
     */
    private void rebuildIndicatorUi() {
        stepIndicatorBar.getChildren().clear();
        int total = steps.size();

        for (int i = 0; i < total; i++) {
            // 创建数字化状态圆圈
            Label circleLabel = new Label(String.valueOf(i + 1));
            circleLabel.setAlignment(Pos.CENTER);
            circleLabel.setPrefSize(28, 28);
            circleLabel.setMinSize(28, 28);

            // 创建文本说明标签
            Label titleLabel = new Label(stepTitles.get(i));
            titleLabel.setStyle("-fx-font-weight: bold;");

            // 水平组合成一个步骤单元（Step Item）
            HBox stepItem = new HBox(8, circleLabel, titleLabel);
            stepItem.setAlignment(Pos.CENTER);

            // 根据状态机状态，动态注入语义样式矩阵
            if (i < currentStep) {
                // 已处理完成的历史步骤：亮绿色高亮
                circleLabel.setStyle("-fx-background-color: -color-success-emphasis; -fx-text-fill: -color-fg-default; -fx-background-radius: 999;");
                titleLabel.setStyle("-fx-text-fill: -color-fg-muted;");
            } else if (i == currentStep) {
                // 当前正激活的步骤：主题信息蓝高亮
                circleLabel.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white; -fx-background-radius: 999;");
                titleLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
            } else {
                // 未到达的处于冷冻期的步骤：静音灰
                circleLabel.setStyle("-fx-background-color: -color-bg-overlay; -fx-text-fill: -color-fg-muted; -fx-background-radius: 999; -fx-border-color: -color-border-default; -fx-border-radius: 999;");
                titleLabel.setStyle("-fx-text-fill: -color-fg-muted;");
            }

            stepIndicatorBar.getChildren().add(stepItem);

            // 核心：如果不是最后一项，在两个步骤单元之间强力插入一根具有拉伸延伸优先级的逻辑中继连线
            if (i < total - 1) {
                HBox line = new HBox();
                line.setPrefHeight(2);
                line.setMinHeight(2);
                HBox.setHgrow(line, Priority.ALWAYS);

                // 动态根据进度为逻辑线染上不同的颜色
                if (i < currentStep) {
                    line.setStyle("-fx-background-color: -color-success-emphasis;");
                } else {
                    line.setStyle("-fx-background-color: -color-border-muted;");
                }

                // 给连线左右各留白 15 像素呼吸间距
                HBox.setMargin(line, new Insets(0, 15, 0, 15));

                // 纵向居中对齐步骤条
                VBox lineWrapper = new VBox(line);
                lineWrapper.setAlignment(Pos.CENTER);
                HBox.setHgrow(lineWrapper, Priority.ALWAYS);

                stepIndicatorBar.getChildren().add(lineWrapper);
            }
        }
    }

    // ==================== 响应式高阶函数式钩子回调 ====================

    /**
     * 快捷注册步骤物理跳转切换时的全局监听响应式钩子
     *
     * @param callback 消费事件的回调函数（接收当前最新的绝对步数索引值）
     * @return FXWizard 实例（链式调用）
     */
    public FXWizard onStepChange(Consumer<Integer> callback) {
        this.onStepChangeCallback = callback;
        return this;
    }

    /**
     * 快捷注册向导最后一页向前突进完成时的终点触发钩子（如：触发保存至本地 SQLite 数据库）
     *
     * @param callback 运行期回调
     * @return FXWizard 实例（链式调用）
     */
    public FXWizard onFinish(Runnable callback) {
        this.onFinishCallback = callback;
        return this;
    }

    // ==================== 物理界限状态机快照只读探测 ====================

    /**
     * 探测当前状态机是否恰好卡死在首屏第一页
     */
    public boolean isFirstStep() {
        return currentStep == 0;
    }

    /**
     * 探测当前状态机是否恰好卡死在最后一页终点
     */
    public boolean isLastStep() {
        return !steps.isEmpty() && currentStep == steps.size() - 1;
    }

    /**
     * 提取当前激活项的内部绝对数字快照
     */
    public int getCurrentStepIndex() {
        return this.currentStep;
    }

    // ==================== 通用物理控制属性流式扩展 ====================

    /**
     * 一键剥离向导底面板背景骨架，转换为更内嵌的皮肤风格
     */
    public FXWizard flat() {
        this.stepIndicatorBar.setStyle("-fx-background-color: transparent; -fx-border-color: -color-border-muted; -fx-border-width: 0 0 1px 0;");
        return this;
    }

    /**
     * 锁定分步向导控制台的固定物理首选宽度
     */
    public FXWizard width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 锁定分步向导控制台的固定物理首选高度
     */
    public FXWizard height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 批量安全追加底层原始的 CSS 样式类
     */
    public FXWizard stylesClass(String... classes) {
        if (classes != null) {
            getStyleClass().addAll(classes);
        }
        return this;
    }
}