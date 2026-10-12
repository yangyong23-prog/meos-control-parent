-- 多集团创建pc端三级菜单（pc端，且默认项目级权限）
-- call saas_functionCreate('二级菜单名称','功能菜单id','功能菜单名称','功能菜单url');

-- 多集团创建移动端三级菜单（移动端，且默认项目级权限）
-- call saas_appFunctionCreate('二级菜单名称','功能菜单id','功能菜单名称','功能菜单url');

-- 多集团创建pc端集团级三级菜单
-- call saas_groupFunctionCreate('二级菜单名称','功能菜单id','功能菜单名称','功能菜单url');

-- 添加菜单  ----------------------
call saas_functionCreate('不倒翁控制', 'xplantrunmonitor', '运行监测',
                         '${persagy_site_url}/meos-control-web/');

--  功能权限
SELECT saas_resourceCreate('xpruncoolsystem', '冷站系统', 'xplantrunmonitor');
SELECT saas_resourceCreate('xpruncsmanualAutoSet', '冷站-设备手自动设置', 'xplantrunmonitor');
SELECT saas_resourceCreate('xpruncseqcontrol', '冷站-运行控制', 'xplantrunmonitor');

