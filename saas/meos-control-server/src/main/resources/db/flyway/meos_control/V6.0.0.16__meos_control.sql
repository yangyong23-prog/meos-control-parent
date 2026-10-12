call drop_table_index('demand_lighting_strategy_config', 'demand_lighting_strategy_config_pk');
call add_table_index('demand_lighting_strategy_config', 'demand_lighting_strategy_config_idx',
                     'create index demand_lighting_strategy_config_idx on demand_lighting_strategy_config (project_id, lighting_type, calendar_tag, instance_type, strategy_type, st, building_id, floor_id);')