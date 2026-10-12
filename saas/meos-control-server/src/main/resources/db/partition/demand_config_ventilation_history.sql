-- ----------------------------
-- Table structure for demand_config_ventilation_history
-- ----------------------------
create table if not exists ${tableName}
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
    update_user                varchar(64)                 null comment '更新人',
    unique index demand_config_ventilation_history_pk (project_id, demand_type, demand_template_type,
                                                       demand_template_Tag, demand_instance_type,
                                                       demand_instance_id, config_date, st),
    index demand_config_ventilation_history_idx (project_id, demand_type, demand_template_type, demand_template_Tag,
                                                 demand_instance_type, demand_instance_id, parent_demand_instance_id,
                                                 config_date, is_customized_setting, st)
) comment '需求配置-通风历史表';
