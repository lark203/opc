package com.atlantafx.features.license;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXButton;
import com.atlantafx.components.base.FXCardPane;
import com.atlantafx.components.base.FXComboBox;
import com.atlantafx.components.base.FXDatePicker;
import com.atlantafx.components.base.FXHBox;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXTextArea;
import com.atlantafx.components.base.FXTextField;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.util.ClipboardUtils;
import com.atlantafx.util.FileChooserUtils;
import com.atlantafx.util.LicenseUtils;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 授权文件（license）签发与校验页面。
 * <p>
 * 等价于 keygen 目录下 sign.sh / SignLicense / VerifyLicense 的桌面版，
 * 产物可直接被 ruoyi-common-license 的 LicenseVerifier 校验通过。
 */
@Page(id = "license", name = "生成license", icon = "mdi2l-license", order = 2, level = 1, lazyLoad = false)
public class LicenseView extends BaseView {

    /**
     * 私钥文件的常见位置，用于文件选择框定位
     */
    private static final String[] PRIVATE_KEY_HINTS = {
            "keygen/license_private.key",
            "../keygen/license_private.key",
            "../../keygen/license_private.key"
    };

    /**
     * 公钥文件的常见位置：keygen 副本 与 后端模块内置副本
     */
    private static final String[] PUBLIC_KEY_HINTS = {
            "keygen/license_public.key",
            "../keygen/license_public.key",
            "../RuoYi-Vue-Plus/ruoyi-common/ruoyi-common-license/src/main/resources/META-INF/license/public.key",
            "../../RuoYi-Vue-Plus/ruoyi-common/ruoyi-common-license/src/main/resources/META-INF/license/public.key"
    };

    private FXVBox mainLayout;

    // ---------- 签发 ----------
    private FXTextArea privateKeyArea;
    private FXTextField fingerprintField;
    private FXDatePicker expireDatePicker;
    private FXTextField expireTimeField;
    private FXTextField versionField;
    private FXComboBox<String> typeBox;
    private FXButton generateButton;
    private FXTextArea resultArea;
    private FXLabel resultDetailLabel;

    // ---------- 校验 ----------
    private FXTextArea verifyContentArea;
    private FXTextArea publicKeyArea;
    private FXTextField expectFingerprintField;
    private FXButton verifyButton;
    private FXLabel verifyResultLabel;

    @Override
    protected void onPageCreated() {
        mainLayout = FXVBox.create(20);

        privateKeyArea = FXTextArea.create("粘贴 PKCS#8 裸 Base64 私钥（单行、无 -----BEGIN----- 头），或点击下方按钮从文件读取")
                .rowCount(3).wrapText(true);
        fingerprintField = FXTextField.create("目标服务器机器指纹，可调用后端 /license/fingerprint 获取").hgrow();
        expireDatePicker = FXDatePicker.create(LocalDate.now().plusYears(1)).format("yyyy-MM-dd");
        expireTimeField = FXTextField.create("HH:mm:ss").text("23:59:59");
        expireTimeField.setPrefWidth(110);
        versionField = FXTextField.create("版本").text("1.0.0").hgrow();
        typeBox = FXComboBox.<String>create().add("trial", "dev", "official").select("trial").editable(true).hgrow();
        resultArea = FXTextArea.create("点击「生成授权文件」后在此显示").editable(false).wrapText(true).prefHeightValue(140);
        resultDetailLabel = FXLabel.create("").muted().wrapText(true);

        verifyContentArea = FXTextArea.create("粘贴授权文件内容，或从 .lic 文件读取").rowCount(3).wrapText(true);
        publicKeyArea = FXTextArea.create("粘贴 X.509 裸 Base64 公钥，或从文件读取").rowCount(3).wrapText(true);
        expectFingerprintField = FXTextField.create("可留空；填写后会额外比对指纹是否一致").hgrow();
        verifyResultLabel = FXLabel.create("").wrapText(true);
    }

    @Override
    protected Node onPageInit() {
        return mainLayout.add(buildSignCard(), buildResultCard(), buildVerifyCard());
    }

    @Override
    protected void onPageDispose() {
    }

    // =========================================================================
    // 签发
    // =========================================================================

    private Node buildSignCard() {
        FXButton loadKeyButton = FXButton.create("从文件读取私钥").icon(MaterialDesignF.FOLDER_OPEN).flat()
                .onAction(e -> {
                    File file = FileChooserUtils.chooseOpen("选择签发私钥", findFirst(PRIVATE_KEY_HINTS), "密钥文件", "*.key");
                    if (file != null) {
                        privateKeyArea.text(FileChooserUtils.readText(file).trim());
                        AppContext.showNotification("已读取私钥: " + file.getName(), NotificationLevel.SUCCESS);
                    }
                });

        FXButton fingerprintButton = FXButton.create("读取本机指纹").icon(MaterialDesignF.FINGERPRINT).flat()
                .tooltip("按后端 MachineFingerprint 算法计算当前这台机器的指纹")
                .onAction(e -> fingerprintField.text(LicenseUtils.machineFingerprint()));

        generateButton = FXButton.create("生成授权文件").icon(MaterialDesignS.SHIELD_KEY).accent()
                .onAction(e -> doGenerate());

        FXVBox body = FXVBox.create(12).add(
                FXLabel.create("私钥").bold(),
                privateKeyArea,
                FXHBox.create(10).add(loadKeyButton),
                formRow("机器指纹", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(fingerprintField, fingerprintButton).hgrow(fingerprintField)),
                formRow("过期时间", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(expireDatePicker, expireTimeField,
                                FXLabel.create("默认一年后，格式 yyyy-MM-dd HH:mm:ss").muted())),
                formRow("版本", versionField),
                formRow("类型", typeBox),
                FXHBox.create(10).add(generateButton)
        );

        return FXCardPane.create()
                .header(header("签发授权文件", "等价于 keygen/sign.sh，RSA-2048 + SHA256withRSA 签名"))
                .content(body);
    }

