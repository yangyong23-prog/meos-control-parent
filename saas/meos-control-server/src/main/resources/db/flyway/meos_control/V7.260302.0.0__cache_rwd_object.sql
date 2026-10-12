CREATE TABLE IF NOT EXISTS `cache_rwd_object`
(
    `id`                  bigint      NOT NULL COMMENT '主键',
    `object_type`         varchar(32) NOT NULL COMMENT '实例类型',
    `object_id`           varchar(64) NOT NULL COMMENT '实例ID',
    `class_code`          VARCHAR(32) NOT NULL COMMENT '分类编码',
    `running_monitor_type` VARCHAR(50) NOT NULL COMMENT '监测类型',
    `custom_index_key`    VARCHAR(200) NOT NULL COMMENT '自定义索引 key，方法入参md5',
    `object_data`         JSON DEFAULT NULL COMMENT '数据 JSON',
    `project_id`          varchar(50) NOT NULL COMMENT '项目id',
    `create_user`         varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime    NOT NULL COMMENT '创建时间',
    `update_user`         varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_running_monitor_type` (`project_id`, `running_monitor_type`),
    KEY `idx_custom_index_key` (`project_id`, `custom_index_key`)
    ) ENGINE = InnoDB COMMENT ='物理世界对象缓存';