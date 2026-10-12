# ------------------------- 创建唯一索引 -------------------------
call add_table_index('demand_config_lighting', 'ux_instance_id_scene_id',
                     'CREATE UNIQUE INDEX ux_instance_id_scene_id ON demand_config_lighting (demand_instance_id,rel_demand_lighting_scene_id);');