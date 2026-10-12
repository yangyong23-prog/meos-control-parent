call add_table_column('rc_info_code_cfg', 'remark_config',
                      'alter table rc_info_code_cfg add remark_config json DEFAULT NULL COMMENT ''额外所有的配置都在这里配置'';');
call add_table_column('rc_calculate_result', 'extend_info',
                      'alter table rc_calculate_result add extend_info json DEFAULT NULL COMMENT ''扩展信息'' after calculate_result;');
DELETE FROM rc_basics_config WHERE type = 'ino_environment';

INSERT IGNORE INTO rc_basics_config (id, `type`, st, `result`) values ('spaceDeficiency', 'space_deficiency_config', 'valid', '{"tempHighValue":0.5, "tempLowValue":-0.5, "co2HighValue":100, "offSetTime":30}');

CREATE TABLE IF NOT EXISTS `air_terminal_abnormal_info` (
	`id` varchar(200) NOT NULL COMMENT '主键ID, work_time+space_id ',
	`group_code` varchar(64) DEFAULT NULL COMMENT '集团编码',
	`project_id` varchar(20) DEFAULT NULL COMMENT '项目id',
	`work_date` varchar(20) DEFAULT NULL COMMENT '日期,yyyyMMdd',
	`space_id` varchar(200) NOT NULL COMMENT '空间ID',
	`space_local_id` varchar(200) DEFAULT NULL COMMENT '空间本地编码',
	`space_local_name` varchar(200) DEFAULT NULL COMMENT '空间本地名称',
    `business_time` json DEFAULT NULL COMMENT '营业时间，升序排序, HH:mm',
	`abnormal_result` json DEFAULT NULL COMMENT '异常数据',
	`abnormal_time` json DEFAULT NULL COMMENT '异常时间',
	`floor_id` varchar(200) DEFAULT NULL COMMENT '楼层ID',
	`floor_local_id` varchar(200) DEFAULT NULL COMMENT '楼层本地编码',
	`floor_local_name` varchar(200) DEFAULT NULL COMMENT '楼层本地名称',
	`building_id` varchar(200) DEFAULT NULL COMMENT '建筑ID',
	`building_local_id` varchar(200) DEFAULT NULL COMMENT '建筑本地编码',
	`building_local_name` varchar(200) DEFAULT NULL COMMENT '建筑本地名称',
	`extend_info` json DEFAULT NULL COMMENT '扩展字段',
	`create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`) USING BTREE,
	KEY `nk_terminal_space_id` (`space_id`) USING BTREE COMMENT '空间级别索引',
	KEY `nk_terminal_floor_id` (`floor_id`) USING BTREE COMMENT '楼层级别索引',
	KEY `nk_terminal_building_id` (`building_id`) USING BTREE COMMENT '建筑级别索引',
	KEY `nk_terminal_project_id` (`project_id`) USING BTREE COMMENT '项目索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '空调末端--异常空间信息';