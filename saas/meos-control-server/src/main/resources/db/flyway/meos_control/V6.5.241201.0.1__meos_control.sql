CREATE TABLE IF NOT EXISTS `analysis_air_conditioner_space`
(
    `id`                  bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_id`          varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目 ID',
    `building_id`         varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '建筑 ID',
    `floor_id`            varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '楼层id',
    `space_id`            varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '空间id',
    `space_name`          varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '空间名称',
    `day`                 datetime                                NOT NULL COMMENT '具体日期时间',
    `tag_type`            varchar(32) COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '标签类型',
    `abnormal_event`      tinyint(1)                              NOT NULL COMMENT '是否有异常事件',
    `time_number`         int                                     NOT NULL COMMENT '总时间数',
    `satisfy_time_number` int                                     NOT NULL COMMENT '达标时间数',
    `info_code_rule`      json                                    DEFAULT NULL COMMENT '需要过滤信息点规则',
    `business_time`       json                                    DEFAULT NULL COMMENT '营业时间',
    `time_tag`            json                                    DEFAULT NULL COMMENT '时间标签数据',
    `create_time`         datetime                                DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建日期',
    PRIMARY KEY (`id`),
    KEY `project_id_day_abnormal_event_index` (`project_id`, `day`, `abnormal_event`),
    KEY `project_id_day_floor_id_index` (`project_id`, `day`, `floor_id`),
    KEY `project_id_day_tag_type_index` (`project_id`, `day`),
    KEY `project_id_day_space_id_tag_type_index` (`project_id`, `day`, `space_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT ='运行分析-空调空间分析';

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