
-- 新增 干预任务配置表
create table if not exists  `intervention_task_config` (
                                            `id` bigint NOT NULL COMMENT '主键 ID',
                                            `project_id` varchar(64)  DEFAULT NULL COMMENT '项目 ID',
                                            `task_id` bigint NOT NULL COMMENT '任务ID(雪花 id)',
                                            `task_name` varchar(512)  DEFAULT NULL COMMENT '任务名称',
                                            `running_monitor_type` varchar(32)  NOT NULL COMMENT '一级监测类型',
                                            `running_monitor_second_type` varchar(32)  DEFAULT NULL COMMENT '运行监测二级类型',
                                            `data_type` int NOT NULL COMMENT '数据类型(0主配置/1关联配置)',
                                            `source_type` varchar(32)  DEFAULT 'WEB' COMMENT '来源类型(app/web)',
                                            `instance_type` varchar(64)  DEFAULT NULL COMMENT '实例类型',
                                            `instance_id` varchar(128)  DEFAULT NULL COMMENT '实例ID',
                                            `begin_time` datetime DEFAULT NULL COMMENT '开始生效时间',
                                            `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
                                            `config_data` json DEFAULT NULL COMMENT '配置信息(回路列表的开关状态)',
                                            `reason` varchar(512)  DEFAULT NULL COMMENT '原因',
                                            `create_time` datetime NOT NULL COMMENT '创建时间',
                                            `create_user` varchar(64) DEFAULT 'system' COMMENT '创建人',
                                            `update_time` datetime COMMENT '更新时间',
                                            `update_user` varchar(64)  DEFAULT 'system' COMMENT '更新人',
                                            PRIMARY KEY (`id`),
                                            KEY `running_monitor_begin_time_end_time_index` (`project_id`,`running_monitor_type`,`begin_time`,`end_time`),
                                            KEY `running_monitor_id_type_instance_id_index` (`project_id`,`running_monitor_type`,`task_id`,`data_type`,`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='干预任务配置表';