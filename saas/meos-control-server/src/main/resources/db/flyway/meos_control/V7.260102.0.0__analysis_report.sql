CREATE TABLE IF NOT EXISTS `analysis_report`
(
    `id`                  bigint      NOT NULL COMMENT '主键',
    `report_date`         datetime    NOT NULL COMMENT '具体日期时间',
    `report_type`         varchar(20) NOT NULL COMMENT '报告类型',
    `report_chapter`      varchar(64) NOT NULL COMMENT '章节',
    `object_id`           varchar(100) DEFAULT NULL COMMENT '对象id',
    `chapter_data`        json        DEFAULT NULL COMMENT '详情数据',
    `chapter_config`      json        DEFAULT NULL COMMENT '章节配置',
    `report_status`       varchar(20)  NOT NULL COMMENT '状态',
    `operator`            varchar(64)  DEFAULT NULL COMMENT '操作人',
    `project_id`          varchar(50) NOT NULL COMMENT '项目id',
    `create_user`         varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime    NOT NULL COMMENT '创建时间',
    `update_user`         varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `project_id_date_type_chapter` (`project_id`, `report_date`, `report_type`, `report_chapter`)
    ) ENGINE = InnoDB COMMENT ='运行分析-报告';