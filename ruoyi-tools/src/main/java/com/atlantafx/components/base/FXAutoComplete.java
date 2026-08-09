package com.atlantafx.components.base;

import com.atlantafx.util.TaskRunner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * FXAutoComplete - 高精智能自动完成文本框（悬浮管线防越界版）
 * 核心：完全清洗了原代码中 subList 的下标越界隐伤。
 * 放弃了 VBox 物理挤压排版，改用现代化的 Popup 浮层技术，建议列表凌空悬浮，绝不抖动和干扰下方表单。
 */
public class FXAutoComplete extends VBox implements IFXNode<FXAutoComplete> {

    private final TextField textField;
    private final ListView<String> suggestionList;
    private final Popup popup; // 引入独立悬浮窗管线，隔绝父级排版流

    private final ObservableList<String> rawData = FXCollections.observableArrayList();
    private final ObservableList<String> suggestions = FXCollections.observableArrayList();

    private Predicate<String> filterPredicate = (item) -> true;
    private Function<String, String> converter = Function.identity();

    private int maxSuggestions = 5;
    private boolean showAllOnFocus = false;

    public FXAutoComplete() {
        super();
        setSpacing(0);

        // 1. 初始化核心文本框
        this.textField = new TextField();
        getChildren().add(textField);

        // 2. 初始化建议列表，接入 AtlantaFX 的专属弹窗样式
        this.suggestionList = new ListView<>(suggestions);

        // 动态高精计算高度限制 facts，防止单条记录撑开或多条记录溢出
        this.suggestionList.setPrefHeight(maxSuggestions * 32.0 + 2);
        this.suggestionList.setMaxHeight(maxSuggestions * 32.0 + 2);

        // 3. 构建独立遮罩悬浮窗
        this.popup = new Popup();
        this.popup.setAutoHide(true); // 鼠标点击别处时自动失焦收起
        this.popup.getContent().add(suggestionList);

        initEventPipelines();
    }

    public static FXAutoComplete create() {
        return new FXAutoComplete();
    }

