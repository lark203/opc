package com.atlantafx.features.home;

import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXStackPane;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.view.BaseView;
import javafx.scene.Node;

@Page(id = "home", name = "首页", icon = "mdi2h-home", order = 1, isDefault = true, level = 1, lazyLoad = false)
public class HomeView extends BaseView {


    @Override
    protected void onPageCreated() {

    }

    @Override
    protected Node onPageInit() {
        return FXStackPane.create(FXLabel.create("若依后台管理系统工具集").h2());
    }

    @Override
    protected void onPageDispose() {

    }
}
