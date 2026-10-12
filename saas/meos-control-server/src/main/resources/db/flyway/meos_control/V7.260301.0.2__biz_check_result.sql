CREATE TABLE IF NOT EXISTS `biz_check_result`
(
    `id`               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_code`         VARCHAR(64) NOT NULL COMMENT '业务条线编码',
    `check_item_code`  VARCHAR(64) NOT NULL COMMENT '检查项编码',
    `check_status`     VARCHAR(16) NOT NULL COMMENT '检查状态 (PENDING:检查中 COMPLETED:已完成 FAILED:失败)',
    `check_type`       VARCHAR(16) NOT NULL COMMENT '检查类型 (AUTO:自动 MANUAL:人工)',
    `check_result`      JSON NULL COMMENT '检查项结果详情',
    `check_start_time` DATETIME NULL DEFAULT NULL COMMENT '检查开始时间',
    `check_end_time`   DATETIME NULL DEFAULT NULL COMMENT '检查结束时间',
    `project_id`          varchar(50) NOT NULL COMMENT '项目id',
    `create_user`         varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime    NOT NULL COMMENT '创建时间',
    `update_user`         varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                `idx_project_biz` (`project_id`, `biz_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查结果表';