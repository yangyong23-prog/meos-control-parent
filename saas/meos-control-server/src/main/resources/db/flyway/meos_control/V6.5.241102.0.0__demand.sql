# ------------------------- create procedure init_snapshot_table -------------------------
drop procedure if exists init_snapshot_table;
delimiter $$
create procedure init_snapshot_table(in tableName varchar(64), in snapshotTableName varchar(64))
begin
    set @setRowCount = concat('select count(*) into @dataCount from ', snapshotTableName);
    prepare setRowCountStmt from @setRowCount;
    execute setRowCountStmt;
    deallocate prepare setRowCountStmt;

    select @dataCount;
    if (@dataCount = 0) then
        set @initSql =
                concat('insert into ', snapshotTableName, ' select *, ''9999-12-31 23:59:59'' as valid_end_time from ',
                       tableName);
        prepare initStmt from @initSql;
        execute initStmt;
        deallocate prepare initStmt;
    end if;

end $$
delimiter ;

# ------------------------- create table demand_config_airconditioning_custom -------------------------
create table if not exists demand_config_airconditioning_custom
(
    id                   bigint auto_increment comment 'ID' primary key,
    project_id           varchar(64)                        null comment '项目ID',
    demand_instance_type varchar(32)                        null comment '需求实例类型',
    demand_instance_id   varchar(32)                        null comment '需求实例ID',
    config_data          json                               null comment '配置数据',
    rel_template_id      varchar(200)                       null comment '关联需求模版ID',
    rel_scene_id         varchar(200)                       null comment '关联需求场景ID',
    create_time          datetime default CURRENT_TIMESTAMP null comment '创建时间',
    create_user          varchar(64)                        null comment '创建人',
    update_time          datetime default CURRENT_TIMESTAMP null comment '更新时间',
    update_user          varchar(64)                        null comment '更新人',
    key nk_airconditioning_custom_instance_template (project_id, demand_instance_id, rel_template_id) comment '模板索引'
) comment '需求管理-空调需求-定制功能表';

# ------------------------- create table demand_template_snapshot -------------------------
create table if not exists demand_template_snapshot
(
    id                   bigint auto_increment comment 'ID',
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
    update_user          varchar(64)                 null comment '更新人',
    valid_end_time       datetime                    not null comment '生效截止时间',
    primary key (id, valid_end_time),
    key demand_template_idx (project_id, demand_type, demand_template_type, demand_template_tag, demand_template_name,
                             is_initialized, is_enabled, st, valid_end_time)
) comment '需求模版快照表';
call init_snapshot_table('demand_template', 'demand_template_snapshot');
# ------------------------- create trigger demand_template_insert_snapshot -------------------------
drop trigger if exists demand_template_insert_snapshot;
delimiter $$
create trigger demand_template_insert_snapshot
    after insert
    on demand_template
    for each row
