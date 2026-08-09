package com.atlantafx.features.password;

import cn.hutool.crypto.digest.BCrypt;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.util.ClipboardUtils;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import static com.atlantafx.features.license.LicenseView.*;

/**
 * 用户密码生成页面。
 * <p>
 * RuoYi-Vue-Plus 使用 hutool 的 BCrypt 进行密码加密，
 * 数据库 sys_user.password 字段存储的是 BCrypt 哈希值（格式 $2a$10$...）。
 * 本页面提供：
 * <ol>
 *   <li>明文密码 → BCrypt 哈希（可直接写入数据库）</li>
 *   <li>密码校验（明文 vs 哈希）</li>
 *   <li>生成 SQL UPDATE 语句</li>
 * </ol>
 */
@Page(id = "password", name = "生成用户密码", icon = "mdi2p-passport", order = 3, level = 1, lazyLoad = false)
public class PasswordView extends BaseView {

    private FXVBox mainLayout;

    // ---------- 生成哈希 ----------
    private FXTextField passwordField;
    private FXComboBox<Integer> costBox;
    private FXButton generateButton;
    private FXTextArea hashResultArea;
    private FXLabel hashDetailLabel;

    // ---------- 校验 ----------
    private FXTextField verifyPasswordField;
    private FXTextArea verifyHashArea;
    private FXButton verifyButton;
    private FXLabel verifyResultLabel;

    // ---------- SQL ----------
    private FXTextField userIdField;
    private FXTextField userNameField;
    private FXButton generateSqlButton;
    private FXTextArea sqlResultArea;

    @Override
    protected void onPageCreated() {
        mainLayout = FXVBox.create(20);

        passwordField = FXTextField.create("输入明文密码，如 admin123").hgrow();
        costBox = FXComboBox.<Integer>create().add(4, 5, 6, 7, 8, 9, 10, 11, 12).select(10);
        costBox.setPrefWidth(100);

        hashResultArea = FXTextArea.create("点击「生成哈希」后在此显示").editable(false).wrapText(true).prefHeightValue(80);
        hashDetailLabel = FXLabel.create("").muted().wrapText(true);

        verifyPasswordField = FXTextField.create("输入明文密码").hgrow();
        verifyHashArea = FXTextArea.create("粘贴 BCrypt 哈希值，如 $2a$10$7JB720...").rowCount(2).wrapText(true);
        verifyResultLabel = FXLabel.create("").wrapText(true);

        userIdField = FXTextField.create("用户ID，如 1761100000000000001").hgrow();
        userNameField = FXTextField.create("用户名，如 admin（留空则按用户ID更新）").hgrow();
        sqlResultArea = FXTextArea.create("点击「生成 SQL」后在此显示").editable(false).wrapText(true).prefHeightValue(100);
    }

    @Override
    protected Node onPageInit() {
        return mainLayout.add(buildGenerateCard(), buildVerifyCard(), buildSqlCard());
    }

    @Override
    protected void onPageDispose() {
    }

    // =========================================================================
    // 生成哈希
    // =========================================================================

    private Node buildGenerateCard() {
        generateButton = FXButton.create("生成哈希").icon(MaterialDesignS.SHIELD_KEY).accent()
                .onAction(e -> doGenerate());

        FXButton copyHashButton = FXButton.create("复制哈希").icon(MaterialDesignC.CONTENT_COPY).flat()
                .onAction(e -> ClipboardUtils.copy(hashResultArea.getText()));

        FXButton fillToSqlButton = FXButton.create("填入下方 SQL").flat()
                .onAction(e -> {
                    String hash = hashResultArea.getText();
                    if (hash != null && !hash.isBlank()) {
                        sqlResultArea.text("");
                        generateSqlButton.fire();
                    }
                });

        FXVBox body = FXVBox.create(12).add(
                formRow("明文密码", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(passwordField).hgrow(passwordField)),
                formRow("cost", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(costBox, FXLabel.create("越大越安全、越慢，后端默认 10").muted())),
                FXHBox.create(10).add(generateButton),
                hashResultArea,
                hashDetailLabel,
                FXHBox.create(10).add(copyHashButton, fillToSqlButton)
        );

        return FXCardPane.create()
                .header(header("生成 BCrypt 哈希",
                        "与后端 BCrypt.hashpw(password) 一致，格式 $2a$10$...，可直接写入 sys_user.password 字段"))
                .content(body);
    }

    private void doGenerate() {
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        if (password.isEmpty()) {
            passwordField.danger();
            AppContext.showNotification("请输入明文密码", NotificationLevel.WARNING);
            return;
        }
        passwordField.resetState();

        int cost = costBox.getValue() == null ? 10 : costBox.getValue();

        TaskRunner.buildSimple(() -> BCrypt.hashpw(password, BCrypt.gensalt(cost)))
                .withSmoothDelay(200)
                .disableButtonWhileRunning(generateButton)
                .onSuccess(hash -> {
                    hashResultArea.text(hash);
                    hashDetailLabel.text("算法: BCrypt\n" +
                            "cost: " + cost + "\n" +
                            "哈希长度: " + hash.length() + " 字符\n" +
                            "格式: $2a$" + cost + "$...");
                    AppContext.showNotification("哈希已生成", NotificationLevel.SUCCESS);
                })
                .onFailure(ex -> AppContext.showNotification("生成失败: " + rootMessage(ex), NotificationLevel.ERROR))
                .run();
    }

