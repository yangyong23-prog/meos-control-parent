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
    valid_end_time  datetime     not null comment '生效截止日期',
    primary key (id, valid_end_time),
    constraint demand_config_group_snapshot_pk unique (project_id, group_name, rel_template_Id, rel_scene_id,
                                                       update_time, valid_end_time)
) comment '需求配置组快照表';

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