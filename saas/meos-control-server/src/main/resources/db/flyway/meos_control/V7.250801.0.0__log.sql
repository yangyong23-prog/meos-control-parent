-- 修改 分类字段
call Pro_Temp_ColumnWork('running_log', 'first_level_category', ' varchar(64)  not null  comment ''一级分类''', 1);
call Pro_Temp_ColumnWork('running_log', 'category',
                         ' second_level_category varchar(64)  not null  comment ''二级分类''  after first_level_category',
                         4);

-- 修改 twin 服务的source_type
update running_log
set source_type = 'TWIN_COMMAND'
where class_code = 'ACCCCP'
  and info_code = 'equipFreqSet';

-- 修改一级分类
update running_log
set first_level_category = 'airConditioningColdSource';

-- 添加索引
call add_table_index('running_log', 'running_log_category_index',
                     'create index running_log_category_index on running_log (project_id, first_level_category, second_level_category) comment ''分类索引'';');