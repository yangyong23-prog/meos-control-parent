# ------------------------- object_info_code_config -------------------------
call edit_table_column('object_info_code_config', 'rel_info_code_config_id',
                       'alter table `object_info_code_config` change `rel_info_code_config_id` `rel_info_code_config_ids` json null comment ''关联信息点配置id'';');
update object_info_code_config set rel_info_code_config_ids = json_array(cast(rel_info_code_config_ids as char)) where rel_info_code_config_ids is not null;