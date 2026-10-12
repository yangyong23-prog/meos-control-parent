create database if not exists `meos_control`;
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

# ------------------------- create table equipment_info_code_config -------------------------
create table if not exists `equipment_info_code_config`
(
    `id`                      bigint      not null comment 'id',
    `info_code`               varchar(64) not null comment '信息点编码',
    `info_code_type`          varchar(32) not null comment '信息点类型',
    `tack_value_logic`        json        default null comment '取值表达式',
    `sequence`                int         not null comment '信息点顺序',
    `rel_group_code`          varchar(64) not null comment '分组编码',
    `rel_info_code_config_id` bigint      default null comment '关联信息点配置id',
    `rel_template_type_id`    bigint      not null comment '关联模版类型id',
    `create_time`             datetime    not null comment '创建时间',
    `create_user`             varchar(64) default null comment '创建人',
    `update_time`             datetime    not null comment '更新时间',
    `update_user`             varchar(64) default null comment '更新人',
    primary key (`id`)
) comment ='设备信息点配置表';
call drop_table_index('equipment_info_code_config', 'equipment_info_code_config_pk');
call add_table_index('equipment_info_code_config', 'equipment_info_code_config_pk',
                     'create unique index `equipment_info_code_config_pk` on `equipment_info_code_config` (`info_code`, `rel_group_code`, `rel_template_type_id`);');

# ------------------------- create table equipment_info_code_group -------------------------
create table if not exists `equipment_info_code_group`
(
    `id`            bigint      not null comment 'id',
    `group_code`    varchar(64) not null comment '分组编码',
    `group_name`    varchar(64) not null comment '分组名称',
    `sequence`      int         not null comment '分组排序',
    `rel_parent_id` bigint      default null comment '关联上级分组id',
    `create_time`   datetime    not null comment '创建时间',
    `create_user`   varchar(64) default null comment '创建人',
    `update_time`   datetime    not null comment '更新时间',
    `update_user`   varchar(64) default null comment '更新人',
    primary key (`id`)
) comment ='设备信息点分组配置表';
call drop_table_index('equipment_info_code_group', 'equipment_info_code_group_pk');
call add_table_index('equipment_info_code_group', 'equipment_info_code_group_pk',
                     'create unique index `equipment_info_code_group_pk` on `equipment_info_code_group` (`group_code`);');

# ------------------------- create table equipment_info_code_group -------------------------
create table if not exists `equipment_info_code_template_type`
(
    `id`                   bigint      not null comment 'id',
    `running_monitor_type` varchar(32) not null comment '运行监测类型',
    `template_type`        varchar(32) not null comment '模版类型',
    `class_code`           varchar(64) not null comment '设备类编码',
    `info_code_filter`     varchar(128) default null comment '信息点筛选',
    `create_time`          datetime    not null comment '创建时间',
    `create_user`          varchar(64)  default null comment '创建人',
    `update_time`          datetime    not null comment '更新时间',
    `update_user`          varchar(64)  default null comment '更新人',
    primary key (`id`)
) comment ='设备模版类型配置';
call drop_table_index('equipment_info_code_template_type', 'equipment_info_code_template_type_pk');
call add_table_index('equipment_info_code_template_type', 'equipment_info_code_template_type_pk',
                     'create unique index `equipment_info_code_template_type_pk` on `equipment_info_code_template_type` (`running_monitor_type`, `template_type`, `class_code`, `info_code_filter`);');