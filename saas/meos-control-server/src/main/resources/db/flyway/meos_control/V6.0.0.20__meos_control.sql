# ------------------------- table demand_instance_config -------------------------
call drop_table_index('demand_instance_config', 'demand_instance_config_pk');
call add_table_index('demand_instance_config', 'demand_instance_config_idx',
                     'create unique index demand_instance_config_idx on demand_instance_config (project_id, demand_type, demand_template_type, demand_instance_type, demand_instance_id, st);');

# ------------------------- table demand_template -------------------------
call add_table_index('demand_template', 'demand_template_pk',
                     'create unique index demand_template_pk on demand_template (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name);');

# ------------------------- table demand_config_airconditioning -------------------------
call drop_table_index('demand_config_airconditioning', 'demand_config_airconditioning_pk');
call drop_table_index('demand_config_airconditioning_history', 'demand_config_airconditioning_history_pk');

# ------------------------- table demand_config_ventilation -------------------------
call drop_table_index('demand_config_ventilation_history', 'demand_config_ventilation_history_pk');
call drop_table_index('demand_config_ventilation', 'demand_config_ventilation_pk');