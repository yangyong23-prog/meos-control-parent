CREATE TABLE IF NOT EXISTS `rc_calculate_receive_message` (
    `id` varchar(200) NOT NULL COMMENT '时间点+空间Id',
    `group_code` varchar(64) DEFAULT NULL COMMENT '集团编码',
    `project_id` varchar(20) DEFAULT NULL COMMENT '项目id',
    `space_id` varchar(200) NOT NULL COMMENT '空间ID',
    `work_time` varchar(20) NOT NULL COMMENT '时间点',
    `startup_minutes` int DEFAULT NULL COMMENT '提前开机时长',
    `storage_minutes` int DEFAULT NULL COMMENT '提前蓄冷/蓄热时长',
    `advance_minutes` int DEFAULT NULL COMMENT '提前关机时长',
    `tag` varchar(10) DEFAULT NULL COMMENT '蓄冷、蓄热类型，0-蓄冷，1-蓄热',
    `extend_info` json DEFAULT NULL COMMENT '扩展字段',
    `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_open_close_time_space_id` (`space_id`) USING BTREE COMMENT '空间级别索引',
    KEY `nk_open_close_time_work_time` (`work_time`) USING BTREE COMMENT '时间点索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '算法推送的开关机时间点数据';
