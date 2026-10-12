# ------------------------- Table demand_config_lighting_result -------------------------
call drop_table_column('demand_config_lighting_result', 'building_id');
call drop_table_column('demand_config_lighting_result', 'floor_id');
call drop_table_column('demand_config_lighting_result', 'space_id');
call drop_table_column('demand_config_lighting_result', 'space_func_type');