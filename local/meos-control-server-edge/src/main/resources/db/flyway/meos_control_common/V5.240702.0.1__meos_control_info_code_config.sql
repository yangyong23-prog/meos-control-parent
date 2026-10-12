# 设备信息点配置表-新增展示样式字段
call add_table_column('equipment_info_code_config', 'show_Style',
                      'alter table equipment_info_code_config add show_Style varchar(256) null comment ''展示样式'' after tack_value_logic;');