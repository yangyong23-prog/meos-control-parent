CREATE TABLE IF NOT EXISTS `history_storage_result` (
    `id` bigint NOT NULL,
    `record_date` datetime DEFAULT NULL COMMENT '记录时间',
    `content` text COLLATE utf8mb4_general_ci COMMENT '内容',
    `project_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目id',
    `create_time` datetime DEFAULT NULL,
    `create_user` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `update_user` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `IN_record_date` (`record_date`,`project_id`) USING BTREE
) ENGINE=InnoDB  COMMENT='蓄冷蓄热结果表';