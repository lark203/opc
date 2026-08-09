package com.atlantafx.features.scaffold;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.features.scaffold.ProjectScaffolder.Params;
import com.atlantafx.util.TaskRunner;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 项目脚手架页面：基于当前 ruoyi-tools 模板，图形化生成一个全新的空项目。
 * <p>
 * 默认「模板来源」为当前运行项目根目录(user.dir)，「目标位置」为其父目录。
 * 填写参数后点击「生成新项目」，后台递归拷贝并改写关键锚点，生成后可直接打开文件夹开始开发。
 * <p>
 * 注意：所有 UI 组件均通过 FX* 的静态工厂 .create() 创建（构造器私有），并使用链式 API，
 * 禁止直接用 new 实例化 FX* 组件。
 */
@Page(id = "scaffold", name = "项目脚手架", icon = "mdi2c-code-braces", level = 1, order = 7, lazyLoad = true)
public class ScaffoldView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(ScaffoldView.class);

    private FXCustomTextField sourceField;
    private FXCustomTextField targetField;
    private FXCustomTextField dirField;
    private FXCustomTextField groupField;
    private FXCustomTextField pkgField;
    private FXCustomTextField verField;
    private FXCustomTextField nameField;

    private FXTextArea logArea;
    private FXButton genBtn;
    private FXButton openBtn;

    private Path lastGenerated;

    @Override
    protected void onPageCreated() {
        Path here = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path parent = here.getParent() == null ? here : here.getParent();

        sourceField = FXCustomTextField.create().width(420).editable(false).text(here.toString());
        targetField = FXCustomTextField.create().width(420).editable(false).text(parent.toString());
        dirField = FXCustomTextField.create().width(420).text("my-new-app");
        groupField = FXCustomTextField.create().width(420).text("com.atlantafx");
        pkgField = FXCustomTextField.create().width(420).text("com.atlantafx");
        verField = FXCustomTextField.create().width(420).text("1.0");
        nameField = FXCustomTextField.create().width(420).text("我的新应用");

        // 轻量联动：未单独填写时，随项目名称/groupId 自动派生
        dirField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (nameField.getText().isBlank()) {
                nameField.setText(newVal);
            }
        });
        groupField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (pkgField.getText().isBlank() || pkgField.getText().equals(oldVal)) {
                pkgField.setText(newVal);
            }
        });
    }

    @Override
    protected Node onPageInit() {
        FXLabel title = FXLabel.create("项目脚手架").bold();
        FXLabel hint = FXLabel.create(
                        "基于当前 ruoyi-tools 模板，生成一个全新的空项目。源目录默认是当前运行的项目根，"
                                + "目标位置默认在其同级目录。生成后请用 IDE 打开并执行 mvn javafx:run 即可开发。")
                .wrapText(true);
        hint.setMaxWidth(820);

        FXVBox form = FXVBox.create(12).fillWidth(true);
        form.add(
                row("模板来源", sourceField, browseBtn(this::onBrowseSource)),
                row("目标位置", targetField, browseBtn(this::onBrowseTarget)),
                row("项目名称 (目录/artifactId/name)", dirField, null),
                row("GroupId", groupField, null),
                row("基础包名 (com.atlantafx 重命名)", pkgField, null),
                row("版本", verField, null),
                row("窗口标题 / 显示名", nameField, null)
        );

        genBtn = FXButton.create("生成新项目").accent().onAction(this::onGenerate);
        openBtn = FXButton.create("打开文件夹").outline().onAction(this::onOpenFolder);
        openBtn.setDisable(true);
        FXHBox actions = FXHBox.create(10).align(Pos.CENTER_LEFT).add(genBtn, openBtn);

        logArea = FXTextArea.create().editable(false).wrapText(true).prefHeightValue(180);
        logArea.setMaxHeight(180);
        FXLabel logTitle = FXLabel.create("生成日志").bold();

        FXVBox root = FXVBox.create(18)
                .add(title, hint, form, actions, logTitle, logArea)
                .padding(4)
                .mxWidth(900)
                .vgrow(logArea);
        return root;
    }

    @Override
    protected void onPageDispose() {
        // 本页未持有 AnimationTimer / 全局监听器 / 后台线程，无需额外释放
    }

    // =========================================================================
    // UI 辅助
    // =========================================================================

    private FXHBox row(String label, Node field, Node extra) {
        FXHBox h = FXHBox.create(10).align(Pos.CENTER_LEFT);
        FXLabel l = FXLabel.create(label);
        l.setPrefWidth(220);
        l.wrapText(true);
        h.add(l, field);
        if (extra != null) {
            h.add(extra);
        }
        return h;
    }

    private FXButton browseBtn(EventHandler<ActionEvent> handler) {
        return FXButton.create("浏览...").outline().onAction(handler);
    }

    private void appendLog(String s) {
        String cur = logArea.getText();
        logArea.setText((cur.isEmpty() ? "" : cur + "\n") + s);
    }

    // =========================================================================
    // 事件
    // =========================================================================

    private void onBrowseSource(ActionEvent e) {
        chooseDir(sourceField);
    }

    private void onBrowseTarget(ActionEvent e) {
        chooseDir(targetField);
    }

    private void chooseDir(FXCustomTextField field) {
        // 目录选择无 FX* 包装，使用原生 DirectoryChooser（框架 FileChooserUtils 内部同样 new FileChooser）
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("选择目录");
        Stage stage = AppContext.getPrimaryStage();
        File f = dc.showDialog(stage);
        if (f != null) {
            field.setText(f.getAbsolutePath());
        }
    }

    private void onGenerate(ActionEvent e) {
        Params p;
        try {
            p = collectParams();
        } catch (IllegalArgumentException ex) {
            appendLog("校验失败：" + ex.getMessage());
            AppContext.showNotification(ex.getMessage(), NotificationLevel.ERROR);
            return;
        }

        genBtn.setDisable(true);
        appendLog("开始生成项目：" + p.dirName + "  (源=" + p.sourceDir + ")");
        TaskRunner.runAsync(() -> {
            try {
                Path result = new ProjectScaffolder().generate(p);
                TaskRunner.runInFx(() -> onGenerated(result, null));
            } catch (Exception ex) {
                TaskRunner.runInFx(() -> onGenerated(null, ex));
            }
        });
    }

    private void onGenerated(Path result, Exception ex) {
        genBtn.setDisable(false);
        if (ex != null) {
            log.error("项目生成失败", ex);
            appendLog("生成失败：" + ex.getMessage());
            AppContext.showNotification("生成失败：" + ex.getMessage(), NotificationLevel.ERROR);
            return;
        }
        lastGenerated = result;
        appendLog("生成成功：" + result);
        appendLog("提示：用 IDE 打开后执行 mvn javafx:run 即可开始开发（首次建议先重命名基础包）。");
        openBtn.setDisable(false);
        AppContext.showNotification("项目已生成：" + result.getFileName(), NotificationLevel.SUCCESS);
    }

    private void onOpenFolder(ActionEvent e) {
        if (lastGenerated == null) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(lastGenerated.toFile());
                appendLog("已打开文件夹：" + lastGenerated);
            } else {
                appendLog("当前环境不支持自动打开文件夹，路径为：" + lastGenerated);
            }
        } catch (Exception ex) {
            appendLog("打开文件夹失败：" + ex.getMessage() + "  路径：" + lastGenerated);
        }
    }

    // =========================================================================
    // 参数收集与校验
    // =========================================================================

    private Params collectParams() {
        Params p = new Params();
        p.sourceDir = Paths.get(sourceField.getText().trim());
        p.targetParentDir = Paths.get(targetField.getText().trim());
        p.dirName = dirField.getText().trim();
        p.groupId = groupField.getText().trim();
        p.basePackage = pkgField.getText().trim();
        p.version = verField.getText().trim().isEmpty() ? "1.0" : verField.getText().trim();
        p.displayName = nameField.getText().trim().isEmpty() ? p.dirName : nameField.getText().trim();

        if (!Files.isDirectory(p.sourceDir)) {
            throw new IllegalArgumentException("模板来源不是有效目录：" + p.sourceDir);
        }
        if (!Files.isDirectory(p.targetParentDir)) {
            throw new IllegalArgumentException("目标位置不是有效目录：" + p.targetParentDir);
        }
        if (!Pattern.matches("[A-Za-z0-9_\\-]+", p.dirName)) {
            throw new IllegalArgumentException("项目名称只能包含字母、数字、下划线和连字符");
        }
        if (!Pattern.matches("[A-Za-z][A-Za-z0-9_.]*", p.groupId)) {
            throw new IllegalArgumentException("GroupId 不合法（应为合法包名式，如 com.mycompany）");
        }
        if (!isValidPackage(p.basePackage)) {
            throw new IllegalArgumentException("基础包名不合法（每段需为合法 Java 标识符）");
        }
        return p;
    }

    private boolean isValidPackage(String pkg) {
        if (pkg.isBlank()) {
            return false;
        }
        for (String seg : pkg.split("\\.", -1)) {
            if (seg.isEmpty() || !Pattern.matches("[A-Za-z_$][A-Za-z0-9_$]*", seg)) {
                return false;
            }
        }
        return true;
    }
}
