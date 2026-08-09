# AtlantaFX Exploration - JavaFX 企业级开发框架

> 基于 JavaFX 26 + AtlantaFX 的现代化桌面应用开发框架，提供完整的页面路由、组件库、状态管理和工具链。

## 项目架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      AppLauncher                            │
│  (启动入口，初始化流程控制)                                   │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     AppContext                              │
│  (全局上下文，静态门面，统一访问入口)                           │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ Navigation-   │ │ Notification- │ │ TaskState-   │
│ Service       │ │ Service       │ │ Service       │
│ (页面导航)     │ │ (消息通知)     │ │ (进度/遮罩)   │
└───────────────┘ └───────────────┘ └───────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│   EventBus    │ │  DIContainer  │ │  ViewFactory  │
│ (事件总线)     │ │ (依赖注入)     │ │ (视图工厂)     │
└───────────────┘ └───────────────┘ └───────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    MainLayout                               │
│  (主布局容器：Sidebar + Header + ContentArea)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
    ┌───────────────┐       ┌─────────────────────┐
    │  SidebarNav   │       │   Page Views        │
    │  (侧边栏菜单)   │       │   (业务页面)        │
    └───────────────┘       └─────────────────────┘
```

### 核心模块说明

| 模块 | 路径 | 职责 |
|------|------|------|
| **components/base** | `src/main/java/com/atlantafx/components/base/` | 封装的 UI 组件，支持链式调用 |
| **components/layout** | `src/main/java/com/atlantafx/components/layout/` | 布局组件（标题栏、侧边栏等） |
| **core/annotation** | `src/main/java/com/atlantafx/core/annotation/` | 页面路由注解 |
| **core/config** | `src/main/java/com/atlantafx/core/config/` | 配置管理 |
| **core/manager** | `src/main/java/com/atlantafx/core/manager/` | 核心服务（导航、DI、模态框等） |
| **core/event** | `src/main/java/com/atlantafx/core/event/` | 事件总线系统 |
| **core/view** | `src/main/java/com/atlantafx/core/view/` | 视图基类和生命周期管理 |
| **features** | `src/main/java/com/atlantafx/features/` | 业务功能模块 |
| **util** | `src/main/java/com/atlantafx/util/` | 工具类集合 |

---

## 开发规约

### 1. 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 类名 | PascalCase | `HomeView`, `UserViewModel` |
| 方法名 | camelCase | `createCard()`, `updateMessage()` |
| 变量名 | camelCase | `welcomeLabel`, `productTable` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY`, `DEFAULT_TIMEOUT` |
| 页面 ID | kebab-case | `"home"`, `"user-list"`, `"system-settings"` |
| 包名 | lowercase | `com.atlantafx.features.home` |

### 2. 代码风格

- **链式调用优先**：使用封装组件提供的链式 API
- **静态导入**：导入 `MaterialDesignX.*` 等图标类时使用静态导入
- **注解位置**：`@Page` 注解必须放在类声明上方，参数分行书写
- **空行规则**：方法之间空 1 行，逻辑块之间空 1 行

### 3. 依赖管理

项目使用 Maven 管理依赖，核心依赖版本：

| 依赖 | 版本 | 说明 |
|------|------|------|
| JavaFX | 26 | UI 框架 |
| AtlantaFX | 2.1.0 | 样式框架 |
| Ikonli | 12.4.0 | 图标库 |
| Jackson | 2.21.1 | JSON 处理 |
| SQLite | 3.51.2.0 | 嵌入式数据库 |
| Logback | 1.5.32 | 日志框架 |

---

## 新手开发指南

### 快速创建一个新页面

#### 步骤 1：创建页面类

在 `src/main/java/com/atlantafx/features/` 下创建新文件夹，例如 `demo/`，然后创建页面类：

