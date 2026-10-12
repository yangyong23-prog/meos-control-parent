# ------------------------- 优化索引 -------------------------
call drop_table_index('equip_record', 'equip_record_obj_id_index');
call drop_table_index('equip_record', 'idx_info_code');


call add_table_index('equip_record', 'equip_record_group_code_project_id_obj_id_exec_status_index',
                     'create index equip_record_group_code_project_id_obj_id_exec_status_index
    on equip_record (group_code, project_id, obj_id, exec_status);');

call add_table_index('equip_record', 'idx_info_code',
                     'create index idx_info_code
    on equip_record (group_code, project_id, exec_status, info_code);');

# ------------------------- 创建记录表 -------------------------
CREATE TABLE IF NOT EXISTS  `equip_record_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '集团编码',
  `project_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '项目ID',
  `system_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '系统ID',
  `system_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '系统编码',
  `obj_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '设备对象ID',
  `change_content` json DEFAULT NULL COMMENT '指令下发内容',
  `exec_status` int DEFAULT '-1' COMMENT '执行状态: 默认 -1 没有执行;0：执行失败;1：执行成功',
  `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `creation_time` varchar(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建时间',
  `modified_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改时间',
  `modifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
  `extend` json DEFAULT NULL COMMENT '扩展字段',
  `info_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS (json_unquote(json_extract(`change_content`,_utf8mb4'$.infoCode'))) VIRTUAL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `creation_time` (`creation_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备异步指令下发表';