    private void doGenerate() {
        String privateKey = privateKeyArea.getText() == null ? "" : privateKeyArea.getText().trim();
        String fingerprint = fingerprintField.getText() == null ? "" : fingerprintField.getText().trim();
        String version = versionField.getText() == null ? "" : versionField.getText().trim();
        String type = typeBox.getEditor().getText() == null ? "" : typeBox.getEditor().getText().trim();
        LocalDate date = expireDatePicker.getValue();
        String time = expireTimeField.getText() == null ? "" : expireTimeField.getText().trim();

        if (privateKey.isEmpty()) {
            privateKeyArea.danger();
            AppContext.showNotification("请先提供签发私钥", NotificationLevel.WARNING);
            return;
        }
        privateKeyArea.resetState();
        if (fingerprint.isEmpty()) {
            fingerprintField.danger();
            AppContext.showNotification("机器指纹不能为空", NotificationLevel.WARNING);
            return;
        }
        fingerprintField.resetState();
        if (date == null) {
            AppContext.showNotification("请选择过期日期", NotificationLevel.WARNING);
            return;
        }

        LocalDateTime expire;
        try {
            expire = LocalDateTime.of(date, LocalTime.parse(time));
        } catch (Exception ex) {
            expireTimeField.danger();
            AppContext.showNotification("过期时间格式错误，应为 HH:mm:ss", NotificationLevel.WARNING);
            return;
        }
        expireTimeField.resetState();
        if (expire.isBefore(LocalDateTime.now())) {
            AppContext.showNotification("过期时间早于当前时间，生成的授权将立即失效", NotificationLevel.WARNING);
        }

        String finalVersion = version.isEmpty() ? "1.0.0" : version;
        String finalType = type.isEmpty() ? "trial" : type;

        TaskRunner.buildSimple(() -> LicenseUtils.sign(privateKey, fingerprint, expire, finalVersion, finalType))
                .withSmoothDelay(300)
                .disableButtonWhileRunning(generateButton)
                .onSuccess(content -> {
                    resultArea.text(content);
                    resultDetailLabel.text("指纹: " + fingerprint
                            + "\n有效期至: " + expire.format(LicenseUtils.ISO_FMT)
                            + "\n版本/类型: " + finalVersion + " / " + finalType
                            + "\n长度: " + content.length() + " 字符");
                    AppContext.showNotification("授权文件已生成", NotificationLevel.SUCCESS);
                })
                .onFailure(ex -> AppContext.showNotification("生成失败: " + rootMessage(ex), NotificationLevel.ERROR))
                .run();
    }

    private Node buildResultCard() {
        FXButton copyButton = FXButton.create("复制").icon(MaterialDesignC.CONTENT_COPY).flat()
                .onAction(e -> ClipboardUtils.copy(resultArea.getText()));

        FXButton saveButton = FXButton.create("另存为 license.lic").icon(MaterialDesignC.CONTENT_SAVE).flat()
                .onAction(e -> {
                    String content = resultArea.getText();
                    if (content == null || content.isBlank()) {
                        AppContext.showNotification("还没有可保存的内容", NotificationLevel.WARNING);
                        return;
                    }
                    File file = FileChooserUtils.chooseSave("保存授权文件", "license.lic", "授权文件", "*.lic");
                    if (file != null) {
                        FileChooserUtils.writeText(file, content);
                        AppContext.showNotification("已保存: " + file.getAbsolutePath(), NotificationLevel.SUCCESS);
                    }
                });

        FXButton toVerifyButton = FXButton.create("填入下方校验").flat()
                .onAction(e -> verifyContentArea.text(resultArea.getText()));

        FXVBox body = FXVBox.create(12).add(
                resultArea,
                resultDetailLabel,
                FXHBox.create(10).add(copyButton, saveButton, toVerifyButton)
        );

        return FXCardPane.create()
                .header(header("生成结果", "内容格式 base64url(payload)::base64url(签名)，落地到服务端 config/license.lic"))
                .content(body);
    }

    // =========================================================================
    // 校验
    // =========================================================================