```java
package com.atlantafx.features.demo;

import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.view.BaseView;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXVBox;
import atlantafx.base.theme.Styles;

@Page(
        id = "my-demo",           // 页面唯一标识
        name = "我的演示",         // 菜单显示名称
        title = "演示页面标题",     // 页面标题（Header显示）
        icon = "mdi2d-dashboard", // Ikonli 图标代码
        order = 10,               // 排序权重（越小越靠前）
        level = 1,                // 菜单层级（1-3级）
        isDefault = false,        // 是否为默认首页
        lazyLoad = true           // 是否懒加载
)
public class MyDemoView extends BaseView {

    public MyDemoView() {
        // 使用链式 API 创建 UI
        FXVBox content = FXVBox.create(20)
                .add(FXLabel.create("欢迎来到演示页面").stylesClass(Styles.TITLE_2))
                .add(FXLabel.create("这是一个示例页面"));

        setContent(content);
    }
}
```

#### 步骤 2：创建 ViewModel（可选）

如果页面需要状态管理，创建对应的 ViewModel：

```java
package com.atlantafx.features.demo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MyDemoViewModel {
    
    private final StringProperty message = new SimpleStringProperty("初始消息");
    
    public StringProperty messageProperty() {
        return message;
    }
    
    public void updateMessage(String newMessage) {
        message.set(newMessage);
    }
}
```

#### 步骤 3：在页面中使用 ViewModel

通过构造函数注入 ViewModel（DIContainer 自动解析）：

```java
public class MyDemoView extends BaseView {
    
    public MyDemoView(MyDemoViewModel viewModel) {
        FXLabel messageLabel = FXLabel.create()
            .bind(viewModel.messageProperty());
        
        FXVBox content = FXVBox.create(20)
            .add(FXLabel.create("欢迎来到演示页面").styles(Styles.TITLE_2))
            .add(messageLabel);
        
        setContent(content);
    }
}
```

---

### 核心 API 速查

#### 1. 页面导航

```java
// 从菜单导航（不显示返回按钮）
AppContext.navigateTo("page-id");

// 从页面内导航（显示返回按钮）
AppContext.navigateFromPage("page-id");

// 返回上一页
AppContext.navigateBackPage();
```

#### 2. 显示通知

```java
import com.atlantafx.core.constant.NotificationLevel;

// 成功通知
AppContext.showNotification("操作成功", NotificationLevel.SUCCESS);

// 错误通知
AppContext.showNotification("操作失败", NotificationLevel.ERROR);

// 警告通知
AppContext.showNotification("请注意", NotificationLevel.WARNING);

// 信息通知
AppContext.showNotification("提示信息", NotificationLevel.INFO);
```

#### 3. 显示模态对话框

```java
import com.atlantafx.components.base.FXCustomDialog;
import com.atlantafx.components.base.FXButton;

FXCustomDialog dialog = new FXCustomDialog("对话框标题");
dialog.

setBody(FXLabel.create("对话框内容"));

Button okBtn = FXButton.create("确定").accent().onAction(e -> dialog.getCloseButton().fire());
dialog.

addAction(okBtn);

ModalManager.

show(dialog);
```

#### 4. 全局遮罩层

```java
// 显示加载遮罩
AppContext.startLoading("正在处理...");

// 停止加载遮罩
AppContext.stopLoading();

// 执行带遮罩的任务
AppContext.runTask("开始处理", "处理完成", () -> {
    // 耗时操作
});
```

#### 5. 事件总线

```java
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.AppEvent;

// 定义自定义事件
public class MyEvent extends AppEvent {
    private final String data;
    
    public MyEvent(String data) {
        this.data = data;
    }
    
    public String getData() {
        return data;
    }
}

// 订阅事件（同步）
EventBus.subscribe(MyEvent.class, event -> {
    System.out.println("收到事件: " + event.getData());
});

// 订阅事件（异步）
EventBus.subscribe(MyEvent.class, event -> {
    // 异步处理
}, true, 0);

// 发布事件
EventBus.publish(new MyEvent("hello"));
```

---

### 组件使用示例

#### 创建按钮

