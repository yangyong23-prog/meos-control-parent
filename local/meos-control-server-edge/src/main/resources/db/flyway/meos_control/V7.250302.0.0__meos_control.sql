-- 索引变更
call drop_table_index('demand_template', 'demand_template_pk');
call add_table_index('demand_template', 'demand_template_pk',
                     'create index demand_template_pk
    on demand_template (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name);');

call drop_table_index('demand_template_calendar', 'demand_template_calendar_pk');
call add_table_index('demand_template_calendar', 'demand_template_calendar_pk',
                     'create index demand_template_calendar_pk
    on demand_template_calendar (project_id, demand_type, demand_template_type, work_date, st);');

call drop_table_index('demand_instance_config', 'demand_instance_config_idx');
call add_table_index('demand_instance_config', 'demand_instance_config_idx',
                     'create index demand_instance_config_idx
    on demand_instance_config (project_id, demand_type, demand_template_type, demand_instance_type, demand_instance_id,
                               st);');

call drop_table_index('demand_config', 'demand_config_group_pk');
call add_table_index('demand_config', 'demand_config_group_pk',
                     'create index demand_config_group_pk
    on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_group_id);');

call drop_table_index('demand_config', 'demand_config_group_scene_pk');
call add_table_index('demand_config', 'demand_config_group_scene_pk',
                     'create index demand_config_group_scene_pk
    on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id, rel_group_id);');

call drop_table_index('demand_config', 'demand_config_pk');
call add_table_index('demand_config', 'demand_config_pk',
                     'create index demand_config_pk
    on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id);');

call drop_table_index('demand_config', 'demand_config_scene_pk');
call add_table_index('demand_config', 'demand_config_scene_pk',
                     'create index demand_config_scene_pk
    on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id);');