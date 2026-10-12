# ------------------------- create table demand_config_result -------------------------
create table if not exists demand_config_result
(
    id                     bigint auto_increment comment 'ID' primary key,
    project_id             varchar(50)  not null comment '项目ID',
    demand_template_type   varchar(32)  not null comment '需求模板类型',
    demand_instance_type   varchar(32)  not null comment '需求实例类型',
    demand_instance_id     varchar(64)  not null comment '需求实例ID',
    final_config_json      json         null comment '最终合并后的配置JSON',
    is_customized_config   tinyint      not null default 0 comment '是否自身存在自定义配置',
    is_shielded            tinyint      not null default 0 comment '是否屏蔽空间',
    inherit_instances_json json         null comment '从自身到项目的继承链路JSON',
    rel_template_id        bigint       not null comment '关联需求模板ID',
    rel_scene_id           bigint       null comment '关联需求场景ID',
    create_time            datetime(6)  not null comment '创建时间',
    create_user            varchar(64)  null comment '创建人',
    update_time            datetime(6)  not null comment '更新时间',
    update_user            varchar(64)  null comment '更新人'
) comment '需求配置预计算结果表';
call add_table_index('demand_config_result', 'idx_project_template_scene_instance',
                     'create index idx_project_template_scene_instance on demand_config_result (project_id, rel_template_id, rel_scene_id, demand_instance_type, demand_instance_id);');
call add_table_index('demand_config_result', 'idx_project_template_instance',
                     'create index idx_project_template_instance on demand_config_result (project_id, rel_template_id, demand_instance_type, demand_instance_id);');
call add_table_index('demand_config_result', 'idx_project_template_type_instance',
                     'create index idx_project_template_type_instance on demand_config_result (project_id, demand_template_type, demand_instance_type, demand_instance_id, rel_template_id, rel_scene_id);');

# ------------------------- create table demand_config_result_snapshot -------------------------
create table if not exists demand_config_result_snapshot
(
    id                     bigint      not null comment 'ID',
    project_id             varchar(50) not null comment '项目ID',
    demand_template_type   varchar(32) not null comment '需求模板类型',
    demand_instance_type   varchar(32) not null comment '需求实例类型',
    demand_instance_id     varchar(64) not null comment '需求实例ID',
    final_config_json      json        null comment '最终合并后的配置JSON',
    is_customized_config   tinyint     not null default 0 comment '是否自身存在自定义配置',
    is_shielded            tinyint     not null default 0 comment '是否屏蔽空间',
    inherit_instances_json json        null comment '从自身到项目的继承链路JSON',
    rel_template_id        bigint      not null comment '关联需求模板ID',
    rel_scene_id           bigint      null comment '关联需求场景ID',
    create_time            datetime(6) not null comment '创建时间',
    create_user            varchar(64) null comment '创建人',
    update_time            datetime(6) not null comment '更新时间',
    update_user            varchar(64) null comment '更新人',
    valid_end_time         datetime(6) not null comment '生效截止时间',
    primary key (id, valid_end_time)
) comment '需求配置预计算结果快照表';
call add_table_index('demand_config_result_snapshot', 'idx_result_snapshot_business',
                     'create index idx_result_snapshot_business on demand_config_result_snapshot (project_id, rel_template_id, rel_scene_id, demand_instance_type, demand_instance_id, valid_end_time, update_time);');
call add_table_index('demand_config_result_snapshot', 'idx_result_snapshot_template_instance',
                     'create index idx_result_snapshot_template_instance on demand_config_result_snapshot (project_id, rel_template_id, demand_instance_type, demand_instance_id, valid_end_time, update_time);');
