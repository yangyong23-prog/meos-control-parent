CREATE TABLE IF NOT EXISTS `rc_calculate_result` (
    `id` varchar(100) NOT NULL COMMENT '日期+空间ID',
    `group_code` varchar(64) DEFAULT NULL COMMENT '集团编码',
    `project_id` varchar(20) DEFAULT NULL COMMENT '项目id',
    `space_id` varchar(100) DEFAULT NULL COMMENT '空间ID',
    `space_local_id` varchar(100) DEFAULT NULL COMMENT '空间本地编码',
    `space_local_name` varchar(100) DEFAULT NULL COMMENT '空间本地名称',
    `work_date` varchar(20) DEFAULT NULL COMMENT '日期，yyyyMMdd',
    `work_time` json DEFAULT NULL COMMENT '实际的开关机时间，升序排序, HH:mm',
    `season` varchar(20) DEFAULT NULL COMMENT '空调季，ACSeason-1：供冷季   ACSeason-2：供暖季    ACSeason-3：过渡季',
    `calculate_type` varchar(64) DEFAULT NULL COMMENT '价值选择的类型，BasicConfigTypeEnum',
    `calculate_result` json DEFAULT NULL COMMENT '价值计算的结果',
    `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_calculate_result_space_id` (`space_id`) USING BTREE COMMENT '空间级别索引',
    KEY `nk_calculate_result_work_date` (`work_date`) USING BTREE COMMENT '日期级别索引',
    KEY `nk_calculate_result_project_id` (`project_id`) USING BTREE COMMENT '项目级别索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '价值计算结果';

CREATE TABLE IF NOT EXISTS `rc_calculate_result_detail` (
    `id` varchar(100) NOT NULL COMMENT '主键ID',
    `group_code` varchar(64) DEFAULT NULL COMMENT '集团编码',
    `project_id` varchar(20) DEFAULT NULL COMMENT '项目id',
    `space_id` varchar(100) DEFAULT NULL COMMENT '空间ID',
    `space_local_id` varchar(100) DEFAULT NULL COMMENT '空间本地编码',
    `space_local_name` varchar(100) DEFAULT NULL COMMENT '空间本地名称',
    `work_date` varchar(20) DEFAULT NULL COMMENT '日期，yyyyMMdd',
    `work_time` TIME DEFAULT NULL COMMENT '当前时间点 LocalTime',
    `work_date_type` varchar(20) DEFAULT NULL COMMENT '当日所属工休日',
    `season` varchar(20) DEFAULT NULL COMMENT '空调季，ACSeason-1：供冷季   ACSeason-2：供暖季    ACSeason-3：过渡季',
    `business_time` json DEFAULT NULL COMMENT '当前时间点对应的营业时间',
    `business_model` varchar(20) DEFAULT NULL COMMENT '当前时间点对应的营业时间的时间模式',
    `calculate_type` varchar(64) DEFAULT NULL COMMENT '价值选择的类型，BasicConfigTypeEnum',
    `calculate_target` varchar(20) DEFAULT NULL COMMENT '价值选择计算的数据目标，EnumCalculateTarget',
    `calculate_result` json DEFAULT NULL COMMENT '价值计算的结果',
    `extend_info` json DEFAULT NULL COMMENT '计算依赖数据集合',
    `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_result_detail_space_id` (`space_id`) USING BTREE COMMENT '空间级别索引',
    KEY `nk_result_detail_work_date` (`work_date`) USING BTREE COMMENT '日期级别索引',
    KEY `nk_result_detail_project_id` (`project_id`) USING BTREE COMMENT '项目级别索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '价值计算结果详情';

INSERT IGNORE INTO rc_basics_async_task (id, type, sys, task_st, sp_el, execute_st, time_frequency, memo, create_user, create_time, update_time) VALUES ('DS', 'DS', 'locality', 'valid', null, 'init', 5, '数据同步任务', null, now(), now());
