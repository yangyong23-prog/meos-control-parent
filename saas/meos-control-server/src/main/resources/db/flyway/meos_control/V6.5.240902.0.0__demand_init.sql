# ------------------------- demand_scene -------------------------
create table if not exists demand_scene
(
    id                   bigint auto_increment comment 'ID' primary key,
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
    update_user          varchar(255) null comment '更新人'
) comment '需求场景表';
call add_table_index('demand_scene', 'demand_scene_idx',
                     'create index `demand_scene_idx` on `demand_scene` (`project_id`, `demand_template_type`, `demand_template_Tag`, `scene_name`);');

# ------------------------- demand_config -------------------------
create table if not exists demand_config
(
    id                   bigint auto_increment comment 'ID' primary key,
    project_id           varchar(255) not null comment '项目ID',
    demand_instance_type varchar(32)  not null comment '需求实例类型',
    demand_instance_id   varchar(255) not null comment '需求实例ID',
    config_data          json         null comment '配置数据',
    rel_template_Id      bigint       not null comment '关联模版ID',
    rel_scene_id         bigint       null comment '关联场景ID',
    create_time          datetime     not null comment '创建时间',
    create_user          varchar(255) null comment '创建人',
    update_time          datetime     not null comment '更新时间',
    update_user          varchar(255) null comment '更新人'
) comment '需求配置表';
call add_table_index('demand_config', 'demand_config_pk',
                     'create unique index `demand_config_pk` on `demand_config` (`project_id`, `demand_instance_type`, `demand_instance_id`, `rel_template_Id`);');
call add_table_index('demand_config', 'demand_config_scene_pk',
                     'create unique index `demand_config_scene_pk` on `demand_config` (`project_id`, `demand_instance_type`, `demand_instance_id`, `rel_template_Id`, `rel_scene_id`);');