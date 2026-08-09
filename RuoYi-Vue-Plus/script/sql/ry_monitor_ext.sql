-- ============================================================
-- 系统监控扩展：缓存键监控 + 系统信息（移植自 JunoYi）
-- 目标：art-design-pro 前端 + RuoYi-Vue-Plus 后端
-- 说明：仅新增菜单与按钮权限，不改动 RuoYi 原有「缓存监控」(monitor:cache:list)
--       新增的缓存监控命名为「缓存键监控」，路由 /monitor/cachekey
-- 父菜单：系统监控 1761400000000000002
-- ============================================================

-- ----------------------------
-- 缓存键监控（monitor/cachekey/index）
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time,
                      update_by, update_time, remark)
values (2100600000000000001, '缓存键监控', 1761400000000000002, 4, 'cachekey', 'monitor/cachekey/index', '', 'N', 'Y',
        'C', '0', '0', 'monitor:cachekey:list', 'ri:database-2-line', '', '', 1761000000000000103, 1761100000000000001,
        sysdate(), null, null, '缓存键监控菜单（Redis 键列表/详情/删除）');

-- 缓存键监控按钮权限
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time,
                      update_by, update_time, remark)
values (2100600000000000011, '缓存键查询', 2100600000000000001, 1, '', '', '', 'N', 'Y', 'F', '0', '0',
        'monitor:cachekey:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null,
        '查看缓存键详情');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time,
                      update_by, update_time, remark)
values (2100600000000000012, '缓存键删除', 2100600000000000001, 2, '', '', '', 'N', 'Y', 'F', '0', '0',
        'monitor:cachekey:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null,
        '删除单个/批量缓存键');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time,
                      update_by, update_time, remark)
values (2100600000000000013, '清空缓存', 2100600000000000001, 3, '', '', '', 'N', 'Y', 'F', '0', '0',
        'monitor:cachekey:clear', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null,
        '清空当前库全部缓存');

-- ----------------------------
-- 系统信息（monitor/sysinfo/index）
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, active_menu, ext, create_dept, create_by, create_time,
                      update_by, update_time, remark)
values (2100600000000000002, '系统信息', 1761400000000000002, 8, 'sysinfo', 'monitor/sysinfo/index', '', 'N', 'Y', 'C',
        '0', '0', 'monitor:sysinfo:list', 'ri:computer-line', '', '', 1761000000000000103, 1761100000000000001,
        sysdate(), null, null, '系统信息菜单（服务器/Java/内存/磁盘）');
