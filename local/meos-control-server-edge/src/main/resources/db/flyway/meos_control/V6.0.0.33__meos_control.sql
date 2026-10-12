# ------------------------- create procedure add_table_index -------------------------
drop procedure if exists add_table_index;
delimiter $$
create procedure add_table_index(in tableName varchar(64), in indexName varchar(64), in createIndexSql text)
begin
    declare indexCount int;

    select count(*)
    into indexCount
    from information_schema.STATISTICS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and INDEX_NAME = indexName;

    if (indexCount = 0) then
        set @createIndexSql = createIndexSql;
        prepare createIndexStmt from @createIndexSql;
        execute createIndexStmt;
        deallocate prepare createIndexStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure add_table_column -------------------------
drop procedure if exists add_table_column;
delimiter $$
create procedure add_table_column(in tableName varchar(64), in columnName varchar(64), in createColumnSql text)
begin
    declare columnCount int;

    select count(*)
    into columnCount
    from information_schema.COLUMNS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and COLUMN_NAME = columnName;

    if (columnCount = 0) then
        set @createColumnSql = createColumnSql;
        prepare createColumnStmt from @createColumnSql;
        execute createColumnStmt;
        deallocate prepare createColumnStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure drop_table_index -------------------------
drop procedure if exists drop_table_index;
delimiter $$
create procedure drop_table_index(in tableName varchar(64), in indexName varchar(64))
begin
    declare indexCount int;

    select count(*)
    into indexCount
    from information_schema.STATISTICS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and INDEX_NAME = indexName;

    if (indexCount > 0) then
        set @dropIndexSql = concat('drop index ', indexName, ' on ', tableName);
        prepare dropIndexStmt from @dropIndexSql;
        execute dropIndexStmt;
        deallocate prepare dropIndexStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure drop_table_column -------------------------
drop procedure if exists drop_table_column;
delimiter $$
create procedure drop_table_column(in tableName varchar(64), in columnName varchar(64))
begin
    declare columnCount int;

    select count(*)
    into columnCount
    from information_schema.COLUMNS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and COLUMN_NAME = columnName;

    if (columnCount > 0) then
        set @dropColumnSql = concat('alter table ', tableName, ' drop column ', columnName);
        prepare dropColumnStmt from @dropColumnSql;
        execute dropColumnStmt;
        deallocate prepare dropColumnStmt;
    end if;

end $$
delimiter ;

# ------------------------- create table demand_template -------------------------
create table if not exists demand_template
(
    id                   bigint auto_increment comment 'ID' primary key,
    project_id           varchar(64)                 not null comment '项目ID',
    demand_type          varchar(32)                 not null comment '需求类型',
    demand_template_type varchar(32)                 not null comment '需求模版类型',
    demand_template_tag  varchar(32)                 not null comment '需求模版标签',
    demand_template_name varchar(64)                 not null comment '模版名称',
    is_initialized       tinyint                     not null comment '是否为预置模版',
    is_enabled           tinyint                     not null comment '是否启用',
    st                   varchar(32) default 'valid' not null comment '状态',
    create_time          datetime                    not null comment '创建时间',
    create_user          varchar(64)                 null comment '创建人',
    update_time          datetime                    not null comment '更新时间',
    update_user          varchar(64)                 null comment '更新人'
) comment '需求模版表';
call add_table_index('demand_template', 'demand_template_pk',
                     'create unique index demand_template_pk on demand_template (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name);');
call add_table_index('demand_template', 'demand_template_idx',
                     'create index demand_template_idx on demand_template (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name, is_initialized, is_enabled, st);');

# ------------------------- create table demand_template_calendar -------------------------
create table if not exists demand_template_calendar
(
    id                     bigint auto_increment comment 'ID' primary key,
    project_id             varchar(64)                 not null comment '项目ID',
    demand_type            varchar(32)                 not null comment '需求类型',
    demand_template_type   varchar(32)                 not null comment '需求模版类型',
    work_date              date                        not null comment '日期',
    is_customized_setting  tinyint                     not null comment '是否自定义配置',
    rel_demand_template_id bigint                      not null comment '关联需求模版ID',
    st                     varchar(32) default 'valid' not null comment '状态',
    create_time            datetime                    not null comment '创建时间',
    create_user            varchar(64)                 null comment '创建人',
    update_time            datetime                    not null comment '更新时间',
    update_user            varchar(64)                 null comment '更新人'
) comment '需求模版日历表';
call add_table_index('demand_template_calendar', 'demand_template_calendar_pk',
                     'create unique index demand_template_calendar_pk on demand_template_calendar (project_id, demand_type, demand_template_type, work_date, st);');
