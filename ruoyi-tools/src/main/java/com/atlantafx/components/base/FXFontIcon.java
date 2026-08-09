package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXFontIcon - 基于 AtlantaFX 风格的现代化字体图标组件
 * 继承自 Ikonli FontIcon，实现 IFXNode 接口以支持纯正的链式流式编程（Fluent API）。
 * 完整保留了源文件的动态动画引擎（Blink, Spin, Swing），并深度融合了自适应染色、
 * 局部高级 Looked-up 颜色穿透以及 AtlantaFX 预设状态色彩规范。
 */
public class FXFontIcon extends FontIcon implements IFXNode<FXFontIcon> {

    /**
     * 通过图标代码字符串创建字体图标（私有化构造器）
     * 强制通过静态工厂方法进行实例化
     *
     * @param iconCode 图标代码字符串（如 "fas-home"）
     */
    private FXFontIcon(String iconCode) {
        super(iconCode);
    }

    /**
     * 通过 Ikon 枚举类型创建字体图标（私有化构造器，推荐使用，具备编译期类型安全）
     *
     * @param iconCode Ikonli 图标枚举对象
     */
    private FXFontIcon(Ikon iconCode) {
        super(iconCode);
    }

    /**
     * 静态工厂：通过字符串图标代码实例化
     *
     * @param iconCode 图标代码字符串
     * @return FXFontIcon 实例（链式调用入口）
     */
    public static FXFontIcon create(String iconCode) {
        return new FXFontIcon(iconCode);
    }

    /**
     * 静态工厂：通过类型安全的 Ikon 枚举对象实例化（团队开发推荐标准）
     *
     * @param iconCode Ikonli 图标枚举
     * @return FXFontIcon 实例（链式调用入口）
     */
    public static FXFontIcon create(Ikon iconCode) {
        return new FXFontIcon(iconCode);
    }

    // ==================== 基础物理属性控制 ====================

