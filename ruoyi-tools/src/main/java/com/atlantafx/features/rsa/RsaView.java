package com.atlantafx.features.rsa;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXButton;
import com.atlantafx.components.base.FXCardPane;
import com.atlantafx.components.base.FXComboBox;
import com.atlantafx.components.base.FXHBox;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXTextArea;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.util.ClipboardUtils;
import com.atlantafx.util.FileChooserUtils;
import com.atlantafx.util.KeyPairUtils;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import java.io.File;

import static com.atlantafx.features.license.LicenseView.formRow;
import static com.atlantafx.features.license.LicenseView.header;
import static com.atlantafx.features.license.LicenseView.rootMessage;

/**
 * RSA 公钥私钥生成页面。
 * <p>
 * RuoYi-Vue-Plus 里 RSA 密钥对有两种互不相干的用途：
 * <ol>
 *   <li>License 签发密钥对：公钥内置于 ruoyi-common-license 的 META-INF/license/public.key</li>
 *   <li>接口加解密 api-decrypt：需要两对密钥，后端 yml 与前端 .env 交叉配对</li>
 * </ol>
 */
@Page(id = "rsa", name = "公钥私钥", icon = "mdi2r-rotate-orbit", order = 4, level = 1, lazyLoad = false)
public class RsaView extends BaseView {

    private static final String MODE_LICENSE = "License 签发密钥对";
    private static final String MODE_API = "接口加解密 api-decrypt 密钥对";

    private FXVBox mainLayout;
    private FXComboBox<String> modeBox;
    private FXComboBox<Integer> bitsBox;
    private FXButton generateButton;

    // License 模式
    private FXVBox licensePane;
    private FXTextArea licensePublicArea;
    private FXTextArea licensePrivateArea;

    // api-decrypt 模式
    private FXVBox apiPane;
    private FXTextArea backendSnippetArea;
    private FXTextArea frontendSnippetArea;

    // 校验
    private FXTextArea validateArea;
    private FXLabel validateResultLabel;

    @Override
    protected void onPageCreated() {
        mainLayout = FXVBox.create(20);

        modeBox = FXComboBox.<String>create().add(MODE_LICENSE, MODE_API).select(MODE_LICENSE).hgrow();
        bitsBox = FXComboBox.<Integer>create().add(1024, 2048, 4096).select(2048);

        licensePublicArea = FXTextArea.create("公钥 license_public.key").editable(false).wrapText(true).prefHeightValue(90);
        licensePrivateArea = FXTextArea.create("私钥 license_private.key").editable(false).wrapText(true).prefHeightValue(140);

        backendSnippetArea = FXTextArea.create("后端 application.yml 片段").editable(false).wrapText(true).prefHeightValue(140);
        frontendSnippetArea = FXTextArea.create("前端 .env 片段").editable(false).wrapText(true).prefHeightValue(140);

        validateArea = FXTextArea.create("粘贴一段裸 Base64 公钥或私钥").rowCount(3).wrapText(true);
        validateResultLabel = FXLabel.create("").wrapText(true);
    }

    @Override
    protected Node onPageInit() {
        licensePane = buildLicensePane();
        apiPane = buildApiPane();
        applyMode(MODE_LICENSE);
        modeBox.onSelect(this::applyMode);

        return mainLayout.add(buildControlCard(), licensePane, apiPane, buildValidateCard());
    }

    @Override
    protected void onPageDispose() {
    }

    // =========================================================================
    // 控制区
    // =========================================================================

    private Node buildControlCard() {
        generateButton = FXButton.create("生成密钥对").icon(MaterialDesignA.AUTORENEW).accent()
                .onAction(e -> doGenerate());

        FXVBox body = FXVBox.create(12).add(
                formRow("用途", modeBox),
                formRow("密钥位数", FXHBox.create(10).align(Pos.CENTER_LEFT).add(bitsBox,
                        FXLabel.create("后端 EncryptUtils 要求不低于 1024 位，低于会导致应用启动失败").muted().wrapText(true))),
                FXHBox.create(10).add(generateButton)
        );

        return FXCardPane.create()
                .header(header("生成 RSA 密钥对", "输出为裸 Base64 单行（公钥 X.509 / 私钥 PKCS#8），不带 PEM 头，可直接粘贴进配置"))
                .content(body);
    }

