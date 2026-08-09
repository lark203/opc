-- ============================================================
-- 系统配置功能（参照 custom-admin-ui / continew-admin-ui）
-- 目标：art-design-pro 前端 + RuoYi-Vue-Plus 后端
-- 说明：分阶段追加，本文件包含全部阶段（阶段1~4 + 字典）。
--       首次执行请整文件执行；后续阶段已在对应段落追加。
-- ============================================================

-- ----------------------------
-- 阶段1：选项类配置 sys_option
-- ----------------------------
create table if not exists sys_option
(
    option_id   bigint(20) not null comment '配置ID',
    category    varchar(50)  default '' comment '分类（SITE/PASSWORD/LOGIN/MAIL）',
    code        varchar(100) default '' comment '配置键名',
    `name`        varchar(100) default '' comment '配置名称',
    `value`       longtext comment '配置值',
    `default_value` longtext     DEFAULT NULL   COMMENT '默认值',
    description varchar(500) default '' comment '描述',
    create_dept bigint(20)   default null comment '创建部门',
    create_by   bigint(20)   default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by   bigint(20)   default null comment '更新者',
    update_time datetime comment '更新时间',
    primary key (option_id)
) engine = innodb comment = '系统选项配置表';

-- 初始化-选项配置数据（SITE/PASSWORD/LOGIN/MAIL）
insert into sys_option (option_id, category, code, name, value, default_value, description, create_dept, create_by, create_time,
                        update_by, update_time)
