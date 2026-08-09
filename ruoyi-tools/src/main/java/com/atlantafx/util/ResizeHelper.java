package com.atlantafx.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * 针对无边框窗口 (StageStyle.UNDECORATED) 的缩放助手
 * 鼠标拖拽窗体进行缩放
 */
public class ResizeHelper {

    public static void addResizeListener(Stage stage) {
        ResizeListener listener = new ResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, listener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_EXITED, listener);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, listener);
    }

    static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private final int border = 5; // 触发缩放的边缘宽度（像素）
        private double startX = 0;
        private double startY = 0;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();

            double mouseEventX = mouseEvent.getSceneX();
            double mouseEventY = mouseEvent.getSceneY();
            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                // 1. 根据鼠标位置判断缩放方向并改变光标
                if (mouseEventX < border && mouseEventY < border) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > sceneWidth - border) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY < border) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);

            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                // 2. 记录起始坐标
                startX = stage.getWidth() - mouseEventX;
                startY = stage.getHeight() - mouseEventY;

            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                // 3. 执行缩放逻辑
                if (!Cursor.DEFAULT.equals(cursorEvent)) {
                    if (!cursorEvent.equals(Cursor.W_RESIZE) && !cursorEvent.equals(Cursor.E_RESIZE)) {
                        double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight() : (border * 2);
                        if (cursorEvent.equals(Cursor.NW_RESIZE) || cursorEvent.equals(Cursor.N_RESIZE) || cursorEvent.equals(Cursor.NE_RESIZE)) {
                            if (stage.getHeight() > minHeight || mouseEvent.getScreenY() < stage.getY()) {
                                stage.setHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                                stage.setY(mouseEvent.getScreenY());
                            }
                        } else {
                            if (stage.getHeight() > minHeight || mouseEvent.getScreenY() + startY > stage.getY() + stage.getHeight()) {
                                stage.setHeight(mouseEvent.getSceneY() + startY);
                            }
                        }
                    }

                    if (!cursorEvent.equals(Cursor.N_RESIZE) && !cursorEvent.equals(Cursor.S_RESIZE)) {
                        double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
                        if (cursorEvent.equals(Cursor.NW_RESIZE) || cursorEvent.equals(Cursor.W_RESIZE) || cursorEvent.equals(Cursor.SW_RESIZE)) {
                            if (stage.getWidth() > minWidth || mouseEvent.getScreenX() < stage.getX()) {
                                stage.setWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                                stage.setX(mouseEvent.getScreenX());
                            }
                        } else {
                            if (stage.getWidth() > minWidth || mouseEvent.getScreenX() + startX > stage.getX() + stage.getWidth()) {
                                stage.setWidth(mouseEvent.getSceneX() + startX);
                            }
                        }
                    }
                    mouseEvent.consume(); // 消耗事件，防止影响其他组件
                }
            }
        }
    }
}