```java
import com.atlantafx.components.base.FXButton;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;

// 创建带图标的按钮
FXButton btn = FXButton.create("点击我")
    .icon(MaterialDesignA.ACCOUNT_CHECK)
    .accent()                    // 强调色样式
    .outline()                   // 轮廓样式
    .onAction(e -> System.out.println("点击"));

// 创建危险按钮
FXButton deleteBtn = FXButton.create("删除")
    .danger()
    .onAction(e -> confirmDelete());
```

#### 创建布局容器

```java
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.components.base.FXHBox;
import com.atlantafx.components.base.FXLabel;
import javafx.geometry.Pos;

// 创建垂直布局
FXVBox vbox = FXVBox.create(10)      // 间距 10px
    .align(Pos.TOP_CENTER)           // 对齐方式
    .padding(20)                     // 内边距
    .add(FXLabel.create("第一行"))
    .add(FXLabel.create("第二行"));

// 创建水平布局
FXHBox hbox = FXHBox.create(15)
    .add(FXLabel.create("左"))
    .add(FXLabel.create("中"))
    .add(FXLabel.create("右"));
```

---

### 页面生命周期

```java
public class MyView extends BaseView {
    
    @Override
    public void onCreated() {
        // 页面创建时调用（构造函数后）
        // 适合初始化成员变量
    }
    
    @Override
    public void onInit() {
        // 页面初始化时调用（节点树构建完成后）
        // 适合执行需要场景或父节点的初始化操作
    }
    
    @Override
    public void onShow() {
        // 页面显示时调用（每次切换到该页面都会触发）
        // 适合刷新数据或重置状态
    }
    
    @Override
    public void onHide() {
        // 页面隐藏时调用（每次离开该页面都会触发）
        // 适合保存状态或暂停操作
    }
    
    @Override
    public void onDispose() {
        // 页面销毁时调用（从缓存中移除时）
        // 适合释放资源、取消订阅等清理操作
    }
}
```

---

## 运行项目

### 开发运行

```bash
cd atlantafx-exploration
mvn javafx:run
```

### 打包构建

```bash
# 仅打包（Windows）
mvn package

# 清理并打包
mvn clean package
```

---

## 目录结构详解

```
src/main/java/com/atlantafx/
├── components/           # UI 组件层
│   ├── base/            # 基础组件封装（FXButton, FXLabel 等）
│   ├── layout/          # 布局组件（标题栏、侧边栏等）
│   ├── splash/          # 启动画面组件
│   └── theme/           # 主题相关组件
├── core/                # 核心框架层
│   ├── annotation/      # 注解定义（@Page）
│   ├── config/          # 配置管理（ConfigStore, AppState）
│   ├── constant/        # 常量定义
│   ├── db/              # 数据库访问层
│   ├── error/           # 全局异常处理
│   ├── event/           # 事件总线系统
│   ├── manager/         # 核心服务管理
│   ├── service/         # 业务服务
│   ├── table/           # 数据库表模型
│   ├── theme/           # 主题管理
│   └── view/            # 视图基类和工厂
├── features/            # 业务功能层
│   ├── home/            # 首页模块
│   ├── settings/        # 设置模块
│   ├── demo/            # 演示模块
│   └── ...              # 其他业务模块
├── util/                # 工具类
├── AppContext.java      # 全局上下文
├── AppLauncher.java     # 启动入口
└── Main.java            # 主类
```

---

## 核心设计模式

1. **MVVM**：View-ViewModel 分离，数据绑定驱动 UI
2. **服务定位器**：DIContainer 提供依赖注入
3. **事件驱动**：EventBus 实现组件解耦
4. **工厂模式**：ViewFactory 动态创建页面
5. **门面模式**：AppContext 统一访问入口

---

## 扩展开发建议

- **新增组件**：在 `components/base/` 下创建新组件，实现 `IFXNode` 接口
- **新增页面**：在 `features/` 下创建模块，使用 `@Page` 注解标记
- **新增服务**：在 `core/manager/` 或 `core/service/` 下创建服务类
- **新增事件**：继承 `AppEvent` 类，通过 `EventBus` 发布/订阅

---

## 版本历史

- **v1.0**：基础框架搭建，包含页面路由、组件库、事件总线