    // =========================================================================
    // 校验
    // =========================================================================

    private Node buildVerifyCard() {
        verifyButton = FXButton.create("校验密码").icon(MaterialDesignS.SHIELD_CHECK).accent()
                .onAction(e -> doVerify());

        FXVBox body = FXVBox.create(12).add(
                formRow("明文密码", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(verifyPasswordField).hgrow(verifyPasswordField)),
                FXLabel.create("BCrypt 哈希").bold(),
                verifyHashArea,
                FXHBox.create(10).add(verifyButton),
                verifyResultLabel
        );

        return FXCardPane.create()
                .header(header("密码校验",
                        "与后端 BCrypt.checkpw(password, hash) 一致，验证明文密码是否匹配某个哈希值"))
                .content(body);
    }

    private void doVerify() {
        String password = verifyPasswordField.getText() == null ? "" : verifyPasswordField.getText().trim();
        String hash = verifyHashArea.getText() == null ? "" : verifyHashArea.getText().trim();

        if (password.isEmpty()) {
            verifyPasswordField.danger();
            AppContext.showNotification("请输入明文密码", NotificationLevel.WARNING);
            return;
        }
        verifyPasswordField.resetState();

        if (hash.isEmpty()) {
            verifyHashArea.danger();
            AppContext.showNotification("请输入 BCrypt 哈希", NotificationLevel.WARNING);
            return;
        }
        verifyHashArea.resetState();

        try {
            boolean match = BCrypt.checkpw(password, hash);
            if (match) {
                verifyResultLabel.text("[通过] 密码匹配").resetState().success();
                AppContext.showNotification("密码匹配", NotificationLevel.SUCCESS);
            } else {
                verifyResultLabel.text("[失败] 密码不匹配").resetState().danger();
                AppContext.showNotification("密码不匹配", NotificationLevel.ERROR);
            }
        } catch (Exception ex) {
            verifyResultLabel.text("[错误] 哈希格式无效: " + rootMessage(ex)).resetState().danger();
            AppContext.showNotification("哈希格式无效", NotificationLevel.ERROR);
        }
    }

    // =========================================================================
    // 生成 SQL
    // =========================================================================

    private Node buildSqlCard() {
        generateSqlButton = FXButton.create("生成 SQL").icon(MaterialDesignD.DATABASE).accent()
                .onAction(e -> doGenerateSql());

        FXButton copySqlButton = FXButton.create("复制 SQL").icon(MaterialDesignC.CONTENT_COPY).flat()
                .onAction(e -> ClipboardUtils.copy(sqlResultArea.getText()));

        FXVBox body = FXVBox.create(12).add(
                formRow("用户ID", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(userIdField).hgrow(userIdField)),
                formRow("用户名", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(userNameField).hgrow(userNameField)),
                FXHBox.create(10).add(generateSqlButton),
                sqlResultArea,
                FXHBox.create(10).add(copySqlButton)
        );

        return FXCardPane.create()
                .header(header("生成 SQL UPDATE 语句",
                        "直接复制到数据库执行，更新 sys_user 表的 password 和 password_update_time 字段"))
                .content(body);
    }

    private void doGenerateSql() {
        String hash = hashResultArea.getText() == null ? "" : hashResultArea.getText().trim();
        if (hash.isEmpty()) {
            AppContext.showNotification("请先生成哈希", NotificationLevel.WARNING);
            return;
        }

        String userId = userIdField.getText() == null ? "" : userIdField.getText().trim();
        String userName = userNameField.getText() == null ? "" : userNameField.getText().trim();

        if (userId.isEmpty() && userName.isEmpty()) {
            AppContext.showNotification("请填写用户ID或用户名", NotificationLevel.WARNING);
            return;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("-- 更新用户密码\n");
        sql.append("UPDATE sys_user SET password = '").append(hash).append("',\n");
        sql.append("    password_update_time = NOW()\n");

        if (!userId.isEmpty() && !userName.isEmpty()) {
            sql.append("WHERE user_id = ").append(userId).append(" AND user_name = '").append(userName).append("';\n");
        } else if (!userId.isEmpty()) {
            sql.append("WHERE user_id = ").append(userId).append(";\n");
        } else {
            sql.append("WHERE user_name = '").append(userName).append("';\n");
        }

        sqlResultArea.text(sql.toString());
        AppContext.showNotification("SQL 已生成", NotificationLevel.SUCCESS);
    }
}
