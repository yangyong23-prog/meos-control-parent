-- 索引变更
call drop_table_index('common_config', 'uk_common_config_tag');
call drop_table_index('object_info_code_group', 'object_info_code_group_pk');

call add_table_index('object_info_code_group', 'object_info_code_group_pk',
                     'create index object_info_code_group_pk on object_info_code_group (group_code);');