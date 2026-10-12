# ------------------------- 新增需求配置分组表 -------------------------
create table if not exists demand_config_group
(
    id              bigint auto_increment comment 'ID' primary key,
    project_id      varchar(255) not null comment '项目ID',
    group_name      varchar(64)  not null comment '分组名称',
    rel_template_Id bigint       not null comment '关联模版ID',
    rel_scene_id    bigint       null comment '关联场景ID',
    create_time     datetime     not null comment '创建时间',
    create_user     varchar(255) null comment '创建人',
    update_time     datetime     not null comment '更新时间',
    update_user     varchar(255) null comment '更新人',
    constraint demand_config_group_pk unique (project_id, group_name, rel_template_Id, rel_scene_id)
) comment '需求配置组表';
create table if not exists demand_config_group_snapshot
(
    id              bigint auto_increment comment 'ID',
    project_id      varchar(255) not null comment '项目ID',
    group_name      varchar(64)  not null comment '分组名称',
    rel_template_Id bigint       not null comment '关联模版ID',
    rel_scene_id    bigint       null comment '关联场景ID',
    create_time     datetime     not null comment '创建时间',
    create_user     varchar(255) null comment '创建人',
    update_time     datetime     not null comment '更新时间',
    update_user     varchar(255) null comment '更新人',
    valid_end_time  datetime(6) not null comment '生效截止日期',
    primary key (id, valid_end_time),
    constraint demand_config_group_snapshot_pk unique (project_id, group_name, rel_template_Id, rel_scene_id,
                                                       update_time, valid_end_time)
) comment '需求配置组快照表';
# ------------------------- 新增需求黑名单表 -------------------------
create table if not exists demand_blacklist_rule
(
    `id`                   bigint auto_increment comment 'id',
    `project_id`           varchar(255) not null comment '项目id',
    `demand_type`          varchar(32)  default null comment '需求类型',
    `demand_template_type` varchar(32)  not null comment '需求模板类型',
    `config_data`          json         default null comment '配置数据',
    `create_time`          datetime     not null comment '创建时间',
    `create_user`          varchar(255) default null comment '创建人',
    `update_time`          datetime     not null comment '更新时间',
    `update_user`          varchar(255) default null comment '更新人',
    primary key (`id`),
    KEY `demand_blacklist_rule_demand_type_demand_template_type_index` (`demand_type`, `demand_template_type`)
) comment '需求黑名单规则表';

# ------------------------- 新增需求配置表关联分组ID -------------------------
call add_table_column('demand_config', 'rel_group_id',
                      'alter table demand_config add rel_group_id bigint null comment ''关联分组ID'' after rel_scene_id;');
call add_table_column('demand_config_snapshot', 'rel_group_id',
                      'alter table demand_config_snapshot add rel_group_id bigint null comment ''关联分组ID'' after rel_scene_id;');
# ------------------------- 重新创建索引 -------------------------
call drop_table_index('demand_config', 'demand_config_group_pk');
call add_table_index('demand_config', 'demand_config_group_pk',
                     'create unique index demand_config_group_pk on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_group_id);');
call drop_table_index('demand_config', 'demand_config_group_scene_pk');
call add_table_index('demand_config', 'demand_config_group_scene_pk',
                     'create unique index demand_config_group_scene_pk on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id, rel_group_id);');
call drop_table_index('demand_config_snapshot', 'demand_config_group_snapshot_idx');
call add_table_index('demand_config_snapshot', 'demand_config_group_snapshot_idx',
                     'create index demand_config_group_snapshot_idx on demand_config_snapshot (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_scene_id, rel_group_id, update_time, valid_end_time);');

