-- 添加字段
call Pro_Temp_ColumnWork('instance_config', 'rel_msg', ' varchar(128) null comment ''关联信息'' after config_data;', 1);
call Pro_Temp_ColumnWork('instance_config', 'config_type', ' varchar(64) not null comment ''配置类型: WHITE_LIST白名单, BLACK_LIST黑名单''',
                         2);
