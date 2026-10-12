--  功能权限
SELECT saas_resourceCreate('xPublicLighting', '公共照明', 'lightxplantrunmonitor');
SELECT saas_resourceCreate('xNightViewLighting', '夜景照明', 'lightxplantrunmonitor');

DELETE FROM t_resource where c_resource_id in ('xprunairterminaleqcontrol', 'xprunairterminaltocoolsys');

SELECT saas_resourceCreate('xprunairterminalhydraulicbalance', '末端水力平衡分析', 'airterminalxplantrunmonitor');
SELECT saas_resourceCreate('xprunairterminal3dmodel', '末端楼层三维模型', 'airterminalxplantrunmonitor');
SELECT saas_resourceCreate('xprunairterminaleqcontrol', '末端-运行控制', 'airterminalxplantrunmonitor');
SELECT saas_resourceCreate('xprunairterminaltocoolsys', '末端关联冷站信息', 'airterminalxplantrunmonitor');
SELECT saas_resourceCreate('xplantenv3dmodel', '三维模型展示', 'xplantindoorenv');