    private void applyMode(String mode) {
        boolean isLicense = MODE_LICENSE.equals(mode);
        licensePane.visible(isLicense).managed(isLicense);
        apiPane.visible(!isLicense).managed(!isLicense);
        bitsBox.select(isLicense ? 2048 : 2048);
    }

    private void doGenerate() {
        String mode = modeBox.getValue();
        Integer bits = bitsBox.getValue() == null ? 2048 : bitsBox.getValue();

        if (MODE_LICENSE.equals(mode)) {
            TaskRunner.buildSimple(() -> KeyPairUtils.generateRsa(bits))
                    .withSmoothDelay(300)
                    .disableButtonWhileRunning(generateButton)
                    .onSuccess(keys -> {
                        licensePublicArea.text(keys.publicKey());
                        licensePrivateArea.text(keys.privateKey());
                        AppContext.showNotification("License 签发密钥对已生成（" + bits + " 位）", NotificationLevel.SUCCESS);
                    })
                    .onFailure(ex -> AppContext.showNotification("生成失败: " + rootMessage(ex), NotificationLevel.ERROR))
                    .run();
            return;
        }

        // api-decrypt 需要两对：请求对（前端加密/后端解密）与响应对（后端加密/前端解密）
        TaskRunner.buildSimple(() -> new KeyPairUtils.RsaKeys[]{KeyPairUtils.generateRsa(bits), KeyPairUtils.generateRsa(bits)})
                .withSmoothDelay(300)
                .disableButtonWhileRunning(generateButton)
                .onSuccess(pairs -> {
                    KeyPairUtils.RsaKeys request = pairs[0];
                    KeyPairUtils.RsaKeys response = pairs[1];
                    backendSnippetArea.text("""
                            api-decrypt:
                              enabled: true
                              headerFlag: encrypt-key
                              # 响应加密公钥，对应前端 VITE_APP_RSA_PRIVATE_KEY
                              publicKey: %s
                              # 请求解密私钥，对应前端 VITE_APP_RSA_PUBLIC_KEY
                              privateKey: %s
                            """.formatted(response.publicKey(), request.privateKey()));
                    frontendSnippetArea.text("""
                            # 请求加密公钥，对应后端 api-decrypt.privateKey
                            VITE_APP_RSA_PUBLIC_KEY = %s
                            # 响应解密私钥，对应后端 api-decrypt.publicKey
                            VITE_APP_RSA_PRIVATE_KEY = %s
                            """.formatted(request.publicKey(), response.privateKey()));
                    AppContext.showNotification("已生成两对密钥并完成交叉配对（" + bits + " 位）", NotificationLevel.SUCCESS);
                })
                .onFailure(ex -> AppContext.showNotification("生成失败: " + rootMessage(ex), NotificationLevel.ERROR))
                .run();
    }

    // =========================================================================
    // License 模式结果
    // =========================================================================

    private FXVBox buildLicensePane() {
        FXCardPane publicCard = FXCardPane.create()
                .header(header("公钥 license_public.key",
                        "需覆盖 ruoyi-common-license/src/main/resources/META-INF/license/public.key 并重新打包后端"))
                .content(FXVBox.create(10).add(
                        licensePublicArea,
                        FXHBox.create(10).add(
                                copyButton(licensePublicArea),
                                saveButton(licensePublicArea, "license_public.key", "*.key"))
                ));

        FXCardPane privateCard = FXCardPane.create()
                .header(header("私钥 license_private.key", "厂商侧签发密钥，切勿提交到仓库或发给客户"))
                .content(FXVBox.create(10).add(
                        licensePrivateArea,
                        FXHBox.create(10).add(
                                copyButton(licensePrivateArea),
                                saveButton(licensePrivateArea, "license_private.key", "*.key"))
                ));

        FXLabel warning = FXLabel.create("注意：更换签发密钥对会让此前签发的所有授权文件立即失效，且必须重新构建并部署后端。")
                .warning().wrapText(true);

        return FXVBox.create(20).add(warning, publicCard, privateCard);
    }

