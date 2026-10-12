-- object_group表
CREATE TABLE IF NOT EXISTS `object_group` (
    `id` bigint NOT NULL COMMENT '主键',
    `group_name` varchar(100) NOT NULL COMMENT '编组名称',
    `group_type` varchar(50) NOT NULL COMMENT '编组类型',
    `layout` varchar(1000) DEFAULT NULL COMMENT '布局',
    `sequence` int DEFAULT NULL COMMENT '排序',
    `project_id` varchar(50) NOT NULL COMMENT '项目id',
    `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_object_group` (`project_id`,`group_type`)
) COMMENT='设备编组表';

-- object_group_relation表
CREATE TABLE IF NOT EXISTS `object_group_relation` (
     `id` bigint NOT NULL  COMMENT '主键',
     `object_id` varchar(100) NOT NULL COMMENT '对象id',
     `group_id` bigint NOT NULL COMMENT '分组id',
     `sequence` int DEFAULT NULL COMMENT '排序',
     `project_id` varchar(50) NOT NULL COMMENT '项目id',
     `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
     `create_time` datetime NOT NULL COMMENT '创建时间',
     `update_user` varchar(64) DEFAULT NULL COMMENT '更新人',
     `update_time` datetime NOT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     KEY `idx_object_group_relation` (`group_id`,`object_id`,`project_id`)
) COMMENT='设备编组关系表';