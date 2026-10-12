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