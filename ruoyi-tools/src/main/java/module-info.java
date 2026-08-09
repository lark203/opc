module com.atlantafx {
    // --- 核心依赖 ---
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires java.net.http;
    requires atlantafx.base; // AtlantaFX 主题库
    requires javafx.swing;

    // --- 工具与三方库 ---
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    requires com.fasterxml.jackson.databind;
    requires io.github.classgraph;

    // --- 数据库与日志 ---
    requires sql2o;
    requires org.xerial.sqlitejdbc;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires cn.hutool;
    requires jdk.management;
    requires org.slf4j;

    // --- 运行时反射权限 (opens) ---
    // 允许 JavaFX 启动和管理主程序
    opens com.atlantafx to javafx.graphics;

    // 允许 JavaFX 反射访问所有功能模块的视图类
    opens com.atlantafx.features.home to javafx.graphics, com.google.common;
    opens com.atlantafx.features.virtuallist to javafx.graphics, com.google.common, io.github.classgraph;
    opens com.atlantafx.features.scaffold to javafx.graphics, com.google.common, io.github.classgraph;
    opens com.atlantafx.components.splash to javafx.graphics, com.google.common;
    // 开放 features 包给全自动扫描器
//    opens com.atlantafx.features to javafx.graphics, com.google.common, io.github.classgraph;

    // 核心管理类需要被 JavaFX 访问（如自定义组件加载）
    opens com.atlantafx.core.theme to javafx.graphics;
    opens com.atlantafx.core.manager to javafx.graphics;
    opens com.atlantafx.core.util to io.github.classgraph;

    // 关键：允许 Sql2o 和 JavaFX Base (TableView) 访问实体模型
    // 必须开放给 sql2o 以便实例化，开放给 javafx.base 以便表格读取属性
    opens com.atlantafx.features.model to javafx.graphics, sql2o, javafx.base, com.fasterxml.jackson.databind;

    // --- 编译时导出权限 (exports) ---
    // 导出模型包供 Jackson 和 Sql2o 使用
    exports com.atlantafx.features.model to com.fasterxml.jackson.databind, sql2o;

    // 导出工具和公共定义
    exports com.atlantafx;
    exports com.atlantafx.util;
    exports com.atlantafx.core.db;
    exports com.atlantafx.core.config;
    exports com.atlantafx.core.constant;
    exports com.atlantafx.core.event;

    // 如果你的自定义组件需要在 FXML 或外部引用，也需要导出
    exports com.atlantafx.components.base;
    exports com.atlantafx.components.functional;
    exports com.atlantafx.core.table to com.fasterxml.jackson.databind, sql2o;
    opens com.atlantafx.core.table to com.fasterxml.jackson.databind, javafx.base, javafx.graphics, sql2o;
    opens com.atlantafx.core.view to com.google.common, io.github.classgraph, javafx.graphics;
}