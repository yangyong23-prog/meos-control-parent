call edit_table_column('cache_iot_rule', 'running_monitor_second_type',
                       'alter table cache_iot_rule modify running_monitor_second_type varchar(64) null comment ''运行监测二级类型'';');