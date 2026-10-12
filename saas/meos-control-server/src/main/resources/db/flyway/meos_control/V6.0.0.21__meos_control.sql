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
    building_id                  varchar(64)                 null comment '建筑ID',
    floor_id                     varchar(64)                 null comment '楼层ID',
    space_id                     varchar(64)                 null comment '空间ID',
    space_func_type        varchar(32) null comment '空间功能类型',
    circuit_id                   varchar(64)                 not null comment '照明回路ID',
    circuit_func_type      varchar(32) not null comment '照明回路功能类型',
    circuit_lamp_func_type varchar(32) not null comment '照明回路灯具功能类型',
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
                     'create index demand_config_lighting_result_idx on demand_config_lighting_result (project_id, building_id, floor_id, space_id, space_func_type, circuit_id, circuit_func_type, circuit_lamp_func_type, rel_demand_template_id, rel_demand_lighting_scene_id, st);');