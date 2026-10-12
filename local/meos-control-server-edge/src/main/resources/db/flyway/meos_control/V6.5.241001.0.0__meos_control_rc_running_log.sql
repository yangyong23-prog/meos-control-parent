-- 添加运行日志表的对象 ID 索引
call add_table_index('rc_running_log', 'rc_running_log_object_id_type_index',
                     'create index rc_running_log_object_id_type_index
    on rc_running_log (object_id, type);');