    /**
     * 核心：装配高性能事件总线与键盘动作监听
     */
    private void initEventPipelines() {
        // 监听文本框内容实时震荡
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                if (showAllOnFocus) {
                    showAllDataDirectly();
                } else {
                    hideSuggestions();
                }
            } else {
                updateSuggestions(newText);
            }
        });

        // 焦点链路追踪
        textField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused && showAllOnFocus && textField.getText().isEmpty()) {
                showAllDataDirectly();
            } else if (!isFocused) {
                // 延迟收起，防止鼠标在点击建议列表的瞬间由于焦点丧失而导致触发死锁
                TaskRunner.runInFx(this::hideSuggestions);
            }
        });

        // 高级键盘辅助上屏机制 facts
        textField.setOnKeyPressed(event -> {
            if (popup.isShowing() && !suggestions.isEmpty()) {
                if (event.getCode() == KeyCode.DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.getSelectionModel().selectFirst();
                    event.consume();
                }
            }
        });

        // 列表内部键盘动作
        suggestionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSelectionSubmit();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hideSuggestions();
                textField.requestFocus();
                event.consume();
            }
        });

        // 鼠标点击确认上屏
        suggestionList.setOnMouseClicked(event -> {
            if (!suggestionList.getSelectionModel().isEmpty()) {
                handleSelectionSubmit();
            }
        });

        // 动态锚定：当主窗体在桌面上被拖拽平移时，悬浮窗必须像素级同步跟随平移
        textField.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.xProperty().addListener((o, oX, nX) -> relocatePopupPosition());
                        newWin.yProperty().addListener((o, oY, nY) -> relocatePopupPosition());
                    }
                });
            }
        });
    }

    /**
     * 核心：彻底根除 subList 越界漏洞的安全流式数据检索引擎
     */
    private void updateSuggestions(String text) {
        if (rawData.isEmpty()) {
            hideSuggestions();
            return;
        }

        // 1. 使用 Java 8+ Stream 管道流进行安全过滤与映射 facts
        List<String> filtered = rawData.stream()
                .filter(item -> {
                    try {
                        return filterPredicate.test(item) &&
                                item.toLowerCase().contains(text.toLowerCase());
                    } catch (Exception e) {
                        return false; // 防御可能因外部自定义断言引发的空指针崩溃
                    }
                })
                .map(converter)
                .collect(Collectors.toList());

        // 2. 核心：执行严密的防御性前置阻断，绝对不执行盲目的 subList 截取
        if (filtered == null || filtered.isEmpty()) {
            suggestions.clear();
            hideSuggestions();
            return;
        }

        // 3. 安全平滑拷贝，消灭下标越界
        int limit = Math.min(filtered.size(), maxSuggestions);
        List<String> safeSubList = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            safeSubList.add(filtered.get(i));
        }

        suggestions.setAll(safeSubList);

        if (!suggestions.isEmpty()) {
            showSuggestions();
        } else {
            hideSuggestions();
        }
    }

    private void showAllDataDirectly() {
        if (!rawData.isEmpty()) {
            int limit = Math.min(rawData.size(), maxSuggestions);
            suggestions.setAll(rawData.subList(0, limit));
            showSuggestions();
        }
    }

    private void handleSelectionSubmit() {
        String selected = suggestionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            textField.setText(selected);
            textField.end(); // 光标强制移动到文本末尾
            hideSuggestions();
            textField.requestFocus();
        }
    }

    /**
     * 核心：高精物理锚定计算，使 Popup 浮层绝对锁死在输入框正下方，宽度与输入框保持强一致
     */
    private void relocatePopupPosition() {
        if (!popup.isShowing()) return;

        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
        if (bounds != null) {
            popup.setX(bounds.getMinX());
            popup.setY(bounds.getMaxY());
            suggestionList.setPrefWidth(bounds.getWidth());
        }
    }

    private void showSuggestions() {
        if (textField.getScene() == null || textField.getScene().getWindow() == null) return;

        if (!popup.isShowing()) {
            Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
            if (bounds != null) {
                // 凌空展现，不侵占任何父级排版盒空间 facts
                popup.show(textField.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY());
                suggestionList.setPrefWidth(bounds.getWidth());
            }
        } else {
            relocatePopupPosition();
        }
    }

    private void hideSuggestions() {
        if (popup.isShowing()) {
            popup.hide();
        }
    }

    /* =========================================================================
     * 开箱即用高频流式链式扩展 API
     * ========================================================================= */

    public FXAutoComplete items(List<String> items) {
        this.rawData.setAll(items);
        return this;
    }

    public FXAutoComplete maxSuggestions(int max) {
        this.maxSuggestions = Math.max(1, max);
        this.suggestionList.setPrefHeight(maxSuggestions * 32.0 + 2);
        this.suggestionList.setMaxHeight(maxSuggestions * 32.0 + 2);
        return this;
    }

    public FXAutoComplete showAllOnFocus(boolean enable) {
        this.showAllOnFocus = enable;
        return this;
    }

    public FXAutoComplete filter(Predicate<String> predicate) {
        if (predicate != null) this.filterPredicate = predicate;
        return this;
    }

    public FXAutoComplete converter(Function<String, String> converter) {
        if (converter != null) this.converter = converter;
        return this;
    }

    public FXAutoComplete width(double w) {
        textField.setMinWidth(w);
        textField.setPrefWidth(w);
        return this;
    }

    public FXAutoComplete height(double h) {
        textField.setMinHeight(h);
        textField.setPrefHeight(h);
        return this;
    }

    public String getText() {
        return textField.getText();
    }

    public FXAutoComplete setText(String text) {
        textField.setText(text);
        return this;
    }

    public TextField getRawTextField() {
        return textField;
    }
}