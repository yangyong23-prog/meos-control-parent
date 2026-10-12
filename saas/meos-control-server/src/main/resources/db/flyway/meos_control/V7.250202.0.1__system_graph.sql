CREATE TABLE IF NOT EXISTS `system_graph_async_job` (
      `id` bigint NOT NULL,
      `project_id` varchar(32)  NOT NULL COMMENT '项目id',
      `system_object_id` varchar(64)  NOT NULL COMMENT '基准对象',
      `graph_type_code` varchar(128)  NOT NULL COMMENT '图类型',
      `template_ori_Id` varchar(64)  NOT NULL COMMENT '模板oriId',
      `not_edit` int DEFAULT NULL COMMENT '是否可编辑',
      `job_param` text  COMMENT '任务参数',
      `job_id` varchar(64)  DEFAULT NULL COMMENT '任务id',
      `job_status` int DEFAULT NULL COMMENT '任务结果0:成功，1,失败，2,执行中',
      `job_message` text  COMMENT '任务消息',
      `create_user` varchar(255)  DEFAULT NULL,
      `create_time` datetime NOT NULL,
      `update_user` varchar(255)  DEFAULT NULL,
      `update_time` datetime NOT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_graph_async_job` (`project_id`,`system_object_id`,`graph_type_code`,`job_id`,`template_ori_Id`)
) COMMENT='关系图异步任务表';