-- TYIFC_meos_control.info_code_set_extend definition

CREATE TABLE if NOT EXISTS  `info_code_set_extend` (
    `id` int NOT NULL AUTO_INCREMENT,
    `project_id` varchar(50)  DEFAULT NULL COMMENT '项目ID',
    `system_id` varchar(50)  DEFAULT NULL COMMENT '系统ID',
    `obj_id` varchar(50)  NOT NULL COMMENT '设备对象ID',
    `info_code` varchar(50)  NOT NULL COMMENT '信息点编码',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `extend` json DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (`id`),
    KEY `nk_obj_id_info_code` (`info_code`) USING BTREE COMMENT '信息点索引'
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='对象信息点设置扩展表';