# ------------------------- [112297]fix. 解决分组名称长度不够问题 -------------------------
call drop_table_index('demand_config_group', 'demand_config_group_pk');
call add_table_index('demand_config_group', 'demand_config_group_idx',
                     'create index demand_config_group_idx on demand_config_group (project_id, rel_template_Id, rel_scene_id);');
call edit_table_column('demand_config_group', 'group_name',
                       'alter table demand_config_group modify group_name text not null comment ''分组名称'';');

call drop_table_index('demand_config_group_snapshot', 'demand_config_group_snapshot_pk');
call add_table_index('demand_config_group_snapshot', 'demand_config_group_snapshot_idx',
                     'create index demand_config_group_snapshot_idx on demand_config_group_snapshot (project_id, rel_template_Id, rel_scene_id, update_time, valid_end_time);');
call edit_table_column('demand_config_group_snapshot', 'group_name',
                       'alter table demand_config_group_snapshot modify group_name text not null comment ''分组名称'';');