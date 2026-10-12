create table if not exists `rc_running_log`
(
    `id`                    bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_id`            VARCHAR(64) NOT NULL COMMENT '项目id',
    `system_id`             VARCHAR(64) DEFAULT NULL COMMENT '系统id',
    `object_id`             VARCHAR(64) NOT NULL COMMENT '操作对象id',
    `object_name`           VARCHAR(64) NOT NULL COMMENT '操作对象名称',
    `class_code`            VARCHAR(16) DEFAULT NULL COMMENT '对象类型',
    `object_category`       varchar(32) DEFAULT NULL COMMENT '对象分类,"project","system","building","floor","space","group"',
    `first_level_category`  varchar(128) NOT NULL COMMENT '一级菜单',
    `second_level_category` varchar(128) NOT NULL COMMENT '二级菜单',
    `third_level_category`  varchar(128) NOT NULL COMMENT '三级菜单',
    `type`                  int(2) NOT NULL COMMENT '日志标识1,人工/2:自动',
    `key_word`              varchar(128) DEFAULT NULL COMMENT '关键字',
    `content`               text DEFAULT NULL COMMENT '操作内容',
    `detail`                text DEFAULT NULL COMMENT '操作详情',
    `effect_start_time`     datetime DEFAULT NULL COMMENT '生效开始时间',
    `effect_end_time`       datetime DEFAULT NULL COMMENT '生效结束时间',
    `reason`                varchar(256) DEFAULT NULL COMMENT '调整原因',
    `create_user`           varchar(64) NOT NULL COMMENT '操作人',
    `create_time`           datetime NOT NULL COMMENT '创建时间',
    `syn_time`              datetime DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`),
    KEY `project_create_time_index` (`project_id`, `create_time`)
    ) ENGINE = InnoDB
    AUTO_INCREMENT = 0
    DEFAULT CHARSET = utf8mb4
    ROW_FORMAT = DYNAMIC COMMENT ='运行日志表';