-- 修改信息点编码的长度限制
call add_table_index('rc_running_log', 'project_create_time_index', 'create index `project_create_time_index` on `rc_running_log` (`project_id`, `create_time`);');
call add_table_index('rc_running_log', 'level_category_log_index', 'create index `level_category_log_index` on `rc_running_log` (`third_level_category`, `object_id`);');