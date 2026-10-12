# ------------------------- 干预配置表 -------------------------
create table if not exists intervention_config
(
    id                          bigint       not null comment 'ID' primary key,
    project_id                  varchar(64)  not null comment '项目ID',
    running_monitor_second_type varchar(32)  not null comment '运行监测二级类型',
    instance_type               varchar(32)  not null comment '实例类型',
    instance_id                 varchar(64)  not null comment '实例ID',
    begin_time                  datetime     not null comment '开始时间',
    end_time                    datetime     not null comment '结束时间',
    config_data                 json         not null comment '干预数据',
    reason                      varchar(512) not null comment '干预原因',
    create_time                 datetime     not null comment '创建时间',
    create_user                 varchar(64)  null comment '创建人',
    update_time                 datetime     not null comment '更新时间',
    update_user                 varchar(64)  null comment '更新人',
    constraint intervention_config_pk unique (project_id, running_monitor_second_type, instance_type, instance_id,
                                              begin_time, end_time)
) comment '人工干预配置表';