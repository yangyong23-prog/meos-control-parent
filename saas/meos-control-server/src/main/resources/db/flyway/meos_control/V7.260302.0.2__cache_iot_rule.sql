call drop_table_index('cache_iot_rule', 'idx_2');
call add_table_index('cache_iot_rule', 'idx_3',
                     'create index idx_3 on cache_iot_rule (project_id, custom_key, type, object_id);');
call add_table_index('cache_info_code_template', 'idx_3',
                     'create index idx_3 on cache_info_code_template (project_id, rel_template_id);');