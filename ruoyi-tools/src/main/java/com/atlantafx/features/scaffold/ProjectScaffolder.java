package com.atlantafx.features.scaffold;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 基于当前 ruoyi-tools 模板，生成一个全新的空项目。
 * <p>
 * 仅做机械化的「目录拷贝 + 文本替换」，不依赖任何第三方库，因此本身也可以被模板自带、
 * 实现「自复制」——在新生成的项目里这个脚手架页面依然存在，可继续派生子项目。
 * <p>
 * 覆盖的改名锚点：
 * <ul>
 *   <li>pom.xml 的项目坐标块(groupId / artifactId / version / name)与应用显示名；</li>
 *   <li>AppState 默认窗口标题(projectName)；</li>
 *   <li>当选定新的基础包时：连同 Java 包声明、import、module-info 模块名、ClassGraph 扫描根、
 *       jpackage 的 mainClass / bundleId 一起替换，并同步搬迁源码目录。</li>
 * </ul>
 */
public final class ProjectScaffolder {

    public static final class Params {
        public Path sourceDir;        // 模板根目录
        public Path targetParentDir;  // 新项目父目录
        public String dirName;        // 目录名 / artifactId / name
        public String groupId;        // Maven groupId
        public String version = "1.0"; // 版本
        public String basePackage;    // 新基础包，例如 com.mycompany.crm
        public String displayName;    // 窗口标题 / 应用显示名
    }

    private static final String OLD_BASE = "com.atlantafx";
    private static final String OLD_PKG_PATH = "com/atlantafx";

    private static final Set<String> TEXT_EXT = Set.of(
            "java", "xml", "css", "fxml", "properties", "md", "txt", "json",
            "yml", "yaml", "html", "gradle", "gitignore", "mf", "cfg", "ini",
            "sql", "toml", "editorconfig");

    // 按路径「段名」排除：任意层级出现以下目录名即跳过（不依赖 glob 的 **/ 前缀语义，
    // 因为 **/xxx 在 Java 中无法匹配顶层 xxx）。
    private static final Set<String> EXCLUDE_NAMES = Set.of(
            "target", ".git", ".idea", ".workbuddy", "node_modules", "logs");

    // 按文件名后缀排除（同样覆盖顶层与嵌套）
    private static final Set<String> EXCLUDE_SUFFIXES = Set.of(".iml", ".log");

    public Path generate(Params p) throws IOException {
        Path src = p.sourceDir.toAbsolutePath().normalize();
        Path parent = p.targetParentDir.toAbsolutePath().normalize();
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
        }
        Path target = parent.resolve(sanitize(p.dirName));
        if (Files.exists(target)) {
            throw new IOException("目标目录已存在，请先清理或更换项目名称: " + target);
        }
        Files.createDirectories(target);

        boolean renamePkg = !OLD_BASE.equals(p.basePackage);
        String newPkgPath = p.basePackage.replace('.', '/');

        copyTree(src, src, target, p, newPkgPath, renamePkg);
        rewritePom(target.resolve("pom.xml"), p);
        rewriteAppStateTitle(target, p.basePackage, p.displayName);
        return target;
    }

    private void copyTree(Path root, Path current, Path targetRoot, Params p,
                          String newPkgPath, boolean renamePkg) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(current)) {
            for (Path child : ds) {
                Path rel = root.relativize(child);
                if (isExcluded(rel)) {
                    continue;
                }

                String relStr = rel.toString().replace('\\', '/');
                if (renamePkg) {
                    // 包根可能嵌套在 src/main/java/ 之下，不能只判断前缀，按整段替换
                    relStr = relStr.replace(OLD_PKG_PATH, newPkgPath);
                }
                Path dest = targetRoot.resolve(relStr);

                if (Files.isDirectory(child)) {
                    Files.createDirectories(dest);
                    copyTree(root, child, targetRoot, p, newPkgPath, renamePkg);
                } else {
                    Files.createDirectories(dest.getParent());
                    if (isText(child)) {
                        String content = Files.readString(child, StandardCharsets.UTF_8);
                        if (renamePkg) {
                            content = content.replace(OLD_BASE, p.basePackage);
                        }
                        Files.writeString(dest, content, StandardCharsets.UTF_8);
                    } else {
                        Files.copy(child, dest, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                }
            }
        }
    }

    private boolean isExcluded(Path rel) {
        for (Path segment : rel) {
            String name = segment.toString();
            if (EXCLUDE_NAMES.contains(name)) {
                return true;
            }
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && EXCLUDE_SUFFIXES.contains(name.substring(dot))) {
                return true;
            }
        }
        return false;
    }

    private boolean isText(Path file) {
        String name = file.getFileName().toString();
        if (name.equals("pom.xml") || name.equals(".gitignore") || name.equals(".editorconfig")) {
            return true;
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return TEXT_EXT.contains(name.substring(dot + 1).toLowerCase());
    }

    /**
     * 改写 pom 坐标块与应用显示名。mainClass / bundleId / module 名等包相关字符串，
     * 在 renamePkg 时已随 com.atlantafx -> 新包 的内容替换一并处理，这里不再重复。
     */
    private void rewritePom(Path pom, Params p) throws IOException {
        if (!Files.exists(pom)) {
            return;
        }
        String c = Files.readString(pom, StandardCharsets.UTF_8);

        // 1) 项目坐标块：首个「groupId -> artifactId -> version -> name」连续块
        String coordRegex = "(<groupId>)[^<]*(</groupId>\\s*<artifactId>)[^<]*(</artifactId>\\s*<version>)[^<]*"
                + "(</version>\\s*<name>)[^<]*(</name>)";
        String coordRepl = "<groupId>" + xml(p.groupId) + "</groupId>\n    <artifactId>"
                + xml(p.dirName) + "</artifactId>\n    <version>" + xml(p.version)
                + "</version>\n    <name>" + xml(p.displayName) + "</name>";
        c = Pattern.compile(coordRegex, Pattern.DOTALL).matcher(c).replaceFirst(coordRepl);

        // 2) 应用显示名相关（与包名无关，始终同步）
        c = c.replace("ruoyi工具集", p.displayName);
        c = c.replace("ruoyi管理平台", p.displayName);

        Files.writeString(pom, c, StandardCharsets.UTF_8);
    }

    private void rewriteAppStateTitle(Path target, String basePackage, String displayName) throws IOException {
        Path f = target.resolve("src/main/java/" + basePackage.replace('.', '/') + "/core/config/AppState.java");
        if (!Files.exists(f)) {
            return;
        }
        String c = Files.readString(f, StandardCharsets.UTF_8);
        c = c.replaceFirst("(projectName\\s*=\\s*new SimpleStringProperty\\()\"[^\"]*\"",
                "$1\"" + xml(displayName) + "\"");
        Files.writeString(f, c, StandardCharsets.UTF_8);
    }

    private static String xml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String sanitize(String dirName) {
        String s = dirName.replaceAll("[^A-Za-z0-9_\\-]", "_");
        return s.isEmpty() ? "new-project" : s;
    }
}
