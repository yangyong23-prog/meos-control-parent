CREATE TABLE IF NOT EXISTS `system_graph`
(
    `id`                        bigint       NOT NULL,
    `class_code`                varchar(32)  DEFAULT NULL COMMENT '设备类code',
    `system_object_id`          varchar(255) NOT NULL COMMENT '系统实例id',
    `graph_type_code`           varchar(128) DEFAULT NULL COMMENT '关系图类',
    `system_graph_duplicate_id` bigint       DEFAULT NULL COMMENT '副本id',
    `graph_name`                varchar(100) DEFAULT NULL COMMENT '关系图图名称',
    `project_id`                varchar(32)  DEFAULT NULL COMMENT '项目id',
    `status`                    tinyint      DEFAULT NULL COMMENT '状态：1成功，0失败',
    `pass_by`                   tinyint      DEFAULT NULL COMMENT '小附件创建标识：1 有 0 无',
    `create_user`               varchar(255) DEFAULT NULL,
    `create_time`               datetime     NOT NULL,
    `update_user`               varchar(100) DEFAULT NULL,
    `update_time`               datetime     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `index_graph` (`class_code`, `system_object_id`, `graph_type_code`, `project_id`)
) COMMENT ='系统图表';


CREATE TABLE IF NOT EXISTS `system_graph_generate_parameter`
(
    `id`                        bigint   NOT NULL,
    `system_graph_duplicate_id` bigint       DEFAULT NULL,
    `class_code`                varchar(32)  DEFAULT NULL COMMENT '设备类code',
    `data`                      text COMMENT '参数数据',
    `project_id`                varchar(32)  DEFAULT NULL COMMENT '项目id',
    `template_id`               varchar(128) DEFAULT NULL COMMENT '模板id',
    `create_user`               varchar(255) DEFAULT NULL,
    `create_time`               datetime NOT NULL,
    `update_user`               varchar(100) DEFAULT NULL,
    `update_time`               datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `index_generate_parameter` (`system_graph_duplicate_id`, `class_code`, `template_id`) USING BTREE
) COMMENT ='关系图参数表';


CREATE TABLE IF NOT EXISTS `system_graph_data`
(
    `id`            bigint   NOT NULL,
    `request_param` varchar(32)  DEFAULT NULL COMMENT '访问参数',
    `data`          longtext COMMENT '系统图数据',
    `project_id`    varchar(32)  DEFAULT NULL COMMENT '项目id',
    `status`        tinyint      DEFAULT NULL COMMENT '状态，1已发布，0未发布',
    `create_user`   varchar(100) DEFAULT NULL,
    `create_time`   datetime NOT NULL,
    `update_user`   varchar(100) DEFAULT NULL,
    `update_time`   datetime NOT NULL,
    PRIMARY KEY (`id`)
) COMMENT ='系统图数据表';


CREATE TABLE IF NOT EXISTS `system_graph_duplicate`
(
    `id`                     bigint   NOT NULL,
    `class_code`             varchar(32)  DEFAULT NULL COMMENT '设备类code',
    `template_id`            varchar(100) DEFAULT NULL COMMENT '模版id',
    `system_object_id`       varchar(255) DEFAULT NULL COMMENT '系统实例id',
    `graph_type_code`        varchar(128) DEFAULT NULL COMMENT '关系图类',
    `system_graph_data_id`   bigint       DEFAULT NULL COMMENT '系统图数据id',
    `system_graph_detail_id` bigint       DEFAULT NULL COMMENT '系统图细节调整id',
    `generate_id`            varchar(100) DEFAULT NULL COMMENT '生成id，系统图引擎返回',
    `name`                   varchar(100) DEFAULT NULL COMMENT '关系图图名称',
    `project_id`             varchar(32)  DEFAULT NULL COMMENT '项目id',
    `is_not_edit`            tinyint      DEFAULT NULL COMMENT '是否不可编辑',
    `status`                 tinyint      DEFAULT NULL COMMENT '状态：0未发布，1已发布，2已下架',
    `order`                  tinyint      DEFAULT NULL COMMENT '副本序号',
    `graph_type`             tinyint      DEFAULT NULL COMMENT '0、原图，1、副本',
    `create_user`            varchar(100) DEFAULT NULL,
    `create_time`            datetime NOT NULL,
    `update_user`            varchar(100) DEFAULT NULL,
    `update_time`            datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `index_graph` (`class_code`, `system_object_id`, `graph_type_code`, `project_id`, `system_graph_data_id`,
                       `system_graph_detail_id`) USING BTREE
) COMMENT ='系统图副本表';


CREATE TABLE IF NOT EXISTS `system_graph_detail`
(
    `id`                        bigint      NOT NULL,
    `project_id`                varchar(50) NOT NULL COMMENT '项目id',
    `system_graph_duplicate_id` bigint       DEFAULT NULL COMMENT '副本id',
    `code`                      varchar(100) DEFAULT NULL COMMENT '编码',
    `type`                      tinyint      DEFAULT NULL COMMENT '1:设备2:信息点3:标签4:分组',
    `data`                      text COMMENT '细节调整相关数据，如信息点列表，名称，位置等',
    `create_user`               varchar(100) DEFAULT NULL,
    `create_time`               datetime    NOT NULL,
    `update_user`               varchar(100) DEFAULT NULL,
    `update_time`               datetime    NOT NULL,
    PRIMARY KEY (`id`)
) COMMENT ='系统图细节调整表';


CREATE TABLE IF NOT EXISTS `system_graph_template_status`
(
    `id`             bigint      NOT NULL,
    `project_id`     varchar(50) NOT NULL COMMENT '项目id',
    `template_id`    varchar(100) DEFAULT NULL COMMENT '模版id',
    `object_id`      varchar(100) DEFAULT NULL COMMENT '项目id',
    `updated_status` tinyint      DEFAULT NULL COMMENT '是否更新,1更新，0：未更新',
    `create_user`    varchar(100) DEFAULT NULL,
    `create_time`    datetime    NOT NULL,
    `update_user`    varchar(100) DEFAULT NULL,
    `update_time`    datetime    NOT NULL,
    PRIMARY KEY (`id`),
    KEY `index_template` (`project_id`, `template_id`)
) COMMENT ='系统图模版状态表';