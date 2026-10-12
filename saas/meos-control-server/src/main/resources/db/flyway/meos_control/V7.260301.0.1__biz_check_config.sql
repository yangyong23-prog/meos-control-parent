CREATE TABLE IF NOT EXISTS `biz_check_config`
(
    `id`                   bigint NOT NULL COMMENT '配置ID，全局唯一标识，关联检查结果主表',
    `biz_code`             VARCHAR(32) NOT NULL COMMENT '业务模块编码，与检查结果主表一致',
    `is_enable`            TINYINT(1) NOT NULL COMMENT '是否启用检查：1=启用，0=禁用',
    `is_auto_check`        TINYINT(1) NOT NULL COMMENT '是否启用自动检查：1=启用，0=禁用',
    `auto_check_time`      VARCHAR(8)           DEFAULT NULL COMMENT '自动检查的每日时间，如10:00',
    `date_range_config`    JSON                 DEFAULT NULL COMMENT '拓展字段：日期段配置，JSON格式存储，如[{"startDay":"01-26","endDay":"01-30"}]',
    `notify_period_config` JSON                 DEFAULT NULL COMMENT '拓展字段：异常通知时段配置，JSON格式存储，如{"systemId":"SYS001","notifyDays":[1,2,3,4,5,6]}',
    `project_id`          varchar(50) NOT NULL COMMENT '项目id',
    `create_user`         varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime    NOT NULL COMMENT '创建时间',
    `update_user`         varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                    `idx_project_biz_config` (`project_id`,`biz_code`) COMMENT '按项目+业务筛选配置信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务可用性检查配置表，为检查结果提供配置溯源，记录各业务条线-子业务的检查配置信息';