CREATE TABLE IF NOT EXISTS `demand_instance_group` (
     `id` bigint NOT NULL COMMENT 'ID',
     `project_id` varchar(64) NOT NULL COMMENT '项目ID',
     `template_type` varchar(32) NOT NULL COMMENT '需求模版类型',
     `name` varchar(64) NOT NULL COMMENT '分组名称',
     `purpose` varchar(256) DEFAULT NULL COMMENT '分组用途',
     `tag` varchar(32) DEFAULT NULL COMMENT '分组标签',
     `create_time` datetime NOT NULL COMMENT '创建时间',
     `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
     `update_time` datetime NOT NULL COMMENT '更新时间',
     `update_user` varchar(64) DEFAULT NULL COMMENT '更新人',
     PRIMARY KEY (`id`),
     KEY `idx_demand_instance_group` (`project_id`,`template_type`,`name`)
)  COMMENT='需求实例-分组表';

CREATE TABLE IF NOT EXISTS `demand_instance_group_detail` (
    `id` bigint NOT NULL COMMENT 'ID',
    `project_id` varchar(64) NOT NULL COMMENT '项目ID',
    `instance_type` varchar(32) NOT NULL COMMENT '实例类型',
    `instance_id` varchar(64) NOT NULL COMMENT '实例ID',
    `rel_group_id` bigint NOT NULL COMMENT '关联分组ID',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    `update_user` varchar(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_demand_instance_group_detail` (`project_id`,`instance_type`,`rel_group_id`,`instance_id`)
) COMMENT='需求实例-分组详情表';