create table if not exists cache_iot_rule
(
    id                          bigint       not null comment 'ID' primary key,
    project_id                  varchar(64)  not null comment '项目ID',
    running_monitor_type        varchar(64)  not null comment '运行监测类型',
    running_monitor_second_type varchar(64)  not null comment '运行监测二级类型',
    custom_key                  varchar(255) not null comment '自定义 Key',
    object_id                   varchar(64)  not null comment '对象ID',
    info_code                   varchar(64)  not null comment '信息点编码',
    type                        varchar(32)  not null comment '类型',
    create_time                 datetime     not null comment '创建时间',
    create_user                 varchar(64)  null comment '创建人',
    update_time                 datetime     not null comment '更新时间',
    update_user                 varchar(64)  null comment '更新人',
    key idx (project_id, custom_key, type),
    key idx_2 (project_id, running_monitor_type, running_monitor_second_type, object_id, info_code, type)
) comment '缓存-IOT规则表';

create table if not exists cache_info_code_template
(
    id                          bigint       not null comment 'ID' primary key,
    project_id                  varchar(64)  not null comment '项目ID',
    running_monitor_type        varchar(64)  not null comment '运行监测类型',
    running_monitor_second_type varchar(64)  not null comment '运行监测二级类型',
    custom_key                  varchar(255) not null comment '模版唯一 Key',
    data_json                   json         null comment '数据',
    rel_template_id             bigint       not null comment '关联模版ID',
    create_time                 datetime     not null comment '创建时间',
    create_user                 varchar(64)  null comment '创建人',
    update_time                 datetime     not null comment '更新时间',
    update_user                 varchar(64)  null comment '更新人',
    key idx (project_id, running_monitor_type, running_monitor_second_type, custom_key),
    key idx_2 (project_id, running_monitor_type, custom_key)
) comment '缓存-信息点模版表';
