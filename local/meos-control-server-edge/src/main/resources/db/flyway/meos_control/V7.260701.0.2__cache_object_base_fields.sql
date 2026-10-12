# ------------------------- add cache object local ID -------------------------
call add_table_column('cache_object', 'local_id',
                      'alter table cache_object add column local_id varchar(64) null comment ''对象本地ID'' after project_id;');