    private Node buildVerifyCard() {
        FXButton loadLicenseButton = FXButton.create("读取 .lic 文件").icon(MaterialDesignF.FOLDER_OPEN).flat()
                .onAction(e -> {
                    File file = FileChooserUtils.chooseOpen("选择授权文件", null, "授权文件", "*.lic");
                    if (file != null) {
                        verifyContentArea.text(FileChooserUtils.readText(file).trim());
                    }
                });

        FXButton loadPublicKeyButton = FXButton.create("读取公钥文件").icon(MaterialDesignF.FOLDER_OPEN).flat()
                .onAction(e -> {
                    File file = FileChooserUtils.chooseOpen("选择验签公钥", findFirst(PUBLIC_KEY_HINTS), "密钥文件", "*.key");
                    if (file != null) {
                        publicKeyArea.text(FileChooserUtils.readText(file).trim());
                    }
                });

        FXButton fillLocalFingerprintButton = FXButton.create("填入本机指纹").flat()
                .onAction(e -> expectFingerprintField.text(LicenseUtils.machineFingerprint()));

        verifyButton = FXButton.create("校验").icon(MaterialDesignS.SHIELD_CHECK).accent()
                .onAction(e -> doVerify());

        FXVBox body = FXVBox.create(12).add(
                FXLabel.create("授权内容").bold(),
                verifyContentArea,
                FXHBox.create(10).add(loadLicenseButton),
                FXLabel.create("验签公钥").bold(),
                publicKeyArea,
                FXHBox.create(10).add(loadPublicKeyButton),
                formRow("期望指纹", FXHBox.create(10).align(Pos.CENTER_LEFT)
                        .add(expectFingerprintField, fillLocalFingerprintButton).hgrow(expectFingerprintField)),
                FXHBox.create(10).add(verifyButton),
                verifyResultLabel
        );

        return FXCardPane.create()
                .header(header("校验授权文件", "校验规则与后端 LicenseVerifier 一致：验签 → 是否过期 → 指纹是否匹配"))
                .content(body);
    }

    private void doVerify() {
        String content = verifyContentArea.getText() == null ? "" : verifyContentArea.getText().trim();
        String publicKey = publicKeyArea.getText() == null ? "" : publicKeyArea.getText().trim();
        String expect = expectFingerprintField.getText() == null ? "" : expectFingerprintField.getText().trim();

        if (content.isEmpty() || publicKey.isEmpty()) {
            AppContext.showNotification("授权内容与公钥都不能为空", NotificationLevel.WARNING);
            return;
        }

        TaskRunner.buildSimple(() -> LicenseUtils.verify(content, publicKey))
                .withSmoothDelay(200)
                .disableButtonWhileRunning(verifyButton)
                .onSuccess(info -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(info.signatureValid() ? "[通过] 签名有效" : "[失败] 签名校验不通过").append('\n');
                    sb.append(info.expired() ? "[失败] 授权已过期" : "[通过] 未过期").append('\n');
                    boolean fingerprintOk = expect.isEmpty() || expect.equals(info.fingerprint());
                    if (!expect.isEmpty()) {
                        sb.append(fingerprintOk ? "[通过] 指纹匹配" : "[失败] 指纹不匹配").append('\n');
                    }
                    sb.append("指纹: ").append(info.fingerprint()).append('\n');
                    sb.append("签发时间: ").append(info.issuedAt()).append('\n');
                    sb.append("过期时间: ").append(info.expireAt()).append('\n');
                    sb.append("版本/类型: ").append(info.version()).append(" / ").append(info.type());

                    boolean allOk = info.signatureValid() && !info.expired() && fingerprintOk;
                    verifyResultLabel.text(sb.toString()).resetState();
                    if (allOk) {
                        verifyResultLabel.success();
                        AppContext.showNotification("校验全部通过", NotificationLevel.SUCCESS);
                    } else {
                        verifyResultLabel.danger();
                        AppContext.showNotification("校验未通过", NotificationLevel.ERROR);
                    }
                })
                .onFailure(ex -> {
                    verifyResultLabel.text(rootMessage(ex)).resetState().danger();
                    AppContext.showNotification("校验失败: " + rootMessage(ex), NotificationLevel.ERROR);
                })
                .run();
    }

    // =========================================================================
    // 通用
    // =========================================================================

    /**
     * 卡片标题 + 副标题
     */
    public static Node header(String title, String subTitle) {
        return FXHBox.create(0).align(Pos.CENTER_LEFT).add(
                FXVBox.create(4).add(FXLabel.create(title).h4(), FXLabel.create(subTitle).muted().wrapText(true))
        );
    }

    /**
     * 定宽标签的表单行
     */
    public static FXHBox formRow(String labelText, Node control) {
        FXLabel label = FXLabel.create(labelText);
        label.setMinWidth(80);
        label.setPrefWidth(80);
        return FXHBox.create(12).align(Pos.CENTER_LEFT).add(label, control).hgrow(control);
    }

    /**
     * 返回第一个存在的文件，用于文件选择框的默认定位
     */
    public static File findFirst(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.isFile()) {
                return file.getAbsoluteFile();
            }
        }
        return null;
    }

    /**
     * 取最内层异常信息，避免界面上出现一长串包装类名
     */
    public static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
    }
}