# 修改快照表有效时间类型, 解决频繁操作时唯一主键冲突问题
# ------------------------- 需求模版快照表 -------------------------
call edit_table_column('demand_template_snapshot', 'valid_end_time',
                       'alter table demand_template_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 需求场景快照表 -------------------------
call edit_table_column('demand_scene_snapshot', 'valid_end_time',
                       'alter table demand_scene_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 需求实例配置快照表 -------------------------
call edit_table_column('demand_instance_config_snapshot', 'valid_end_time',
                       'alter table demand_instance_config_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 需求配置快照表 -------------------------
call edit_table_column('demand_config_snapshot', 'valid_end_time',
                       'alter table demand_config_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 空调需求自定义快照表 -------------------------
call edit_table_column('demand_config_airconditioning_custom_snapshot', 'valid_end_time',
                       'alter table demand_config_airconditioning_custom_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 空调配置分组快照表 -------------------------
call edit_table_column('demand_config_group_snapshot', 'valid_end_time',
                       'alter table demand_config_group_snapshot modify valid_end_time datetime(6) not null comment ''生效截止日期'';');
# ------------------------- 需求模版表触发器 -------------------------
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
    set valid_end_time = current_timestamp(6)
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
# -------------------- create trigger demand_template_delete_snapshot --------------------
drop trigger if exists demand_template_delete_snapshot;
delimiter $$
create trigger demand_template_delete_snapshot
    after delete
    on demand_template
    for each row
begin
    update demand_template_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;
# ------------------------- 需求场景表触发器 -------------------------
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
# -------------------- create trigger demand_scene_update_snapshot --------------------
drop trigger if exists demand_scene_update_snapshot;
delimiter $$
create trigger demand_scene_update_snapshot
    after update
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = current_timestamp(6)
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
# -------------------- create trigger demand_scene_delete_snapshot --------------------
drop trigger if exists demand_scene_delete_snapshot;
delimiter $$
create trigger demand_scene_delete_snapshot
    after delete
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;
# ------------------------- 需求实例配置表触发器 -------------------------
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
# -------------------- create trigger demand_instance_config_update_snapshot --------------------
drop trigger if exists demand_instance_config_update_snapshot;
delimiter $$
create trigger demand_instance_config_update_snapshot
    after update
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = current_timestamp(6)
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
# -------------------- create trigger demand_instance_config_delete_snapshot --------------------
drop trigger if exists demand_instance_config_delete_snapshot;
delimiter $$
create trigger demand_instance_config_delete_snapshot
    after delete
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;
# ------------------------- 需求配置表触发器 -------------------------
# -------------------- create trigger demand_config_insert_snapshot --------------------
drop trigger if exists demand_config_insert_snapshot;
delimiter $$
create trigger demand_config_insert_snapshot
    after insert
    on demand_config
    for each row
begin
    insert into demand_config_snapshot (id, project_id, demand_instance_type, demand_instance_id, config_data,
                                        rel_template_Id, rel_scene_id, rel_group_id, create_time, create_user,
                                        update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_Id, NEW.rel_scene_id, NEW.rel_group_id, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_config_update_snapshot --------------------
drop trigger if exists demand_config_update_snapshot;
delimiter $$
create trigger demand_config_update_snapshot
    after update
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_snapshot (id, project_id, demand_instance_type, demand_instance_id, config_data,
                                        rel_template_Id, rel_scene_id, rel_group_id, create_time, create_user,
                                        update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_Id, NEW.rel_scene_id, NEW.rel_group_id, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_config_delete_snapshot --------------------
drop trigger if exists demand_config_delete_snapshot;
delimiter $$
create trigger demand_config_delete_snapshot
    after delete
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;
# ------------------------- 空调需求自定义配置触发器 -------------------------
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
    set valid_end_time = current_timestamp(6)
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
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;
# ------------------------- 需求配置分组触发器 -------------------------
# ------------------------- create trigger demand_config_group_insert_snapshot -------------------------
drop trigger if exists demand_config_group_insert_snapshot;
delimiter $$
create trigger demand_config_group_insert_snapshot
    after insert
    on demand_config_group
    for each row
begin
    insert into demand_config_group_snapshot (id, project_id, group_name, rel_template_Id, rel_scene_id, create_time,
                                              create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.group_name, NEW.rel_template_Id, NEW.rel_scene_id, NEW.create_time,
               NEW.create_user, NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_group_update_snapshot -------------------------
drop trigger if exists demand_config_group_update_snapshot;
delimiter $$
create trigger demand_config_group_update_snapshot
    after update
    on demand_config_group
    for each row
begin
    update demand_config_group_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_group_snapshot (id, project_id, group_name, rel_template_Id, rel_scene_id, create_time,
                                              create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.group_name, NEW.rel_template_Id, NEW.rel_scene_id, NEW.create_time,
               NEW.create_user, NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_group_delete_snapshot -------------------------
drop trigger if exists demand_config_group_delete_snapshot;
delimiter $$
create trigger demand_config_group_delete_snapshot
    after delete
    on demand_config_group
    for each row
begin
    update demand_config_group_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;