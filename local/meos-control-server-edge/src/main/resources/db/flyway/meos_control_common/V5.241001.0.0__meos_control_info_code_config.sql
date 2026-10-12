# ------------------------- object_info_code_template_type -------------------------
call add_table_column('object_info_code_template_type', 'running_monitor_second_type',
                      'alter table `object_info_code_template_type` add `running_monitor_second_type` varchar(32) null comment ''运行监测二级类型'' after `running_monitor_type`;');
call add_table_index('object_info_code_template_type', 'object_info_code_template_type_idx2',
                     'create index `object_info_code_template_type_idx2` on `object_info_code_template_type` (`running_monitor_type`, `running_monitor_second_type`, `template_type`, `object_type`, `class_code`);');

delete from `object_info_code_config` where `rel_template_type_id` in (select `id` from `object_info_code_template_type` where `running_monitor_type` = 'VENTILATION' and `template_type` = 'TABLE_LIST');
delete from `object_info_code_template_type` where `running_monitor_type` = 'VENTILATION' and `template_type` = 'TABLE_LIST';

update `object_info_code_template_type` set `running_monitor_second_type` = 'AIR_TERMINAL' where running_monitor_type = 'AIR_TERMINAL';
update `object_info_code_template_type` set `running_monitor_second_type` = 'SECURITY' where running_monitor_type = 'SECURITY';
update `object_info_code_template_type` set `running_monitor_second_type` = 'Robot' where running_monitor_type = 'Robot';
update `object_info_code_template_type` set `running_monitor_second_type` = 'ELEVATOR' where running_monitor_type = 'ELEVATOR';
update `object_info_code_template_type` set `running_monitor_second_type` = 'PASSENGER_FLOW' where running_monitor_type = 'PASSENGER_FLOW';
update `object_info_code_template_type` set `running_monitor_second_type` = 'PARKING' where running_monitor_type = 'PARKING';
update `object_info_code_template_type` set `running_monitor_second_type` = 'KITCHEN_RANGE_HOOD' where running_monitor_type = 'KITCHEN_RANGE_HOOD';