begin
    insert into demand_template_snapshot (id, project_id, demand_type, demand_template_type, demand_template_tag,
                                          demand_template_name, is_initialized, is_enabled, st, create_time,
                                          create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_template_tag,
               NEW.demand_template_name, NEW.is_initialized, NEW.is_enabled, NEW.st, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_template_update_snapshot -------------------------
drop trigger if exists demand_template_update_snapshot;
delimiter $$
create trigger demand_template_update_snapshot
    after update
    on demand_template
    for each row
begin
    update demand_template_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_template_snapshot (id, project_id, demand_type, demand_template_type, demand_template_tag,
                                          demand_template_name, is_initialized, is_enabled, st, create_time,
                                          create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_template_tag,
               NEW.demand_template_name, NEW.is_initialized, NEW.is_enabled, NEW.st, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_template_delete_snapshot -------------------------
drop trigger if exists demand_template_delete_snapshot;
delimiter $$
create trigger demand_template_delete_snapshot
    after delete
    on demand_template
    for each row
begin
    update demand_template_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create table demand_scene_snapshot -------------------------
create table if not exists demand_scene_snapshot
(
    id                   bigint auto_increment comment 'ID',
    project_id           varchar(255) not null comment '项目ID',
    demand_template_type varchar(32)  not null comment '需求模版类型',
    demand_template_Tag  varchar(32)  not null comment '需求模版标签',
    scene_name           varchar(255) not null comment '场景名称',
    begin_time           json         not null comment '开始时间',
    end_time             json         not null comment '结束时间',
    across_day           tinyint      not null comment '是否跨天',
    create_time          datetime     not null comment '创建时间',
    create_user          varchar(255) null comment '创建人',
    update_time          datetime     not null comment '更新时间',
    update_user          varchar(255) null comment '更新人',
    valid_end_time       datetime     not null comment '生效截止日期',
    primary key (id, valid_end_time),
    key demand_scene_idx (project_id, demand_template_type, demand_template_Tag, scene_name, valid_end_time)
) comment '需求场景快照表';
call init_snapshot_table('demand_scene', 'demand_scene_snapshot');
# ------------------------- create trigger demand_scene_insert_snapshot -------------------------
drop trigger if exists demand_scene_insert_snapshot;
delimiter $$
create trigger demand_scene_insert_snapshot
    after insert
    on demand_scene
    for each row
begin
    insert into demand_scene_snapshot (id, project_id, demand_template_type, demand_template_Tag, scene_name,
                                       begin_time, end_time, across_day, create_time, create_user, update_time,
                                       update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_template_type, NEW.demand_template_Tag, NEW.scene_name,
               NEW.begin_time, NEW.end_time, NEW.across_day, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_scene_update_snapshot -------------------------
drop trigger if exists demand_scene_update_snapshot;
delimiter $$
create trigger demand_scene_update_snapshot
    after update
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_scene_snapshot (id, project_id, demand_template_type, demand_template_Tag, scene_name,
                                       begin_time, end_time, across_day, create_time, create_user, update_time,
                                       update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_template_type, NEW.demand_template_Tag, NEW.scene_name,
               NEW.begin_time, NEW.end_time, NEW.across_day, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_scene_delete_snapshot -------------------------
drop trigger if exists demand_scene_delete_snapshot;
delimiter $$
create trigger demand_scene_delete_snapshot
    after delete
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create table demand_instance_config_snapshot -------------------------
create table if not exists demand_instance_config_snapshot
(
    id                   bigint                      not null comment 'ID',
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
    update_user          varchar(64)                 null comment '更新人',
    valid_end_time       datetime                    not null comment '生效截止时间',
    primary key (id, valid_end_time),
    key demand_instance_config_idx (project_id, demand_type, demand_template_type, demand_instance_type,
                                    demand_instance_id, st, valid_end_time)
) comment '需求实例配置快照表';
call init_snapshot_table('demand_instance_config', 'demand_instance_config_snapshot');
# ------------------------- create trigger demand_instance_config_insert_snapshot -------------------------
drop trigger if exists demand_instance_config_insert_snapshot;
delimiter $$
create trigger demand_instance_config_insert_snapshot
    after insert
    on demand_instance_config
    for each row
begin
    insert into demand_instance_config_snapshot (id, project_id, demand_type, demand_template_type,
                                                 demand_instance_type, demand_instance_id, config_data, create_time,
                                                 create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_instance_type,
               NEW.demand_instance_id, NEW.config_data, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_instance_config_update_snapshot -------------------------
drop trigger if exists demand_instance_config_update_snapshot;
delimiter $$
create trigger demand_instance_config_update_snapshot
    after update
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_instance_config_snapshot (id, project_id, demand_type, demand_template_type,
                                                 demand_instance_type, demand_instance_id, config_data, create_time,
                                                 create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_instance_type,
               NEW.demand_instance_id, NEW.config_data, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_instance_config_delete_snapshot -------------------------
drop trigger if exists demand_instance_config_delete_snapshot;
delimiter $$
create trigger demand_instance_config_delete_snapshot
    after delete
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create table demand_config_snapshot -------------------------
create table if not exists demand_config_snapshot
(
    id                   bigint auto_increment comment 'ID',
    project_id           varchar(255) not null comment '项目ID',
    demand_instance_type varchar(32)  not null comment '需求实例类型',
    demand_instance_id   varchar(255) not null comment '需求实例ID',
    config_data          json         null comment '配置数据',
    rel_template_Id      bigint       not null comment '关联模版ID',
    rel_scene_id         bigint       null comment '关联场景ID',
    create_time          datetime     not null comment '创建时间',
    create_user          varchar(255) null comment '创建人',
    update_time          datetime     not null comment '更新时间',
    update_user          varchar(255) null comment '更新人',
    valid_end_time       datetime     not null comment '生效截止时间',
    primary key (id, valid_end_time),
    key demand_config_scene_pk (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id,
                                valid_end_time)
) comment '需求配置快照表';
call init_snapshot_table('demand_config', 'demand_config_snapshot');
# ------------------------- create trigger demand_config_insert_snapshot -------------------------
drop trigger if exists demand_config_insert_snapshot;
delimiter $$
create trigger demand_config_insert_snapshot
    after insert
    on demand_config
    for each row
begin
    insert into demand_config_snapshot (id, project_id, demand_instance_type, demand_instance_id, config_data,
                                        rel_template_Id, rel_scene_id, create_time, create_user, update_time,
                                        update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_Id, NEW.rel_scene_id, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_update_snapshot -------------------------
drop trigger if exists demand_config_update_snapshot;
delimiter $$
create trigger demand_config_update_snapshot
    after update
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_snapshot (id, project_id, demand_instance_type, demand_instance_id, config_data,
                                        rel_template_Id, rel_scene_id, create_time, create_user, update_time,
                                        update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_Id, NEW.rel_scene_id, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_delete_snapshot -------------------------
drop trigger if exists demand_config_delete_snapshot;
delimiter $$
create trigger demand_config_delete_snapshot
    after delete
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create table demand_config_airconditioning_custom_snapshot -------------------------
create table if not exists demand_config_airconditioning_custom_snapshot
(
    `id`                   bigint   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `project_id`           varchar(64)  DEFAULT NULL COMMENT '项目ID',
    `demand_instance_type` varchar(32)  DEFAULT NULL COMMENT '需求实例类型',
    `demand_instance_id`   varchar(32)  DEFAULT NULL COMMENT '需求实例ID',
    `config_data`          json         DEFAULT NULL COMMENT '配置数据',
    `rel_template_id`      varchar(200) DEFAULT NULL COMMENT '关联需求模版ID',
    `rel_scene_id`         varchar(200) DEFAULT NULL COMMENT '关联需求场景ID',
    `create_time`          datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user`          varchar(64)  DEFAULT NULL COMMENT '创建人',
    `update_time`          datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user`          varchar(64)  DEFAULT NULL COMMENT '更新人',
    valid_end_time         datetime not null comment '生效截止时间',
    primary key (id, valid_end_time),
    KEY `nk_airconditioning_custom_instance_template` (`project_id`, `demand_instance_id`, `rel_template_id`,
                                                       `valid_end_time`) USING BTREE COMMENT '模板索引'
) comment '需求管理-空调需求-定制功能表快照表';
# ------------------------- create trigger demand_config_airconditioning_custom_insert_snapshot -------------------------
drop trigger if exists demand_config_airconditioning_custom_insert_snapshot;
delimiter $$
create trigger demand_config_airconditioning_custom_insert_snapshot
    after insert
    on demand_config_airconditioning_custom
    for each row
begin
    insert into demand_config_airconditioning_custom_snapshot (id, project_id, demand_instance_type, demand_instance_id,
                                                               config_data, rel_template_id, rel_scene_id, create_time,
                                                               create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_id, NEW.rel_scene_id, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_airconditioning_custom_update_snapshot -------------------------
drop trigger if exists demand_config_airconditioning_custom_update_snapshot;
delimiter $$
create trigger demand_config_airconditioning_custom_update_snapshot
    after update
    on demand_config_airconditioning_custom
    for each row
begin
    update demand_config_airconditioning_custom_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_airconditioning_custom_snapshot (id, project_id, demand_instance_type, demand_instance_id,
                                                               config_data, rel_template_id, rel_scene_id, create_time,
                                                               create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_id, NEW.rel_scene_id, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_airconditioning_custom_delete_snapshot -------------------------
drop trigger if exists demand_config_airconditioning_custom_delete_snapshot;
delimiter $$
create trigger demand_config_airconditioning_custom_delete_snapshot
    after delete
    on demand_config_airconditioning_custom
    for each row
begin
    update demand_config_airconditioning_custom_snapshot
    set valid_end_time = now()
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;