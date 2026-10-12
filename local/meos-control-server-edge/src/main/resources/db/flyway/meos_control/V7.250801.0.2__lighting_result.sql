-- 修改照明结果表结构
call Pro_Temp_ColumnWork('demand_config_lighting_result', 'config_source', '', 3);
call Pro_Temp_ColumnWork('demand_config_lighting_result', 'circuit_func_type', '', 3);
call Pro_Temp_ColumnWork('demand_config_lighting_result', 'circuit_lamp_func_type', '', 3);
call Pro_Temp_ColumnWork('demand_config_lighting_result', 'st', '', 3);

call Pro_Temp_ColumnWork('demand_config_lighting_result', 'config_source_obj_id',
                         ' varchar(128) null comment ''配置来源对象 ID'' after rel_demand_template_id;',
                         1);
call Pro_Temp_ColumnWork('demand_config_lighting_result', 'config_source_type',
                         ' varchar(64) null comment ''配置来源类型'' after config_source_obj_id;',
                         1);

-- 清空当前结果重新生成
delete
from demand_config_lighting_result;