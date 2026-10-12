# ------------------------- 节点树 -------------------------
CREATE TABLE if not exists `logtrace_node_info` (
  `id` bigint NOT NULL,
   `code` varchar(255)  DEFAULT NULL COMMENT '节点业务编码',
   `business_id` varchar(512) DEFAULT NULL  COMMENT '业务对应id',
   `application` varchar(255)  DEFAULT NULL COMMENT '业务所属方',
   `business_time` datetime DEFAULT NULL COMMENT '业务时间',
   `description` varchar(255) DEFAULT NULL COMMENT '节点描述',
   `create_time` datetime DEFAULT NULL COMMENT '节点创建时间',
   PRIMARY KEY (`id`)
);

# ------------------------- 节点关系 -------------------------
CREATE TABLE if not exists `logtrace_node_relation` (
  `id` bigint NOT NULL ,
  `node_id` bigint NOT NULL COMMENT '节点id',
  `parent_node_id` bigint DEFAULT NULL COMMENT '节点父id',
  `create_time` datetime DEFAULT NULL COMMENT '节点创建时间',
   PRIMARY KEY (`id`),
   KEY `node_id_index` (`node_id`) USING BTREE
) ;

# ------------------------- 节点业务数据 -------------------------
CREATE TABLE if not exists `logtrace_node_extend`  (
  `id` bigint NOT NULL,
  `node_code` varchar(512) DEFAULT NULL COMMENT '节点编码',
  `node_id` bigint DEFAULT NULL COMMENT '节点id',
  `extend_code` varchar(255) DEFAULT NULL COMMENT '业务数据key',
  `extend_name` varchar(255) DEFAULT NULL COMMENT '业务数据名字',
  `extend_value` varchar(512) DEFAULT NULL COMMENT '业务数据值',
  `business_time` datetime DEFAULT NULL COMMENT '业务时间，跟节点业务时间保持一致',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `node_id_index` (`node_id`) USING BTREE
) ;

# ------------------------- 查询模板 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate` (
  `id` bigint NOT NULL,
  `code` varchar(255) DEFAULT NULL COMMENT '模板编码',
  `name` varchar(255) DEFAULT NULL COMMENT '模板名称',
  `group_id` bigint DEFAULT NULL COMMENT '模板所属分组id',
  `query_index_node_code` varchar(255)  DEFAULT NULL COMMENT '模板开始查询树起始节点编码',
  `order_num` int DEFAULT NULL COMMENT '模板排序',
  `query_param` varchar(1024) DEFAULT NULL COMMENT '模板查询条件参数',
  `create_time` datetime DEFAULT NULL COMMENT '模板创建时间',
  `last_update_time` datetime DEFAULT NULL COMMENT '模板更新时间',
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);

# ------------------------- 查询模板分组 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_group` (
  `id` bigint NOT NULL,
  `group_code` varchar(255) DEFAULT NULL COMMENT '分组code',
  `group_name` varchar(255) DEFAULT NULL COMMENT '分组名称',
  `order_num` int DEFAULT NULL  COMMENT '分组排序',
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

# ------------------------- 查询模板分组关系表 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_group_relation` (
  `id` bigint NOT NULL,
  `group_id` bigint DEFAULT NULL COMMENT '分组id',
  `group_parent_id` bigint DEFAULT NULL COMMENT '分组的父id',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);
# ------------------------- 查询模板表头 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_header` (
  `id` bigint NOT NULL,
  `query_template_id` bigint DEFAULT NULL  COMMENT '表头所属模板id',
  `order_num` int DEFAULT NULL COMMENT '表头排序',
  `header_type` int DEFAULT NULL COMMENT '1为固定表头，2为动态表头',
  `header_code` varchar(255) DEFAULT NULL COMMENT '表头编码',
  `header_name` varchar(255) DEFAULT NULL COMMENT '表头名',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);

# ------------------------- 查询模板表头关系表 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_header_relation` (
  `id` bigint NOT NULL,
  `header_id` bigint DEFAULT NULL COMMENT '表头id',
  `header_parent_id` bigint DEFAULT NULL COMMENT '表头父id',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);
# ------------------------- 查询模板表头跟提取字段的映射关系 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_header_extractkey_relation` (
  `id` bigint NOT NULL,
  `header_id` bigint DEFAULT NULL COMMENT '表头id',
  `extract_object` varchar(512) DEFAULT NULL COMMENT '业务编码',
  `extract_key` varchar(255) DEFAULT NULL COMMENT '业务对象中的key',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);

# ------------------------- 查询模板关系表 -------------------------
CREATE TABLE if not exists `logtrace_querytemplate_relation` (
  `id` bigint NOT NULL,
  `template_id` bigint DEFAULT NULL COMMENT '模板id',
  `template_parent_id` bigint DEFAULT NULL  COMMENT '模板父id',
  `create_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL COMMENT '业务所属方',
  PRIMARY KEY (`id`)
);