    // =========================================================================
    // api-decrypt 模式结果
    // =========================================================================

    private FXVBox buildApiPane() {
        FXCardPane backendCard = FXCardPane.create()
                .header(header("后端 ruoyi-admin/src/main/resources/application.yml", "整段替换原有 api-decrypt 配置"))
                .content(FXVBox.create(10).add(
                        backendSnippetArea,
                        FXHBox.create(10).add(copyButton(backendSnippetArea))
                ));

        FXCardPane frontendCard = FXCardPane.create()
                .header(header("前端 .env.development / .env.production", "替换对应两行"))
                .content(FXVBox.create(10).add(
                        frontendSnippetArea,
                        FXHBox.create(10).add(copyButton(frontendSnippetArea))
                ));

        FXLabel tip = FXLabel.create("""
                两对密钥必须交叉配对：前端用「请求对公钥」加密请求，后端用「请求对私钥」解密；\
                后端用「响应对公钥」加密响应，前端用「响应对私钥」解密。\
                任意一侧配错都会导致接口全部报错，格式非法则应用直接启动失败。""")
                .muted().wrapText(true);

        return FXVBox.create(20).add(tip, backendCard, frontendCard);
    }

    // =========================================================================
    // 密钥格式校验
    // =========================================================================

    private Node buildValidateCard() {
        FXButton publicButton = FXButton.create("按公钥校验").icon(MaterialDesignS.SHIELD_CHECK).flat()
                .onAction(e -> doValidate(true));
        FXButton privateButton = FXButton.create("按私钥校验").icon(MaterialDesignS.SHIELD_CHECK).flat()
                .onAction(e -> doValidate(false));
        FXButton loadButton = FXButton.create("从文件读取").flat()
                .onAction(e -> {
                    File file = FileChooserUtils.chooseOpen("选择密钥文件", null, "密钥文件", "*.key");
                    if (file != null) {
                        validateArea.text(FileChooserUtils.readText(file).trim());
                    }
                });

        FXVBox body = FXVBox.create(12).add(
                validateArea,
                FXHBox.create(10).add(publicButton, privateButton, loadButton),
                validateResultLabel
        );

        return FXCardPane.create()
                .header(header("密钥格式校验", "与后端 EncryptUtils.validateRsaPublicKey / validateRsaPrivateKey 同逻辑，用来提前拦住启动失败"))
                .content(body);
    }

    private void doValidate(boolean isPublic) {
        String key = validateArea.getText() == null ? "" : validateArea.getText().trim();
        try {
            int bits = isPublic ? KeyPairUtils.validateRsaPublicKey(key) : KeyPairUtils.validateRsaPrivateKey(key);
            validateResultLabel.text("[通过] 格式合法，密钥长度 " + bits + " 位").resetState().success();
            AppContext.showNotification("密钥校验通过", NotificationLevel.SUCCESS);
        } catch (Exception ex) {
            validateResultLabel.text("[失败] " + rootMessage(ex)).resetState().danger();
            AppContext.showNotification("密钥校验不通过", NotificationLevel.ERROR);
        }
    }

    // =========================================================================
    // 通用
    // =========================================================================

    private FXButton copyButton(FXTextArea source) {
        return FXButton.create("复制").icon(MaterialDesignC.CONTENT_COPY).flat()
                .onAction(e -> ClipboardUtils.copy(source.getText()));
    }

    private FXButton saveButton(FXTextArea source, String defaultName, String extension) {
        return FXButton.create("另存为").icon(MaterialDesignC.CONTENT_SAVE).flat()
                .onAction(e -> {
                    String content = source.getText();
                    if (content == null || content.isBlank()) {
                        AppContext.showNotification("还没有可保存的内容", NotificationLevel.WARNING);
                        return;
                    }
                    File file = FileChooserUtils.chooseSave("保存密钥", defaultName, "密钥文件", extension);
                    if (file != null) {
                        FileChooserUtils.writeText(file, content);
                        AppContext.showNotification("已保存: " + file.getAbsolutePath(), NotificationLevel.SUCCESS);
                    }
                });
    }
}