call add_table_index('demand_template_calendar', 'demand_template_calendar_idx',
                     'create index demand_template_calendar_idx on demand_template_calendar (project_id, demand_type, demand_template_type, work_date, is_customized_setting, rel_demand_template_id, st);');

# ------------------------- create table demand_instance_config -------------------------
create table if not exists demand_instance_config
(
    id                   bigint                      not null comment 'ID' primary key,
    project_id           varchar(64)                 not null comment '项目ID',
    demand_type          varchar(32)                 null comment '需求类型',
    demand_template_type varchar(32)                 not null comment '需求模版类型',
    demand_instance_type varchar(32)                 not null comment '需求实例类型',
    demand_instance_id   varchar(64)                 not null comment '需求实例ID',
    config_data          json                        not null comment '配置数据',
    st                   varchar(32) default 'valid' not null comment '状态',
    create_time          datetime                    not null comment '创建时间',
    create_user          varchar(64)                 null comment '创建人',
    update_time          datetime                    not null comment '更新时间',
    update_user          varchar(64)                 null comment '更新人'
) comment '需求实例配置表';
call add_table_index('demand_instance_config', 'demand_instance_config_idx',
                     'create unique index demand_instance_config_idx on demand_instance_config (project_id, demand_type, demand_template_type, demand_instance_type, demand_instance_id, st);');

# ------------------------- create table demand_config_airconditioning -------------------------
create table if not exists demand_config_airconditioning
(
    id                     bigint auto_increment comment 'ID' primary key,
    project_id             varchar(64)                 not null comment '项目ID',
    demand_type            varchar(32)                 not null comment '需求类型',
    demand_template_type   varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag    varchar(32)                 not null comment '需求模版标签',
    demand_instance_type   varchar(32)                 not null comment '需求实例类型',
    demand_instance_id     varchar(64)                 not null comment '需求实例ID',
    is_customized_setting  tinyint                     not null comment '是否自定义配置;当不是自定义配置时则查询引用配置',
    config_data            json                        not null comment '配置数据',
    rel_demand_template_id bigint                      not null comment '关联需求模版ID',
    st                     varchar(32) default 'valid' not null comment '状态',
    create_time            datetime                    not null comment '创建时间',
    create_user            varchar(64)                 null comment '创建人',
    update_time            datetime                    not null comment '更新时间',
    update_user            varchar(64)                 null comment '更新人'
) comment '需求配置-空调表';
call add_table_index('demand_config_airconditioning', 'demand_config_airconditioning_idx',
                     'create index demand_config_airconditioning_idx on demand_config_airconditioning (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, is_customized_setting, rel_demand_template_id, st);');

# ------------------------- create table demand_config_lighting -------------------------
create table if not exists demand_config_lighting
(
    id                           bigint auto_increment comment 'ID' primary key,
    project_id                   varchar(64)                 not null comment '项目ID',
    demand_type                  varchar(32)                 not null comment '需求类型',
    demand_template_type         varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag          varchar(32)                 not null comment '需求模版标签',
    demand_instance_type         varchar(32)                 not null comment '需求实例类型',
    demand_instance_id           varchar(64)                 not null comment '需求实例ID',
    is_customized_setting        tinyint                     not null comment '是否自定义配置;当不是自定义配置时则查询引用配置',
    config_data                  json                        not null comment '配置数据',
    rel_demand_template_id       bigint                      not null comment '关联需求模版ID',
    rel_demand_lighting_scene_id bigint                      not null comment '关联照明场景ID',
    st                           varchar(32) default 'valid' not null comment '状态',
    create_time                  datetime                    not null comment '创建时间',
    create_user                  varchar(64)                 null comment '创建人',
    update_time                  datetime                    not null comment '更新时间',
    update_user                  varchar(64)                 null comment '更新人'
) comment '需求配置-照明表';
call add_table_index('demand_config_lighting', 'demand_config_lighting_idx',
                     'create index demand_config_lighting_idx on demand_config_lighting (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, is_customized_setting, rel_demand_template_id, rel_demand_lighting_scene_id, st);');

