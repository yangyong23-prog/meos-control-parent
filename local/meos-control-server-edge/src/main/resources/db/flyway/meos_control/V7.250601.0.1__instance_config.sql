CREATE TABLE IF NOT EXISTS `instance_config`
(
    `id`                          bigint      NOT NULL COMMENT '主键',
    `running_monitor_type`        varchar(50) NOT NULL COMMENT '运行监测枚举',
    `running_monitor_second_type` varchar(50) DEFAULT NULL COMMENT '运行监测二级类型枚举',
    `object_type`                 varchar(50) NOT NULL COMMENT '实例类型',
    `config_type`                 varchar(20) NOT NULL COMMENT '配置类型: WHITE_LIST白名单, BLACK_LIST黑名单',
    `config_data`                 json        DEFAULT NULL COMMENT '配置数据',
    `project_id`                  varchar(50) NOT NULL COMMENT '项目id',
    `create_user`                 varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`                 datetime    NOT NULL COMMENT '创建时间',
    `update_user`                 varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`                 datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_instance_config` (`running_monitor_type`, `running_monitor_second_type`, `object_type`, `config_type`)
) COMMENT ='实例配置表';