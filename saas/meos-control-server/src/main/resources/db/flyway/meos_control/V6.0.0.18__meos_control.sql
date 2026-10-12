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
call add_table_index('demand_instance_config', 'demand_instance_config_pk',
                     'create unique index demand_instance_config_pk on demand_instance_config (project_id, demand_type, demand_template_type, demand_instance_type, demand_instance_id, st);');

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
                     'create unique index demand_template_pk on demand_template (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name, st);');
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
call add_table_index('demand_config_airconditioning', 'demand_config_airconditioning_pk',
                     'create unique index demand_config_airconditioning_pk on demand_config_airconditioning (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, st);');
call add_table_index('demand_config_airconditioning', 'demand_config_airconditioning_idx',
                     'create index demand_config_airconditioning_idx on demand_config_airconditioning (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, is_customized_setting, rel_demand_template_id, st);');

# ------------------------- create table demand_config_airconditioning_history -------------------------
create table if not exists demand_config_airconditioning_history
(
    id                         bigint auto_increment comment 'ID' primary key,
    project_id                 varchar(64)                 not null comment '项目ID',
    demand_type                varchar(32)                 not null comment '需求类型',
    demand_template_type       varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag        varchar(32)                 not null comment '需求模版标签',
    demand_instance_type       varchar(32)                 not null comment '需求实例类型',
    demand_instance_id         varchar(64)                 not null comment '需求实例ID',
    demand_instance_name       varchar(64)                 null comment '需求实例名称',
    demand_instance_local_name varchar(64)                 null comment '需求实例本地名称',
    parent_demand_instance_id  varchar(64)                 null comment '上级需求实例ID',
    config_date                date                        not null comment '配置日期',
    is_custom_control_space    tinyint                     not null comment '是否为自定义管控空间;仅 [demand_instance_type] 为 [SPACE] 时可为 [1]',
    is_shielded                tinyint                     not null comment '是否被屏蔽;仅 [demand_instance_type] 为 [SPACE] 时可为 [1]',
    is_customized_setting      tinyint                     not null comment '是否自定义配置',
    config_data                json                        not null comment '配置数据',
    rel_system_objects         json                        not null comment '关联系统对象',
    st                         varchar(32) default 'valid' not null comment '状态',
    create_time                datetime                    not null comment '创建时间',
    create_user                varchar(64)                 null comment '创建人',
    update_time                datetime                    not null comment '更新时间',
    update_user                varchar(64)                 null comment '更新人'
) comment '需求配置-空调历史表';
call add_table_index('demand_config_airconditioning_history', 'demand_config_airconditioning_history_pk',
                     'create unique index demand_config_airconditioning_history_pk on demand_config_airconditioning_history (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, config_date, st);');
call add_table_index('demand_config_airconditioning_history', 'demand_config_airconditioning_history_idx',
                     'create index demand_config_airconditioning_history_idx on demand_config_airconditioning_history (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, parent_demand_instance_id, config_date, is_custom_control_space, is_shielded, is_customized_setting, st);');

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
call add_table_index('demand_config_ventilation', 'demand_config_ventilation_pk',
                     'create unique index demand_config_ventilation_pk on demand_config_ventilation (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, st);');
call add_table_index('demand_config_ventilation', 'demand_config_ventilation_idx',
                     'create index demand_config_ventilation_idx on demand_config_ventilation (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, is_customized_setting, rel_demand_template_id, st);');

# ------------------------- create table demand_config_ventilation_history -------------------------
create table if not exists demand_config_ventilation_history
(
    id                         bigint auto_increment comment 'ID' primary key,
    project_id                 varchar(64)                 not null comment '项目ID',
    demand_type                varchar(32)                 not null comment '需求类型',
    demand_template_type       varchar(32)                 not null comment '需求模版类型',
    demand_template_Tag        varchar(32)                 not null comment '需求模版标签',
    demand_instance_type       varchar(32)                 not null comment '需求实例类型',
    demand_instance_id         varchar(64)                 not null comment '需求实例ID',
    demand_instance_name       varchar(64)                 null comment '需求实例名称',
    demand_instance_local_name varchar(64)                 null comment '需求实例本地名称',
    parent_demand_instance_id  varchar(64)                 null comment '上级需求实例ID',
    config_date                date                        not null comment '配置日期',
    is_customized_setting      tinyint                     not null comment '是否自定义配置;当不是自定义配置时则查询引用配置',
    config_data                json                        not null comment '配置数据',
    rel_ventilation_objects    json                        not null comment '关联通风对象',
    st                         varchar(32) default 'valid' not null comment '状态',
    create_time                datetime                    not null comment '创建时间',
    create_user                varchar(64)                 null comment '创建人',
    update_time                datetime                    not null comment '更新时间',
    update_user                varchar(64)                 null comment '更新人'
) comment '需求配置-通风历史表';
call add_table_index('demand_config_ventilation_history', 'demand_config_ventilation_history_pk',
                     'create unique index demand_config_ventilation_history_pk on demand_config_ventilation_history (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, config_date, st);');
call add_table_index('demand_config_ventilation_history', 'demand_config_ventilation_history_idx',
                     'create index demand_config_ventilation_history_idx on demand_config_ventilation_history (project_id, demand_type, demand_template_type, demand_template_Tag, demand_instance_type, demand_instance_id, parent_demand_instance_id, config_date, is_customized_setting, st);');