# ------------------------- create table demand_config_lighting_scene -------------------------
create table if not exists demand_config_lighting_scene
(
    id                     bigint auto_increment comment 'ID' primary key,
    project_id             varchar(64)                 not null comment '项目ID',
    demand_type            varchar(32)                 not null comment '需求类型',
    demand_template_type   varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag    varchar(32)                 not null comment '需求模版标签',
    begin_time             json                        not null comment '开始时间',
    end_time               json                        not null comment '结束时间',
    scene_name             varchar(64)                 not null comment '场景名称',
    st                     varchar(32) default 'valid' not null comment '状态',
    rel_demand_template_id bigint                      not null comment '关联需求模版ID',
    create_time            datetime                    not null comment '创建时间',
    create_user            varchar(64)                 null comment '创建人',
    update_time            datetime                    not null comment '更新时间',
    update_user            varchar(64)                 null comment '更新人'
) comment '需求配置-照明场景表';
call add_table_index('demand_config_lighting_scene', 'demand_config_lighting_scene_idx',
                     'create index demand_config_lighting_scene_idx on demand_config_lighting_scene (project_id, demand_type, demand_template_type, demand_template_Tag, scene_name, rel_demand_template_id, st);');

# ------------------------- create table demand_config_lighting_result -------------------------
create table if not exists demand_config_lighting_result
(
    id                           bigint auto_increment comment 'ID' primary key,
    project_id                   varchar(64)                 not null comment '项目ID',
    circuit_id                   varchar(64)                 not null comment '照明回路ID',
    circuit_func_type            varchar(32)                 not null comment '照明回路功能类型',
    circuit_lamp_func_type       varchar(32)                 null comment '照明回路灯具功能类型',
    config_data                  json                        not null comment '配置数据',
    config_source                varchar(32)                 not null comment '配置来源',
    rel_demand_template_id       bigint                      not null comment '关联需求模版ID',
    rel_demand_lighting_scene_id bigint                      not null comment '关联照明场景ID',
    st                           varchar(32) default 'valid' not null comment '状态',
    create_time                  datetime                    not null comment '创建时间',
    create_user                  varchar(64)                 null comment '创建人',
    update_time                  datetime                    not null comment '更新时间',
    update_user                  varchar(64)                 null comment '更新人'
) comment '需求管理-照明结果表';
call add_table_index('demand_config_lighting_result', 'demand_config_lighting_result_idx',
                     'create index demand_config_lighting_result_idx on demand_config_lighting_result (project_id, circuit_id, circuit_func_type, circuit_lamp_func_type, rel_demand_template_id, rel_demand_lighting_scene_id, st);');

# ------------------------- create table demand_config_ventilation -------------------------
create table if not exists demand_config_ventilation
(
    id                     bigint auto_increment comment 'ID' primary key,
    project_id             varchar(64)                 not null comment '项目ID',
    demand_type            varchar(32)                 not null comment '需求类型',
    demand_template_type   varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag    varchar(32)                 not null comment '需求模版标签',
    demand_instance_type   varchar(32)                 not null comment '需求实例类型',
    demand_instance_id     varchar(64)                 not null comment '需求实例ID',
    is_customized_setting  tinyint                     not null comment '是否自定义配置;当不是自定义配置时则查询引用配置',
    config_data            json                        not null comment '配置数据',
    rel_demand_template_id bigint                      not null comment '关联需求模版ID',
    st                     varchar(32) default 'valid' not null comment '状态',
    create_time            datetime                    not null comment '创建时间',
    create_user            varchar(64)                 null comment '创建人',
    update_time            datetime                    not null comment '更新时间',
    update_user            varchar(64)                 null comment '更新人'
) comment '需求配置-通风表';
call add_table_index('demand_config_ventilation', 'demand_config_ventilation_idx',
                     'create index demand_config_ventilation_idx on demand_config_ventilation (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, is_customized_setting, rel_demand_template_id, st);');