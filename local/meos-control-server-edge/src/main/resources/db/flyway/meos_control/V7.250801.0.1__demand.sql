# ------------------------- 需求场景表 -------------------------
call drop_table_column('demand_scene', 'across_day');
call drop_table_column('demand_scene', 'across_day');
call drop_table_column('demand_scene_snapshot', 'across_day');

call add_table_column('demand_scene', 'sequence',
                      'alter table demand_scene add sequence int default 0 not null comment ''排序'' after end_time;');
call add_table_column('demand_scene', 'rel_template_id',
                      'alter table demand_scene add rel_template_id bigint not null comment ''关联需求模版ID'' after sequence;');
call add_table_column('demand_scene_snapshot', 'sequence',
                      'alter table demand_scene_snapshot add sequence int default 0 not null comment ''排序'' after end_time;');
call add_table_column('demand_scene_snapshot', 'rel_template_id',
                      'alter table demand_scene_snapshot add rel_template_id bigint not null comment ''关联需求模版ID'' after sequence;');

call drop_table_index('demand_scene', 'demand_scene_idx');
call add_table_index('demand_scene', 'demand_scene_idx',
                     'create index demand_scene_idx on demand_scene (project_id, rel_template_id, scene_name);');
call drop_table_index('demand_scene_snapshot', 'demand_scene_snapshot_idx');
call add_table_index('demand_scene_snapshot', 'demand_scene_snapshot_idx',
                     'create index demand_scene_snapshot_idx on demand_scene_snapshot (project_id, rel_template_id, update_time, valid_end_time, scene_name);');

# ------------------------- 需求配置表 -------------------------
call drop_table_index('demand_config', 'demand_config_group_pk');
call add_table_index('demand_config', 'demand_config_group_idx',
                     'create index demand_config_group_idx on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_group_id);');
call drop_table_index('demand_config', 'demand_config_pk');
call add_table_index('demand_config', 'demand_config_idx',
                     'create index demand_config_idx on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id);');