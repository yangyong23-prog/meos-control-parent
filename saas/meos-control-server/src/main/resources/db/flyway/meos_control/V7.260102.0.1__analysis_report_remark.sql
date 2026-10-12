CREATE TABLE IF NOT EXISTS `analysis_report_remark`
(
    `id`                  bigint      NOT NULL COMMENT '主键',
    `report_id`           bigint      NOT NULL COMMENT '报告id',
    `object_id`           varchar(100) DEFAULT NULL COMMENT '对象id',
    `custom_key`          varchar(100) DEFAULT NULL COMMENT '自定义key',
    `remark`              varchar(5000) NOT NULL COMMENT '备注',
    `project_id`          varchar(50) NOT NULL COMMENT '项目id',
    `create_user`         varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime    NOT NULL COMMENT '创建时间',
    `update_user`         varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `project_id_report` (`project_id`, `report_id`)
    ) ENGINE = InnoDB COMMENT ='运行分析-报告备注';