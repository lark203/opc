package com.atlantafx.core.manager;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.SkeletonPlaceholder;
import com.atlantafx.core.view.AsyncView;
import com.atlantafx.core.view.BaseView;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewManager {

    private static final Logger log = LoggerFactory.getLogger(ViewManager.class);
    // 右侧 - 中心内容区 - 页面
    private static StackPane contentArea;
    // 记录当前的后台服务
    private static Service<Void> currentService;

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    /**
     * 切换页面（带方向感知动画）
     *
     * @param view 新页面视图
     */
    public static void switchPage(Node view) {
        // 获取目标页面 ID（从 AppContext 中获取当前导航的页面 ID）
        boolean isForward = AppContext.isForwardNavigation();

        // 1. 如果有正在运行的后台加载任务，先取消它
        if (currentService != null && currentService.isRunning()) {
            currentService.cancel();
            currentService = null;
        }

        if (!contentArea.getChildren().isEmpty()) {
            contentArea.getChildren().clear();
        }

        if (view instanceof AsyncView asyncView && view instanceof BaseView baseView) {
            // 检查数据是否已经加载完成
            if (baseView.isDataLoaded()) {
                // 数据已加载，直接显示页面，跳过骨架屏
                log.debug("页面数据已缓存，直接显示: {}", view.getClass().getSimpleName());
                PageTransitionAnimator.animateSwitch(contentArea, view, isForward);
            } else {
                // 数据未加载，显示骨架屏并后台加载
                // 2. 获取该页面自定义的骨架屏或使用默认
                Node skeleton = asyncView.getSkeleton();
                if (skeleton == null) {
                    skeleton = new SkeletonPlaceholder();
                }

                // 3. 先切入骨架屏动画（使用方向感知动画）
                PageTransitionAnimator.animateSwitch(contentArea, skeleton, isForward);

                // 4. 使用 Service 管理后台任务，方便取消
                currentService = new Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<>() {
                            @Override
                            protected Void call() throws Exception {
                                // 在执行前检查是否已被取消
                                if (isCancelled()) return null;

                                asyncView.loadData();
                                return null;
                            }
                        };
                    }
                };

                currentService.setOnSucceeded(e -> {
                    // 标记数据已加载
                    baseView.setDataLoaded(true);
                    asyncView.setupUI();
                    // 数据加载完成后淡入最终视图
                    PageTransitionAnimator.animateFadeIn(contentArea, view);
                });

                // 如果任务被取消，可以做一些清理工作
                currentService.setOnCancelled(e -> {
                    log.warn("后台任务取消，UI更新被跳过。");
                });

                currentService.start();
            }
        } else {
            // 普通页面直接切换（使用方向感知动画）
            PageTransitionAnimator.animateSwitch(contentArea, view, isForward);
        }
    }

}
