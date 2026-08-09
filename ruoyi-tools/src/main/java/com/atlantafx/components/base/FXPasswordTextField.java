package com.atlantafx.components.base;

import atlantafx.base.controls.PasswordTextField;
import javafx.scene.Cursor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;

/**
 * FXPasswordTextField
 */
public class FXPasswordTextField extends PasswordTextField implements IFXNode<FXPasswordTextField> {

    private FXPasswordTextField() {
        super();
    }

    public static FXPasswordTextField create() {
        FXPasswordTextField fxPasswordTextField = new FXPasswordTextField();
        FXFontIcon fxFontIcon = FXFontIcon.create(MaterialDesignE.EYE_OFF);
        fxFontIcon.setCursor(Cursor.HAND);
        fxFontIcon.setOnMouseClicked(event -> {
            fxFontIcon.setIconCode(fxPasswordTextField.getRevealPassword() ? MaterialDesignE.EYE_CLOSED : MaterialDesignE.EYE);
            fxPasswordTextField.setRevealPassword(!fxPasswordTextField.getRevealPassword());
        });
        fxPasswordTextField.setRight(fxFontIcon);
        return fxPasswordTextField;
    }

    public FXPasswordTextField prefWidthValue(double width) {
        this.setPrefWidth(width);
        return this;
    }
}
