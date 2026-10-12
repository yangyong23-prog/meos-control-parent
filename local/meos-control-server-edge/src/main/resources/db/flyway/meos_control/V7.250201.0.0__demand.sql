# ------------------------- 删除快照表索引 -------------------------
call drop_table_index('demand_config_airconditioning_custom_snapshot', 'nk_airconditioning_custom_instance_template');
call drop_table_index('demand_template_snapshot', 'demand_template_idx');
call drop_table_index('demand_scene_snapshot', 'demand_scene_idx');
call drop_table_index('demand_instance_config_snapshot', 'demand_instance_config_idx');
call drop_table_index('demand_config_snapshot', 'demand_config_scene_pk');

# ------------------------- 重新创建快照表索引 -------------------------
call add_table_index('demand_config_airconditioning_custom_snapshot', 'demand_config_airconditioning_custom_snapshot_idx',
                     'create index demand_config_airconditioning_custom_snapshot_idx on demand_config_airconditioning_custom_snapshot (project_id, demand_instance_id, rel_template_id, update_time, valid_end_time) comment ''模板索引'';');
call add_table_index('demand_template_snapshot', 'demand_template_snapshot_idx',
                     'create index demand_template_snapshot_idx on demand_template_snapshot (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name, is_initialized, is_enabled, st, update_time, valid_end_time);');
call add_table_index('demand_scene_snapshot', 'demand_scene_snapshot_idx',
                     'create index demand_scene_snapshot_idx on demand_scene_snapshot (project_id, demand_template_type, demand_template_Tag, scene_name, update_time, valid_end_time);');
call add_table_index('demand_instance_config_snapshot', 'demand_instance_config_snapshot_idx',
                     'create index demand_instance_config_snapshot_idx on demand_instance_config_snapshot (project_id, demand_type, demand_template_type, demand_instance_type, demand_instance_id, st, update_time, valid_end_time);');
call add_table_index('demand_config_snapshot', 'demand_config_snapshot_idx',
                     'create index demand_config_snapshot_idx on demand_config_snapshot (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id, update_time, valid_end_time);');