values
-- 网站配置 SITE
(2100000000000000001, 'SITE', 'SITE_LOGO', '网站Logo', '', 'src/assets/images/favicon.ico', '显示在登录页面和系统导航栏的网站图标（建议 .svg 格式）', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000002, 'SITE', 'SITE_FAVICON', '网站图标', '', 'src/assets/images/favicon.ico', '浏览器标签页显示的网站图标（建议 .ico 格式）', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000003, 'SITE', 'SITE_TITLE', '网站标题', '量子科技', '量子科技', '显示在浏览器标题栏和登录界面的系统名称', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000004, 'SITE', 'SITE_DESCRIPTION', '网站描述', '', '一款兼具设计美学与高效开发的后台系统', '用于 SEO 的网站元描述', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000005, 'SITE', 'SITE_COPYRIGHT', '版权信息', '', 'Copyright © 2022 - present', '显示在页面底部的版权声明文本', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000006, 'SITE', 'SITE_BEIAN', '备案号', '', '京ICP备2024088429号-1', '工信部 ICP 备案编号（如：京ICP备12345678号）', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000007, 'SITE', 'SITE_SUB_DESCRIPTION', '网站子描述', '',
 '美观实用的界面，经过视觉优化，确保卓越的用户体验', '网站子描述（登录页副标题，展示在描述下方）', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000008, 'SITE', 'SITE_SHOW_FOOTER', '显示底部备案区域', 'true', 'true',
 '关闭后，登录进入系统将不再在页面底部展示版权与备案号', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
-- 安全配置 PASSWORD
(2100000000000000011, 'PASSWORD', 'PASSWORD_ERROR_LOCK_COUNT', '密码错误锁定次数', '5', '5', '用户密码错误超过此次数将被锁定（0 表示不锁定）',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000012, 'PASSWORD', 'PASSWORD_ERROR_LOCK_MINUTES', '密码错误锁定分钟', '15', '5', '锁定后自动解锁的时间',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000013, 'PASSWORD', 'PASSWORD_EXPIRATION_DAYS', '密码有效期天数', '90', '90', '密码过期后需要修改（0 表示永不过期）',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000014, 'PASSWORD', 'PASSWORD_EXPIRATION_WARNING_DAYS', '密码过期提醒天数', '7', '7',
 '密码过期前多少天开始提醒用户修改', 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000015, 'PASSWORD', 'PASSWORD_REPETITION_TIMES', '密码不可重复次数', '3', '3', '设置后新密码不能与最近N次密码相同',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000016, 'PASSWORD', 'PASSWORD_MIN_LENGTH', '密码最小长度', '8', '8', '用户密码最少字符数', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000017, 'PASSWORD', 'PASSWORD_ALLOW_CONTAIN_USERNAME', '允许包含用户名', '0',
 '0', '密码中是否允许包含用户名', 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000018, 'PASSWORD', 'PASSWORD_REQUIRE_SYMBOLS', '要求特殊字符', '0', '0', '密码中是否要求包含特殊字符（如 @、# 等）',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
-- 导入初始密码（仅用于 Excel 批量导入用户，未单独设置密码时采用）
(2100000000000000019, 'PASSWORD', 'PASSWORD_INIT', '账号初始密码', 'Lp9#kM2xQ7@v', 'Lp9#kM2xQ7@v',
 'Excel 批量导入用户时，未单独设置密码所采用的初始密码（仅用于导入，建议告知用户后及时修改）', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
-- 登录配置 LOGIN
(2100000000000000021, 'LOGIN', 'LOGIN_CAPTCHA_ENABLED', '登录验证码开关', '1', '1', '开启后登录需输入图形验证码',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000022, 'LOGIN', 'LOGIN_CAPTCHA_TYPE', '验证码类型', '1', '1', '选择图形验证码的类型（算术/字符）',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000023, 'LOGIN', 'LOGIN_CAPTCHA_LENGTH', '验证码字符长度', '4', '4', '验证码显示的字符数量',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
(2100000000000000024, 'LOGIN', 'LOGIN_REGISTER_ENABLED', '是否允许注册', '1', '1', '开启后登录页显示注册入口，允许新用户自助注册',
 1761000000000000103, 1761100000000000001, sysdate(), null, null),
-- 邮件配置 MAIL
(2100000000000000031, 'MAIL', 'MAIL_PROTOCOL', '邮件协议', 'smtp', 'smtp', '发送邮件使用的协议类型', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000032, 'MAIL', 'MAIL_HOST', 'SMTP服务器', '', 'smtp.126.com', '邮件发送服务器地址（如 smtp.qq.com）', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000033, 'MAIL', 'MAIL_PORT', 'SMTP端口', '465', '465', '邮件发送服务器端口号', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000034, 'MAIL', 'MAIL_USERNAME', '发件人账号', '', 'charles7c@126.com', '发送邮件的邮箱账号', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000035, 'MAIL', 'MAIL_PASSWORD', '发件人密码', '', '', '邮箱密码或授权码（注意保密）', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000036, 'MAIL', 'MAIL_SSL_ENABLED', 'SSL启用', '1', '1', '是否使用 SSL 加密连接邮件服务器', 1761000000000000103,
 1761100000000000001, sysdate(), null, null),
(2100000000000000037, 'MAIL', 'MAIL_SSL_PORT', 'SSL端口', '465', '465', 'SSL 加密连接使用的端口号', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000038, 'MAIL', 'MAIL_NICKNAME', '发件人昵称', '', '', '显示在邮件中的发件人名称', 1761000000000000103, 1761100000000000001,
 sysdate(), null, null),
(2100000000000000039, 'MAIL', 'MAIL_ENABLED', '邮件功能开启', '0', '0', '是否开启邮件发送功能（数据库配置为准）',
 1761000000000000103, 1761100000000000001,
 sysdate(), null, null);

-- ----------------------------
-- 阶段1：菜单（系统配置 为可见菜单；其下 6 项为隐藏菜单，作为布局页左侧 tab）
-- 说明：客户端配置(RuoYi 自带 SysClient) 不再重复添加
-- 列顺序与 sys_menu 严格一致
-- ----------------------------
insert into sys_menu
values (2100000000000000101, '系统配置', 1761400000000000001, 12, 'sysconfig', 'system/config/system', '', 'N', 'Y',
        'C', '0', '0', 'system:config:list', 'ri:exchange-2-line', '', '', null, 1761000000000000103,
        1761100000000000001, sysdate(), null, null, '系统配置（左侧 tab 切换网站/安全/登录/邮件/短信/存储）');

-- 隐藏子菜单：网站配置 / 安全配置 / 登录配置 / 邮件配置 / 短信配置（visible=1 隐藏，仅作为布局页左侧 tab）
insert into sys_menu
values (2100000000000000110, '网站配置', 2100000000000000101, 1, 'site', 'system/config/site', '', 'N', 'Y', 'C', '1',
        '0', 'system:option:list', 'ri:apps-line', '', '', null, 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '网站配置');
insert into sys_menu
values (2100000000000000111, '安全配置', 2100000000000000101, 2, 'security', 'system/config/security', '', 'N', 'Y',
        'C', '1', '0', 'system:option:list', 'ri:shield-user-line', '', '', null, 1761000000000000103,
        1761100000000000001, sysdate(), null, null, '安全配置');
insert into sys_menu
values (2100000000000000112, '登录配置', 2100000000000000101, 3, 'login', 'system/config/login', '', 'N', 'Y', 'C', '1',
        '0', 'system:option:list', 'ri:lock-2-line', '', '', null, 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '登录配置');
insert into sys_menu
values (2100000000000000113, '邮件配置', 2100000000000000101, 4, 'mail', 'system/config/mail', '', 'N', 'Y', 'C', '1',
        '0', 'system:option:list', 'ri:mail-line', '', '', null, 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '邮件配置');
insert into sys_menu
values (2100000000000000116, '邮件测试发送', 2100000000000000113, 1, '#', '', '', 'N', 'Y', 'F', '0',
        '0', 'system:mail:test', '#', '', '', null, 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '邮件测试发送');
insert into sys_menu
values (2100000000000000114, '短信配置', 2100000000000000101, 5, 'sms', 'system/config/sms', '', 'N', 'Y', 'C', '1',
        '0', 'system:sms:list', 'ri:chat-smile-ai-3-line', '', '', null, 1761000000000000103, 1761100000000000001,
        sysdate(), null, null, '短信配置');
insert into sys_menu
values (2100000000000000215, '存储配置', 2100000000000000101, 6, 'oss', 'system/config/oss', '', 'N', 'Y', 'C', '1',
        '0', 'system:ossConfig:list', 'ri:cloud-line', '', '', null, 1761000000000000103, 1761100000000000001,
        sysdate(), null, null, '存储配置');

-- ----------------------------
-- 阶段2：短信配置 sys_sms_config + 字典 sms_supplier
-- ----------------------------
create table if not exists sys_sms_config
(
    sms_id          bigint(20) not null comment '短信配置ID',
    config_id varchar(64) not null default '' comment 'sms4j配置ID（唯一，作为 blend 注册键）',
    name            varchar(100) default '' comment '名称',
    supplier        varchar(50)  default '' comment '供应商（字典 sms_supplier）',
    access_key      varchar(200) default '' comment 'accessKey',
    secret_key      varchar(200) default '' comment 'secretKey',
    signature       varchar(100) default '' comment '签名',
    template_id     varchar(100) default '' comment '模板ID',
    weight          int(11)      default 1 comment '权重（1-100）',
    retry_interval  int(11)      default 0 comment '重试间隔（秒）',
    max_retries     int(11)      default 0 comment '最大重试次数',
    maximum         int(11)      default 0 comment '最大发送量',
    supplier_config longtext comment '供应商扩展配置（JSON）',
    status          char(1)      default '1' comment '状态（1正常 2停用）',
    is_default      char(1)      default '0' comment '是否默认（1是 0否）',
    sort            int(11)      default 999 comment '排序',
    create_dept     bigint(20)   default null comment '创建部门',
    create_by       bigint(20)   default null comment '创建者',
    create_time     datetime comment '创建时间',
    update_by       bigint(20)   default null comment '更新者',
    update_time     datetime comment '更新时间',
    primary key (sms_id),
    unique key uk_config_id (config_id)
) engine = innodb comment = '短信配置表';

insert into sys_dict_type
values (2101500000000000001, '短信供应商', 'sms_supplier', null, 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '短信供应商列表');
insert into sys_dict_data
values (2101600000000000001, 1, '阿里云', 'alibaba', 'sms_supplier', '', 'primary', 'N', null, 1761000000000000103,
        1761100000000000001, sysdate(), null, null, '阿里云');
insert into sys_dict_data
values (2101600000000000002, 2, '腾讯云', 'tencent', 'sms_supplier', '', 'success', 'N', null, 1761000000000000103,
        1761100000000000001, sysdate(), null, null, '腾讯云');

-- 短信配置按钮权限
insert into sys_menu
values (2100000000000000316, '查询', 2100000000000000114, 1, '#', '', '', 'N', 'Y', 'F', '0',
        '0', 'system:sms:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null,
        null, '查询');
insert into sys_menu
values (2100000000000000317, '新增', 2100000000000000114, 2, '#', '', '', 'N', 'Y', 'F', '0',
        '0', 'system:sms:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '新增');
insert into sys_menu
values (2100000000000000318, '修改', 2100000000000000114, 3, '#', '', '', 'N', 'Y', 'F', '0',
        '0', 'system:sms:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '修改');
insert into sys_menu
values (2100000000000000319, '删除', 2100000000000000114, 4, '#', '', '', 'N', 'Y', 'F', '0',
        '0', 'system:sms:del', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(),
        null, null, '删除');

