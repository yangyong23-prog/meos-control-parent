-- 新增运行日志表
create table if not exists `running_log`
(
    `id`                bigint       NOT NULL COMMENT '主键 id',
    `project_id`        varchar(64)  NOT NULL COMMENT '项目 ID',
    `category` varchar(128) NOT NULL COMMENT '日志分类',
    `type`              int          NOT NULL COMMENT '日志类型  1:自动/2人工/3 报警',
    `object_id`         varchar(128) NOT NULL COMMENT '操作对象id',
    `object_name`       varchar(128)          DEFAULT NULL COMMENT '操作对象名称',
    `state`             int          NOT NULL COMMENT '反馈状态(-1控制结果未知/1成功/2失败/3超时/4终止/5异常)',
    `key_word`          varchar(32)  NOT NULL COMMENT '操作关键字',
    `create_user`       varchar(128) NOT NULL DEFAULT '系统' COMMENT '操作人名称(默认''系统'')',
    `data_time`         datetime     NOT NULL COMMENT '数据时间',
    `create_time`       datetime     NOT NULL COMMENT '创建时间',
    `content`           json         NOT NULL COMMENT '操作内容',
    `class_code`        varchar(32)           DEFAULT NULL COMMENT '对象类型',
    `system_id`         varchar(64)           DEFAULT NULL COMMENT '所属于系统 ID',
    `system_name`       varchar(128) DEFAULT NULL COMMENT '系统名称',
    `system_class_code` varchar(32)  DEFAULT NULL COMMENT '系统编码',
    `effect_start_time` datetime              DEFAULT NULL COMMENT '生效开始时间',
    `effect_end_time`   datetime              DEFAULT NULL COMMENT '生效结束时间',
    `trace_id`          varchar(64)           DEFAULT NULL COMMENT '追踪ID',
    `info_code`         varchar(64)           DEFAULT NULL COMMENT '信息点编码',
    `info_value`        varchar(64)           DEFAULT NULL COMMENT '信息点设定值',
    `source_type`       varchar(64)           DEFAULT NULL COMMENT '来源数据类型(时间表等)',
    PRIMARY KEY (`id`),
    KEY `running_log_data_time_type_index` (`data_time`, `type`),
    KEY `running_log_system_id_data_time_index` (`system_id`, `data_time`),
    KEY `running_log_trace_id_object_id_info_code_state_index` (`trace_id`, `object_id`, `info_code`, `state`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='感知运行日志表';

-- 废弃日志历史表 未使用
DROP TABLE IF EXISTS rc_running_log_history;
DROP TABLE IF EXISTS rc_running_log;