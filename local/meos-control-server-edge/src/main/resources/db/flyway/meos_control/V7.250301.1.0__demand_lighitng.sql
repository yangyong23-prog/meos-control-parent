# ------------------------- [114207]feat. 新增照明需求场景排序字段 -------------------------
call add_table_column('demand_config_lighting_scene', 'sequence',
                      'alter table demand_config_lighting_scene add sequence int default 0 not null comment ''排序'' after scene_name;');