    /**
     * 设置图标的物理呈现尺寸大小
     *
     * @param size 图标大小（像素值）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon size(int size) {
        setIconSize(size);
        // 2. 核心避坑：不要直接 setStyle() 抹除过去。
        // 获取当前已有的内联样式，确保不丢失其他自定义样式
        String currentStyle = getStyle() == null ? "" : getStyle();

        // 3. 彻底隔离清除之前可能残留的旧尺寸内联设置，防止无限字符串追加
        currentStyle = currentStyle.replaceAll("-fx-icon-size\\s*:[^;]+;", "")
                .replaceAll("-fx-font-size\\s*:[^;]+;", "");

        // 4. 终极注入：利用 JavaFX 内联样式的最高权重覆盖外界容器干扰。
        // 同时写入 -fx-icon-size（给 Ikonli 引擎看）和 -fx-font-size（给底层 Text 纹理看）
        setStyle(currentStyle + "; -fx-icon-size: " + size + "px; -fx-font-size: " + size + "px;");

        // 5. 关键杀招：由于 setStyle 可能会干扰 Ikonli 的内部逻辑，
        // 显式调用它的内部重绘刷新触发器（如果原生支持），或者通过强制重置当前图标代码，
        // 逼迫 Ikonli 的监听器（IconCode Property Listener）重新走一遍安全的、“带字体族保护”的渲染生命周期
        Ikon currentCode = getIconCode();
        if (currentCode != null) {
            setIconCode(currentCode);  // 重新绑定，此时 Ikonli 重新读取最高权重的内联 size 并锁死正确的字体族
        }
        return this;
    }

    /**
     * 改变当前图标所呈现的 Ikon 符号代码（动态切换图标）
     *
     * @param iconCode 新的 Ikonli 图标枚举
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon iconCode(Ikon iconCode) {
        setIconCode(iconCode);
        return this;
    }

    /**
     * 设置图标可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置图标是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置图标透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置图标旋转角度
     *
     * @param angle 旋转角度（度数），正值表示顺时针旋转
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon rotate(double angle) {
        setRotate(angle);
        return this;
    }

    // ==================== 保留并增强的源动画引擎 (Animation Engines) ====================

    /**
     * 保留源方法：添加呼吸闪烁动画效果
     *
     * @param durationMs 动画单次呼吸循环的时长（毫秒）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon blink(int durationMs) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(durationMs), this);
        transition.setFromX(1.0);
        transition.setToX(1.2);
        transition.setFromY(1.0);
        transition.setToY(1.2);
        transition.setAutoReverse(true);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.play();
        return this;
    }

    /**
     * 保留源方法：添加自适应呼吸闪烁动画效果（默认 1 秒/周期）
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon blink() {
        return blink(1000);
    }

    /**
     * 保留源方法：添加旋转动画效果
     * 图标会持续无限旋转，极度适用于 Loading、刷新、同步、磁盘异步 IO 读取等状态反馈场景。
     *
     * @param durationMs 旋转一周（360度）的时长（毫秒）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon spin(int durationMs) {
        RotateTransition transition = new RotateTransition(Duration.millis(durationMs), this);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
        return this;
    }

    /**
     * 保留源方法：添加旋转动画效果（默认 1 秒/周）
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon spin() {
        return spin(1000);
    }

    /**
     * 保留源方法：添加摇摆动画效果
     * 图标会进行左右周期性摆动，适用于铃铛通知、系统高危警报、异常闪烁提醒。
     *
     * @param durationMs 摇摆单次循环时长（毫秒）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon swing(int durationMs) {
        RotateTransition transition = new RotateTransition(Duration.millis(durationMs), this);
        transition.setFromAngle(-15);
        transition.setToAngle(15);
        transition.setAutoReverse(true);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.play();
        return this;
    }

    /**
     * 保留源方法：添加摇摆动画效果（默认 1 秒/周期）
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon swing() {
        return swing(1000);
    }

    // ==================== 精细化色彩与 AtlantaFX 变量定制 ====================

    /**
     * 对齐 FXButton：利用 Paint 对象（如 Color.RED）快捷为图标核心填充色进行赋值
     *
     * @param paint 颜色对象
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon fontColor(Paint paint) {
        setIconColor(paint);
        return this;
    }

    /**
     * 对齐 FXButton：突破 CSS 局部样式限制，利用十六进制或 AtlantaFX Looked-up 核心变量进行全局穿透染色。
     * 解决原生 Ikonli 在宿主容器状态切换时（如 Button 悬停、禁用）图标颜色不跟随的痛点。
     *
     * @param color CSS 颜色字符串（如 "#3b82f6" 或 "-color-accent-fg"）
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon fontColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return this;
        }

        // 2. 核心避坑：不要直接 setStyle() 抹除过去。
        // 获取当前已有的内联样式，确保不丢失其他自定义样式
        String currentStyle = getStyle() == null ? "" : getStyle();

        // 3. 彻底隔离清除之前可能残留的旧尺寸内联设置，防止无限字符串追加
        currentStyle = currentStyle.replaceAll("-fx-icon-color\\s*:[^;]+;", "")
                .replaceAll("-fx-text-fill\\s*:[^;]+;", "");

        // 4. 终极注入：利用 JavaFX 内联样式的最高权重覆盖外界容器干扰。
        // 同时写入 -fx-icon-color（给 Ikonli 引擎看）和 -fx-text-fill（给底层 Text 纹理看）
        // 如果传入的是 AtlantaFX 内部变量或十六进制，则同时重定向原生填充色及 CSS 文本填充色权重
        if (!color.startsWith("-")) {
            setStyle(currentStyle + "; -fx-icon-color: " + color + "; -fx-text-fill: " + color + ";");
        } else {
            setStyle(currentStyle + "; -fx-icon-color: " + color + ";");
        }

        // 5. 关键杀招：由于 setStyle 可能会干扰 Ikonli 的内部逻辑，
        // 显式调用它的内部重绘刷新触发器（如果原生支持），或者通过强制重置当前图标代码，
        // 逼迫 Ikonli 的监听器（IconCode Property Listener）重新走一遍安全的、“带字体族保护”的渲染生命周期
        Ikon currentCode = getIconCode();
        if (currentCode != null) {
            setIconCode(currentCode);  // 重新绑定，此时 Ikonli 重新读取最高权重的内联 size 并锁死正确的字体族
        }

        return this;
    }

    // ==================== 经典 AtlantaFX 预设状态颜色快捷变体 ====================

    /**
     * 一键染色：设置图标色彩为系统推荐聚焦主色调 (Styles.ACCENT) - 通常为精致商务蓝
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 一键染色：设置图标色彩为业务逻辑成功态风格 (Styles.SUCCESS) - 绿色形态
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 一键染色：设置图标色彩为高危/异常/错误警示风格 (Styles.DANGER) - 危险红
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 一键染色：设置图标色彩为高频关注/预警提示风格 (Styles.WARNING) - 警告黄
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 彻底重置图标的所有 AtlantaFX 预设状态类，使其返回最初状态
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXFontIcon resetState() {
        getStyleClass().removeAll(Styles.ACCENT, Styles.SUCCESS, Styles.DANGER, Styles.WARNING);
        return this;
    }
}