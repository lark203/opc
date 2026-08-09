package com.atlantafx.components.base;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.atlantafx.core.manager.ModalManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

/**
 * 结合 AtlantaFX Card 和 ModalBox 风格的通用对话框
 */
public class FXCustomDialog extends FXVBox {

    private final Card card = new Card();
    private final FXHBox footer = FXHBox.create(10);
    private final FXButton closeBtn = FXButton.create(null).icon(MaterialDesignC.CLOSE).circle().flat();
    private FXStackPane mask;

    private FXCustomDialog(String title) {
        // 1. 配置 Card 样式 (类似于 ThemeThumbnail 中的阴影和圆角控制)
        card.getStyleClass().add(Styles.ELEVATED_2); // 使用提拉阴影
        card.setMinWidth(400);
        card.setMaxWidth(400);

        // 2. Header 实现 (参考 ThemeDialog 的标题栏)
        Label titleLabel = FXLabel.create(title).h4();
        closeBtn.setOnAction(e -> {
            if (mask != null) {
                ModalManager.hide(mask);
            }
        });
        card.setHeader(FXHBox.create().align(Pos.CENTER_LEFT).add(titleLabel, FXRegion.create().hSpacer(), closeBtn));

        // 3. Footer 实现
        footer.align(Pos.CENTER_RIGHT);
        card.setFooter(footer);

        // 基础布局设置
        this.add(card).padding(50).align(Pos.CENTER);
    }

    public static FXCustomDialog create(String title) {
        return new FXCustomDialog(title);
    }

    public FXCustomDialog setBody(Node body) {
        card.setBody(body);
        return this;
    }

    public FXCustomDialog addAction(Button btn) {
        footer.add(btn);
        return this;
    }

    public Button getCloseButton() {
        return closeBtn;
    }

    public void show() {
        this.mask = ModalManager.show(this);
    }
}