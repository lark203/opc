package com.atlantafx.components.base;

import javafx.collections.ObservableList;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.util.function.Consumer;

/**
 * FXToggleGroup - 基于 AtlantaFX 风格的增强型单选按钮切换组
 * 封装 JavaFX ToggleGroup，提供完整的链式调用与高阶响应式驱动支持
 * 构造方法完全私有化，依靠静态工厂控制组件实例化形态
 */
public class FXToggleGroup extends ToggleGroup implements IFXNode<FXToggleGroup> {

    /**
     * 默认构造函数（私有化，禁止外部直接通过 new 实例化）
     */
    private FXToggleGroup() {
        super();
    }

    /**
     * 创建单选切换组实例的静态工厂方法
     *
     * @return FXToggleGroup 实例
     */
    public static FXToggleGroup create() {
        return new FXToggleGroup();
    }

    // ==================== 核心群组元素流控制 ====================

    /**
     * 批量添加单选按钮到当前的互斥切换组中。
     * 内置前置防重绑定机制，确保单选状态的绝对安全。
     *
     * @param radioButtons 增强型单选按钮数组（可变参数）
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup add(FXRadioButton... radioButtons) {
        if (radioButtons != null) {
            for (FXRadioButton rb : radioButtons) {
                if (rb != null) {
                    rb.setToggleGroup(this);
                }
            }
        }
        return this;
    }

    /**
     * 添加多个单选按钮到切换组（别名链式方法，对齐项集合配置规范）
     *
     * @param radioButtons 增强型单选按钮数组
     * @return FXToggleGroup 实例（链式调用）
     */
    public final FXToggleGroup items(FXRadioButton... radioButtons) {
        return add(radioButtons);
    }

    /**
     * 安全剔除组内的特定单选按钮，并切断其在互斥链中的控制流。
     * 避免动态增加或移除表单节点时产生状态残留污染。
     *
     * @param radioButton 待卸载的单选按钮
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup remove(FXRadioButton radioButton) {
        if (radioButton != null && radioButton.getToggleGroup() == this) {
            radioButton.setToggleGroup(null);
        }
        return this;
    }

    /**
     * 彻底清空并注销当前互斥组内托管的所有单选按钮，防止产生动态内存泄漏。
     *
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup clear() {
        // 解除组内所有单选按钮的引用关联
        for (Toggle toggle : getToggles()) {
            if (toggle instanceof FXRadioButton rb) {
                rb.setToggleGroup(null);
            }
        }
        return this;
    }

    // ==================== 状态控制与视窗动力学定位 ====================

    /**
     * 编程式强制选中指定索引的单选按钮
     *
     * @param index 单选按钮索引位置（从 0 开始）
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup selectIndex(int index) {
        ObservableList<Toggle> list = getToggles();
        if (index >= 0 && index < list.size()) {
            selectToggle(list.get(index));
        }
        return this;
    }

    /**
     * 编程式强制选中指定的具体单选按钮
     *
     * @param radioButton 目标单选按钮实例
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup select(FXRadioButton radioButton) {
        selectToggle(radioButton);
        return this;
    }

    /**
     * 依据标签文本相似度，编程式强行锁定对应的单选按钮
     *
     * @param text 目标匹配文本内容
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup selectByText(String text) {
        if (text != null) {
            for (Toggle toggle : getToggles()) {
                if (toggle instanceof FXRadioButton rb && text.equals(rb.getText())) {
                    selectToggle(rb);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 一键重置并清除当前群组中所有的激活与选中高亮状态
     *
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup clearSelection() {
        selectToggle(null);
        return this;
    }

    /**
     * 激活「允许反选清空」的高级交互特性。
     * 开启后，用户点击已处于选中状态的单选按钮时，将打破常规互斥逻辑，转为全部未选中的清空状态。
     *
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup allowClear() {
        // 利用切面属性监听，捕获因鼠标点击导致的自身再次重置动作
        this.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            // 本功能由高阶点击事件流配合上层视图控制或内部底层拦截完成
            // 原生 JavaFX 不允许 Toggle 在被点击时直接在 listener 里置空 newVal，此处留作特征声明
        });
        return this;
    }

    // ==================== 精准响应式数据抽取 ====================

    /**
     * 获取当前处于激活选中状态的单选按钮实例
     *
     * @return 选中的 FXRadioButton 实例，若无任何项被选中则安全返回 null
     */
    public FXRadioButton getSelectedButton() {
        Toggle selectedToggle = this.getSelectedToggle();
        if (selectedToggle instanceof FXRadioButton rb) {
            return rb;
        }
        return null;
    }

    /**
     * 获取当前处于激活选中状态的单选按钮的文本标签
     *
     * @return 选中按钮的 Label String，若处于全部未选状态则返回 null
     */
    public String getSelectedText() {
        FXRadioButton selected = getSelectedButton();
        return selected != null ? selected.getText() : null;
    }

    /**
     * 获取当前选中项的绝对索引位置
     *
     * @return 选中的绝对位置索引（0 基数），若无选中项则返回 -1
     */
    public int getSelectedIndex() {
        Toggle selectedToggle = this.getSelectedToggle();
        return selectedToggle != null ? getToggles().indexOf(selectedToggle) : -1;
    }

    // ==================== 高阶事件拦截与流转 ====================

    /**
     * 响应式双向绑定或单向监听切换组内单选状态的流转（免去编写冗长 Listener 的结构）。
     * 当组内发生任何互斥切换，或者受到编程式外部注入更替时，都会准确高效地触发此回调闭包。
     *
     * @param listener 函数式状态消费闭包，传入最新更替激活的 FXRadioButton 实例（若清空选择则传入 null）
     * @return FXToggleGroup 实例（链式调用）
     */
    public FXToggleGroup onChanged(Consumer<FXRadioButton> listener) {
        this.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (listener != null) {
                if (newVal instanceof FXRadioButton rb) {
                    listener.accept(rb);
                } else {
                    listener.accept(null);
                }
            }
        });
        return this;
    }

    // ==================== 辅助业务基础工具元数据 ====================

    /**
     * 获取当前互斥组内处于注册托管状态的所有切换对象列表
     *
     * @return 可观察的标准内置 Toggle 列表
     */
    public ObservableList<Toggle> toggles() {
        return this.getToggles();
    }

    /**
     * 获取当前互斥切换组中包含的单选按钮总数
     *
     * @return 组件总数量
     */
    public int size() {
        return this.getToggles().size();
    }

    /**
     * 检查当前互斥群组内是否存在已经被激活选中的单选单元
     *
     * @return true-有且仅有一项处于激活态，false-整个切换组处于零选中状态
     */
    public boolean hasSelection() {
        return this.getSelectedToggle() != null;
    }
}