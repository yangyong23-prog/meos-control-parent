# ------------------------- equipment_info_code_config -------------------------
call add_table_column('equipment_info_code_template_type', 'object_type', 'alter table `equipment_info_code_template_type` add `object_type` varchar(64) not null comment ''对象类型'' after `template_type`;');
call add_table_index('equipment_info_code_template_type', 'equipment_info_code_template_type_idx',
                     'create index `equipment_info_code_template_type_idx` on `equipment_info_code_template_type` (`running_monitor_type`, `template_type`, `object_type`, `class_code`);');
# ------------------------- equipment_info_code_config -------------------------
call drop_table_index('equipment_info_code_config', 'equipment_info_code_config_pk');
call add_table_index('equipment_info_code_config', 'equipment_info_code_config_idx',
                     'create index `equipment_info_code_config_idx` on `equipment_info_code_config` (`info_code_type`, `info_code`, `rel_group_code`, `rel_template_type_id`);');