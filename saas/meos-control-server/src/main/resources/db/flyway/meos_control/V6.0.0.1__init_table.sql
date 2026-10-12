create table if not exists `alarm_comment`
(
    `id`            varchar(80) NOT NULL COMMENT '主键',
    `record_id`     varchar(40) NOT NULL DEFAULT '' COMMENT '报警记录id',
    `content`       text        NOT NULL COMMENT '批注内容',
    `create_user`   varchar(32) NOT NULL COMMENT '创建人',
    `project_id`    varchar(50)          DEFAULT NULL COMMENT '项目id',
    `group_code`    varchar(50)          DEFAULT NULL COMMENT '集团编码',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user`   varchar(32)          DEFAULT NULL COMMENT '更新人',
    `valid`         int                  DEFAULT '1' COMMENT '删除标记 1:true,0:false',
    `event_type_id` varchar(50)          DEFAULT NULL COMMENT '报警类型',
    `syn_time`      datetime             DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='报警批注';

create table if not exists `alarm_condition_v2`
(
    `id`                 varchar(64) NOT NULL COMMENT '主键',
    `info_codes`         json         DEFAULT NULL COMMENT '包含的参数',
    `trigger_frontend`   json         DEFAULT NULL COMMENT '触发条件-前端展示',
    `trigger_backend`    text COMMENT '触发条件-后端使用',
    `trigger_rules`      varchar(255) DEFAULT NULL COMMENT '触发规则-中文描述',
    `trigger_uphold`     int          DEFAULT '0' COMMENT '持续多久后触发',
    `end_uphold`         int          DEFAULT NULL COMMENT '持续多久后恢复',
    `end_frontend`       json         DEFAULT NULL COMMENT '恢复条件-前端',
    `end_backend`        text COMMENT '恢复条件-后端',
    `end_rules`          varchar(255) DEFAULT NULL COMMENT '恢复规则-中文描述',
    `alarm_level`        varchar(10)  DEFAULT NULL COMMENT '报警等级',
    `effect_time_type`   varchar(10)  DEFAULT NULL COMMENT '生效时间类型',
    `effect_time_detail` json         DEFAULT NULL COMMENT '生效时间详情',
    `ts`                 datetime     DEFAULT NULL COMMENT '乐观锁',
    `creator`            varchar(64)  DEFAULT NULL COMMENT '创建人',
    `creation_time`      datetime     DEFAULT NULL COMMENT '创建时间',
    `modifier`           varchar(64)  DEFAULT NULL COMMENT '修改人',
    `modified_time`      datetime     DEFAULT NULL COMMENT '修改时间',
    `valid`              int          DEFAULT '1' COMMENT '删除标志',
    `syn_time`           datetime     DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='报警条件';

create table if not exists `alarm_record`
(
    `id`                varchar(50)  NOT NULL,
    `item_code`         varchar(40)  NOT NULL COMMENT '报警类型编码',
    `project_id`        varchar(50)  NOT NULL COMMENT '项目id',
    `obj_id`            varchar(120) NOT NULL COMMENT '对象id',
    `obj_name`          varchar(200)          DEFAULT NULL COMMENT '对象名称',
    `class_code`        varchar(50)           DEFAULT NULL COMMENT '对象类型编码',
    `level`             varchar(10)           DEFAULT NULL COMMENT '严重程度',
    `remark`            text COMMENT '报警描述',
    `create_user`       varchar(80)           DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user`       varchar(80)           DEFAULT NULL COMMENT '更新人',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `handler`           varchar(50)           DEFAULT NULL,
    `handle_time`       datetime              DEFAULT NULL COMMENT '处理时间',
    `valid`             int          NOT NULL DEFAULT '1' COMMENT '删除标记 1:true,0:false',
    `name`              varchar(200)          DEFAULT NULL,
    `category`          varchar(80)           DEFAULT NULL COMMENT '报警系统',
    `concern`           int                   DEFAULT NULL COMMENT '是否重点关注',
    `state`             int                   DEFAULT NULL COMMENT '报警状态',
    `effect_start_time` datetime              DEFAULT NULL COMMENT '有效期开始时间',
    `effect_end_time`   datetime              DEFAULT NULL COMMENT '有效期结束时间',
    `nature`            int                   DEFAULT NULL COMMENT '报警性质',
    `treat_mode`        int                   DEFAULT '0' COMMENT '处理方式',
    `treat_state`       int                   DEFAULT '1' COMMENT '处理状态',
    `alarm_state`       int                   DEFAULT '0' COMMENT '报警状态',
    `group_code`        varchar(50)           DEFAULT NULL,
    `condition`         json                  DEFAULT NULL COMMENT '触发和恢复条件',
    `trigger_info`      json                  DEFAULT NULL COMMENT '触发值信息',
    `end_info`          json                  DEFAULT NULL COMMENT '恢复值信息',
    `trigger_time`      datetime              DEFAULT NULL COMMENT '报警触发时间',
    `end_time`          datetime              DEFAULT NULL COMMENT '恢复时间',
    `order_id`          varchar(50)           DEFAULT NULL,
    `order_state`       varchar(10)           DEFAULT NULL,
    `supplement`        json                  DEFAULT NULL COMMENT '补充说明',
    `target_id`         varchar(100)          DEFAULT NULL,
    `extend_info`       varchar(255)          DEFAULT NULL COMMENT '房间等信息',
    `syn_time`          datetime              DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_index` (`item_code`, `obj_id`, `trigger_time`) USING BTREE,
    KEY `index2` (`item_code`) USING BTREE,
    KEY `index3` (`obj_id`) USING BTREE,
    KEY `index4` (`trigger_time`) USING BTREE,
    KEY `index5` (`class_code`) USING BTREE,
    KEY `category` (`category`) USING BTREE,
    KEY `project_id` (`project_id`) USING BTREE,
    KEY `level` (`level`) USING BTREE,
    KEY `state` (`state`) USING BTREE,
    KEY `treat_state` (`treat_state`) USING BTREE,
    KEY `treat_mode` (`treat_mode`) USING BTREE,
    KEY `group_code` (`group_code`) USING BTREE,
    KEY `index_alarm_state` (`alarm_state`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='报警记录';

create table if not exists `project_alarm_rule`
(
    `id`                 varchar(64) NOT NULL COMMENT '主键',
    `group_id`           varchar(64) DEFAULT NULL COMMENT '分组id',
    `alarm_item_code`    varchar(50) DEFAULT NULL COMMENT '报警条目编码',
    `alarm_item_name`    varchar(50) DEFAULT NULL COMMENT '报警条目名称',
    `alarm_condition_id` varchar(64) DEFAULT NULL COMMENT '报警条件id',
    `belong`             int         DEFAULT '0' COMMENT '所属类型（0项目自定义、1集团标准）',
    `project_id`         varchar(50) DEFAULT NULL COMMENT '项目id',
    `ts`                 datetime    DEFAULT NULL COMMENT '乐观锁',
    `creator`            varchar(64) DEFAULT NULL COMMENT '创建人',
    `creation_time`      datetime    DEFAULT NULL COMMENT '创建时间',
    `modifier`           varchar(64) DEFAULT NULL COMMENT '修改人',
    `modified_time`      datetime    DEFAULT NULL COMMENT '修改时间',
    `valid`              int         DEFAULT '1' COMMENT '删除标志(0 否1 是)',
    `ext_info`           json        DEFAULT NULL COMMENT '扩展的信息',
    `syn_time`           datetime    DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='项目报警规则';


create table if not exists `project_alarm_rule_obj_rel`
(
    `id`                    varchar(64) NOT NULL COMMENT '主键',
    `project_alarm_rule_id` varchar(64) DEFAULT NULL COMMENT '项目报警规则id',
    `obj_id`                varchar(64) DEFAULT NULL COMMENT '设备ID',
    `obj_name`              varchar(64) DEFAULT NULL COMMENT '设备名称',
    `open`                  int         DEFAULT '1' COMMENT '屏蔽状态 1-open、0- 屏蔽',
    `project_id`            varchar(50) DEFAULT NULL COMMENT '项目id',
    `ts`                    datetime    DEFAULT NULL COMMENT '乐观锁',
    `creator`               varchar(64) DEFAULT NULL COMMENT '创建人',
    `creation_time`         datetime    DEFAULT NULL COMMENT '创建时间',
    `modifier`              varchar(64) DEFAULT NULL COMMENT '修改人',
    `modified_time`         datetime    DEFAULT NULL COMMENT '修改时间',
    `valid`                 int         DEFAULT '1' COMMENT '删除标志',
    `syn_time`              datetime    DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='项目报警规则与监测对象的关联';


create table if not exists `rc_basics_async_task`
(
    `id`                varchar(64) NOT NULL COMMENT '日志ID',
    `project_id`        varchar(64)          DEFAULT NULL COMMENT '项目ID',
    `architecture_code` varchar(64)          DEFAULT NULL COMMENT '建筑编码',
    `floor_code`        varchar(64)          DEFAULT NULL COMMENT '楼层编码',
    `room_code`         varchar(64)          DEFAULT NULL COMMENT '房间编码',
    `facility_code`     varchar(64)          DEFAULT NULL COMMENT '设备编码',
    `type`              varchar(64) NOT NULL COMMENT '配置类型',
    `sys`               varchar(64) NOT NULL COMMENT '执行系统 云端端还是本地某个系统',
    `task_st`           varchar(32) NOT NULL DEFAULT 'valid' COMMENT '配置状态 默认生效 valid',
    `sp_el`             varchar(128)         DEFAULT NULL COMMENT '匹配表达式(后续扩展使用)',
    `execute_st`        varchar(32) NOT NULL DEFAULT 'init' COMMENT '最新执行状态  init 初始化，fail 执行失败，succeed执行成功，underway执行中',
    `time_frequency`    int                  DEFAULT NULL COMMENT '执行评率（分钟）默认 60分钟',
    `start_time`        datetime             DEFAULT NULL COMMENT '任务开始时间',
    `end_time`          datetime             DEFAULT NULL COMMENT '任务结束时间',
    `ctx`               varchar(512)         DEFAULT NULL COMMENT '任务上下文',
    `extend_info`       varchar(512)         DEFAULT NULL COMMENT '扩展信息',
    `memo`              varchar(128)         DEFAULT NULL COMMENT '备注说明',
    `create_user`       varchar(64)          DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime             DEFAULT NULL COMMENT '创建时间',
    `update_time`       datetime             DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `syn_time`          datetime             DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk` (`update_time`, `syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='基础任务表（配置定时任务）';


create table if not exists `rc_basics_config`
(
    `id`                varchar(64) NOT NULL COMMENT '主键ID',
    `project_id`        varchar(64)          DEFAULT NULL COMMENT '项目ID',
    `type`              varchar(64) NOT NULL COMMENT '配置类型',
    `child_type`        varchar(64)          DEFAULT NULL COMMENT '子配置类型',
    `st`                varchar(32) NOT NULL DEFAULT 'valid' COMMENT '配置状态(有效无效)',
    `architecture_code` varchar(64)          DEFAULT NULL COMMENT '建筑编码',
    `floor_code`        varchar(64)          DEFAULT NULL COMMENT '楼层编码',
    `room_code`         varchar(64)          DEFAULT NULL COMMENT '房间编码',
    `facility_code`     varchar(64)          DEFAULT NULL COMMENT '设备编码',
    `system_id`         varchar(64)          DEFAULT NULL COMMENT '系统编号',
    `sp_el`             varchar(255)         DEFAULT NULL COMMENT '表达式',
    `start_time`        datetime             DEFAULT NULL COMMENT '配置生效时间',
    `end_time`          datetime             DEFAULT NULL COMMENT '配置失效时间',
    `md`                varchar(255)         DEFAULT NULL COMMENT '配置结果MD5(配置结果的MD5用于配置幂等判断)',
    `result`            text                 DEFAULT NULL COMMENT '配置结果',
    `extend_info`       text                 DEFAULT NULL COMMENT '扩展字段',
    `memo`              varchar(255)         DEFAULT NULL COMMENT '配置备注',
    `create_user`       varchar(64)          DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime             DEFAULT NULL COMMENT '创建时间',
    `update_user`       varchar(64)          DEFAULT NULL COMMENT '修改人',
    `update_time`       datetime             DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `syn_time`          datetime             DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk` (`update_time`, `syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='基础配置表';

create table if not exists `rc_basics_config_log`
(
    `id`                varchar(64) NOT NULL COMMENT '主键ID',
    `parent_id`         varchar(64)  DEFAULT NULL COMMENT '配置变更前配置ID',
    `project_id`        varchar(64)  DEFAULT NULL COMMENT '项目ID',
    `type`              varchar(50)  DEFAULT NULL COMMENT '配置类型',
    `child_type`        varchar(50)  DEFAULT NULL COMMENT '子配置类型',
    `st`                varchar(255) DEFAULT NULL COMMENT '配置状态(有效无效)',
    `architecture_code` varchar(64)  DEFAULT NULL COMMENT '建筑编码',
    `floor_code`        varchar(64)  DEFAULT NULL COMMENT '楼层编码',
    `room_code`         varchar(64)  DEFAULT NULL COMMENT '房间编码',
    `facility_code`     varchar(64)  DEFAULT NULL COMMENT '设备编码',
    `system_id`         varchar(64)  DEFAULT NULL COMMENT '系统编号',
    `sp_el`             varchar(255) DEFAULT NULL COMMENT '表达式',
    `start_time`        datetime     DEFAULT NULL COMMENT '配置生效时间',
    `end_time`          datetime     DEFAULT NULL COMMENT '配置失效时间',
    `md`                varchar(255) DEFAULT NULL COMMENT '配置结果MD5(配置结果的MD5用于配置幂等判断)',
    `result`            text         DEFAULT NULL COMMENT '配置结果',
    `extend_info`       text         DEFAULT NULL COMMENT '扩展字段',
    `memo`              varchar(255) DEFAULT NULL COMMENT '配置备注',
    `create_user`       varchar(125) DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime     DEFAULT NULL COMMENT '创建时间',
    `syn_time`          datetime     DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk` (`parent_id`) USING BTREE,
    KEY `nk_time` (`syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='配置日志表';


create table if not exists `rc_basics_relation`
(
    `id`          varchar(64)  NOT NULL COMMENT '主键ID',
    `type`        varchar(64)  NOT NULL COMMENT '关系类型',
    `obj_from`    varchar(256) NOT NULL COMMENT '被关联ID',
    `obj_to`      varchar(256) NOT NULL COMMENT '关联对象ID',
    `extend_info` text COMMENT '扩展信息',
    `create_time` datetime     NOT NULL COMMENT '创建时间',
    `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime     NOT NULL COMMENT '更新时间',
    `update_user` varchar(64) DEFAULT NULL COMMENT '更新人',
    `sync_time`   datetime    DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `rc_basics_relation_type_obj_from_obj_to_uindex` (`type`, `obj_from`, `obj_to`) COMMENT '关系类型-被关联ID-关联对象ID',
    UNIQUE KEY `rc_basics_relation_type_sync_time_obj_from_obj_to_index` (`type`, `sync_time`, `obj_from`, `obj_to`) COMMENT '关系类型-同步时间-被关联ID-关联对象ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='基础关系表';

create table if not exists `rc_eq_info_code_cfg`
(
    `id`               varchar(64) NOT NULL COMMENT '配置ID',
    `project_id`       varchar(64)  DEFAULT NULL COMMENT '项目id',
    `equipment_id`     varchar(64)  DEFAULT NULL COMMENT '设备id',
    `class_code`       varchar(64)  DEFAULT NULL COMMENT '设备类',
    `info_code`        varchar(64)  DEFAULT NULL COMMENT '信息点',
    `name`             varchar(128) DEFAULT NULL COMMENT '信息点名称',
    `alias`            varchar(128) DEFAULT NULL COMMENT '信息点别名',
    `unit`             varchar(64)  DEFAULT NULL COMMENT '单位名称',
    `scale`            double       DEFAULT NULL COMMENT '换算系数。页面展示的数值=采集数据*换算系数',
    `display_position` varchar(128) DEFAULT NULL COMMENT '展示位置，枚举类型。如页面展示的数值=采集数据*换算系数',
    `max`              int          DEFAULT NULL COMMENT '最大值',
    `min`              int          DEFAULT NULL COMMENT '最小值',
    `ordered`          int          DEFAULT NULL COMMENT '展示顺序',
    `parameter_type`   varchar(32)  DEFAULT NULL COMMENT '参数类型：运行参数，设定参数',
    `extend_info`      varchar(512) DEFAULT NULL COMMENT '扩展信息',
    `create_time`      datetime     DEFAULT NULL COMMENT '创建时间',
    `sys_time`         datetime     DEFAULT NULL COMMENT '同步时间',
    `system_id`        varchar(64)  DEFAULT NULL COMMENT '系统Id',
    `group_info`       varchar(64)  DEFAULT NULL COMMENT '分组信息',
    `enum`             varchar(255) DEFAULT NULL COMMENT '状态,枚举类型.',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_time` (`sys_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='设备信息点配置 已经绑定的设备信息通过rc_info_code_cfg配置生成';

CREATE TABLE IF NOT EXISTS `rc_info_code_cfg`
(
    `id`               varchar(64) NOT NULL COMMENT '配置ID',
    `group_code`       varchar(64)          DEFAULT NULL COMMENT '集团编码',
    `project_id`       varchar(32)          DEFAULT NULL COMMENT '项目id',
    `data_type`        varchar(32) NOT NULL COMMENT '数据类型, grouping 分组，info 信息点',
    `system_id`        varchar(64)          DEFAULT NULL COMMENT '系统ID',
    `system_type`      varchar(64)          DEFAULT NULL COMMENT '系统类型',
    `class_code`       varchar(20)          DEFAULT NULL COMMENT '设备类编码',
    `class_name`       varchar(128)         DEFAULT NULL COMMENT '设备类名称',
    `info_code`        varchar(64)          DEFAULT NULL COMMENT '信息点编码',
    `name`             varchar(128)         DEFAULT NULL COMMENT '信息点名称',
    `alias`            varchar(128)         DEFAULT NULL COMMENT '信息点别名',
    `feedback_code`    varchar(64)          DEFAULT NULL COMMENT '反馈信息点编码',
    `unit`             varchar(32)          DEFAULT NULL COMMENT 'IOT数据单位',
    `scale`            double               DEFAULT NULL COMMENT '换算系数',
    `iot_decimal`      int                  DEFAULT NULL COMMENT 'IOT数据保留的小数位',
    `max`              int                  DEFAULT NULL COMMENT '最大值限制',
    `min`              int                  DEFAULT NULL COMMENT '最小值限制',
    `ordered`          varchar(32)          DEFAULT NULL COMMENT '显示顺序',
    `group_info`       varchar(64)          DEFAULT NULL COMMENT '分组信息',
    `parameter_type`   varchar(32)          DEFAULT NULL COMMENT '参数类型，与BDTP保持一致',
    `display_position` json                 DEFAULT NULL COMMENT '展示位置',
    `force_info`       json                 DEFAULT NULL COMMENT '该信息点对应的强控信息点集合',
    `data_source`      json                 DEFAULT NULL COMMENT '枚举类型信息点的数据源',
    `extend_info`      json                 DEFAULT NULL COMMENT '扩展信息',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `syn_time`         datetime             DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_info_code_system_id` (`system_id`) USING BTREE COMMENT '系统级别索引',
    KEY `nk_info_code_project_id` (`project_id`, `group_code`) USING BTREE COMMENT '项目级别索引',
    KEY `nk_info_code_syn_time` (`syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT = '信息点配置';

create table if not exists `rc_intelligent_control_percentage`
(
    `id`                    varchar(64) NOT NULL COMMENT '主键ID',
    `system_id`             varchar(64) NOT NULL COMMENT '对象ID',
    `type`                  varchar(64) NOT NULL COMMENT '关系类型',
    `data_time`             datetime    NOT NULL COMMENT '数据时间',
    `self_control_duration` bigint      NOT NULL COMMENT '自控运行时长',
    `run_duration`          bigint      NOT NULL COMMENT '运行时长',
    `intact`                varchar(64) NOT NULL COMMENT '是否是完整数据',
    `extend_info`           text COMMENT '扩展信息',
    `create_time`           datetime    NOT NULL COMMENT '创建时间',
    `update_time`           datetime    NOT NULL COMMENT '更新时间',
    `sync_time`             datetime DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`),
    KEY `rc_intelligent_control_percentage_multi_index` (`type`, `data_time` DESC, `system_id`, `self_control_duration`,
                                                         `intact`) COMMENT '关系类型-数据时间-对象ID-比例-是否是完整数据'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='自控运行占比计算表';

CREATE TABLE IF NOT EXISTS `rc_object_relation_cfg`
(
    `id`                varchar(64)  NOT NULL COMMENT '配置ID',
    `group_code`        varchar(64)           DEFAULT NULL COMMENT '集团编码',
    `project_id`        varchar(32)           DEFAULT NULL COMMENT '项目id',
    `system_id`         varchar(64)           DEFAULT NULL COMMENT '系统ID',
    `system_type`       varchar(64)           DEFAULT NULL COMMENT '系统类型',
    `master_class_code` varchar(20)  NOT NULL COMMENT '主设备类编码-需要进行关联设备查询的主对象对应的设备类编码',
    `master_class_name` varchar(128) NOT NULL COMMENT '主设备类名称-需要进行关联设备查询的主对象对应的设备类名称',
    `slave_class_code`  varchar(20)  NOT NULL COMMENT '从设备类编码-关系对象的设备类编码',
    `slave_class_name`  varchar(128) NOT NULL COMMENT '从设备类名称-关系对象的设备类名称',
    `graph_code`        varchar(64)  NOT NULL COMMENT '图类型编码',
    `graph_name`        varchar(128) NOT NULL COMMENT '图类型名称',
    `rel_code`          varchar(64)  NOT NULL COMMENT '边类型编码',
    `rel_name`          varchar(128) NOT NULL COMMENT '边类型名称',
    `rel_direction`     varchar(10)  NOT NULL COMMENT '关系方向，主或从，from-主，to-从',
    `group_name`        varchar(128) NOT NULL COMMENT '分组名称-实指关系类型的名称',
    `extend_info`       json                  DEFAULT NULL COMMENT '扩展信息-保留字段',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `syn_time`          datetime              DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_object_relation_system_id` (`system_id`) USING BTREE COMMENT '系统级别索引',
    KEY `nk_object_relation_project_id` (`project_id`, `group_code`) USING BTREE COMMENT '项目级别索引',
    KEY `nk_object_relation_syn_time` (`syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT = '关联设备查询-关系配置';


CREATE TABLE IF NOT EXISTS `rc_sys_log`
(
    `id`             varchar(64) NOT NULL COMMENT '配置ID',
    `group_code`     varchar(64)          DEFAULT NULL COMMENT '集团编码',
    `project_id`     varchar(32)          DEFAULT NULL COMMENT '项目id',
    `system_id`      varchar(64)          DEFAULT NULL COMMENT '系统ID',
    `system_type`    varchar(64)          DEFAULT NULL COMMENT '系统类型',
    `object_id`      varchar(64)          DEFAULT NULL COMMENT '对象ID',
    `local_id`       varchar(64)          DEFAULT NULL COMMENT '对象本地编码',
    `local_name`     varchar(128)         DEFAULT NULL COMMENT '对象本地名称',
    `class_code`     varchar(20)          DEFAULT NULL COMMENT '设备类编码',
    `class_name`     varchar(128)         DEFAULT NULL COMMENT '设备类名称',
    `log_source`     varchar(64)          DEFAULT NULL COMMENT '日志来源，LogSourceEnum，系统或用户',
    `log_type`       varchar(64)          DEFAULT NULL COMMENT '日志类型，LogTypeEnum',
    `data_type`      varchar(64)          DEFAULT NULL COMMENT '数据类型，DataTypeEnum',
    `operate_type`   varchar(64)          DEFAULT NULL COMMENT '操作类型，OperateTypeEnum',
    `operate_reason` varchar(255)         DEFAULT NULL COMMENT '操作原因',
    `point_info`     json                 DEFAULT NULL COMMENT '指令详情',
    `content_info`   text                 DEFAULT NULL COMMENT '文本内容，一般是前端展示需要',
    `result_info`    text                 DEFAULT NULL COMMENT '操作结果，失败原因等',
    `ip`             varchar(255)         DEFAULT NULL COMMENT '接口ip地址',
    `extend_info`    json                 DEFAULT NULL COMMENT '扩展信息',
    `create_user`    varchar(64)          DEFAULT NULL COMMENT '任务类型',
    `create_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `syn_time`       datetime             DEFAULT NULL comment '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_sys_log_system_id` (`object_id`) USING BTREE COMMENT '对象级别索引',
    KEY `nk_sys_log_project_id` (`project_id`, `group_code`) USING BTREE COMMENT '项目级别索引',
    KEY `nk_sys_log_syn_time` (`syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT = '通用操作日志记录表';

CREATE TABLE IF NOT EXISTS `rc_system_pic`
(
    `id`           varchar(64) NOT NULL COMMENT '配置ID',
    `group_code`   varchar(64)          DEFAULT NULL COMMENT '集团编码',
    `project_id`   varchar(32)          DEFAULT NULL COMMENT '项目id',
    `object_id`    varchar(64)          DEFAULT NULL COMMENT '对象ID，指的是这系统图是提供给谁查询的',
    `name`         varchar(64)          DEFAULT NULL COMMENT '名称，指的是这个系统图数据的名称',
    `type`         varchar(20)          DEFAULT NULL comment '图片类型：system，model，equipment',
    `width`        double               DEFAULT NULL comment '宽',
    `height`       double               DEFAULT NULL comment '高',
    `picture_code` longtext             DEFAULT NULL comment '图片base64字符串',
    `compressed`   Boolean              DEFAULT NULL comment '结果集是否进行了压缩，true-是',
    `class_code`   varchar(64)          DEFAULT NULL comment '设备类',
    `class_status` varchar(32)          DEFAULT NULL comment '设备状态',
    `deviation`    varchar(512)         DEFAULT NULL comment '偏差量，表示设备管道的连接位置',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `create_user`  varchar(64)          DEFAULT NULL comment '创建人',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '更新时间',
    `update_user`  varchar(64)          DEFAULT NULL comment '更新人',
    `syn_time`     datetime             DEFAULT NULL comment '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_system_pic_object_id` (`object_id`) USING BTREE COMMENT '对象级别索引',
    KEY `nk_system_pic_project_id` (`project_id`, `group_code`) USING BTREE COMMENT '项目级别索引',
    KEY `nk_system_pic_syn_time` (`syn_time`) USING BTREE COMMENT '数据同步校验索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT = '系统图表';

create table if not exists `rc_user`
(
    `user_id`    varchar(50) NOT NULL COMMENT '用户ID',
    `project_id` varchar(50) NOT NULL COMMENT '项目编号',
    `user_name`  varchar(125) DEFAULT NULL COMMENT '用户名称',
    `password`   varchar(255) DEFAULT NULL COMMENT '密码（MD5密码）',
    `functions`  json         DEFAULT NULL COMMENT '用户权限',
    `state`      varchar(50)  DEFAULT NULL COMMENT '状态',
    `lock_state` varchar(255) DEFAULT NULL COMMENT '账号是否锁定，0-未锁定，1-锁定',
    `sys_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '同步时间',
    `valid_time` datetime     DEFAULT NULL COMMENT '账号生效时间',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='本地用户信息表';

create table if not exists `strategy_cfg`
(
    `id`            int         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_id`    varchar(64) NOT NULL COMMENT '项目ID',
    `system_type`   varchar(64) NOT NULL COMMENT '系统类型',
    `system_id`     varchar(64) NOT NULL COMMENT '系统ID',
    `business_type` varchar(32) NOT NULL COMMENT '业务类型',
    `strategy_type` varchar(32) NOT NULL COMMENT '策略类型',
    `object_id`     varchar(64) NOT NULL COMMENT '对象ID',
    `class_code`    varchar(64)          DEFAULT NULL COMMENT '设备类编码',
    `st`            varchar(16) NOT NULL DEFAULT 'valid' COMMENT '配置状态',
    `memo`          text COMMENT '备注说明',
    `create_user`   varchar(64)          DEFAULT NULL COMMENT '创建人',
    `update_user`   varchar(64)          DEFAULT NULL COMMENT '修改人',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `update_time`   datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `strategy_cfg_index` (`project_id`, `system_type`, `system_id`, `business_type`, `strategy_type`, `object_id`,
                              `st`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='策略配置表';


create table if not exists `strategy_date_cfg`
(
    `id`              varchar(64) NOT NULL COMMENT '主键ID',
    `strategy_id`     int         NOT NULL COMMENT '关联策略配置ID',
    `date`            date        NOT NULL COMMENT '策略日期',
    `ignore_plan_ids` text COMMENT '忽略计划ID',
    `st`              varchar(16) NOT NULL COMMENT '配置状态',
    `create_time`     datetime    NOT NULL COMMENT '创建时间',
    `update_time`     datetime    NOT NULL COMMENT '更新时间',
    `create_user`     varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_user`     varchar(16) DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `strategy_date_cfg_uindex` (`strategy_id`, `date`, `st`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='策略日期表';

create table if not exists `strategy_plan_cfg`
(
    `id`          int         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `strategy_id` int         NOT NULL COMMENT '关联策略配置ID',
    `start_time`  datetime    DEFAULT NULL COMMENT '策略生效时间',
    `end_time`    datetime    DEFAULT NULL COMMENT '策略失效时间',
    `result`      json        NOT NULL COMMENT '计划数据',
    `st`          varchar(16) NOT NULL COMMENT '配置状态',
    `create_time` datetime    NOT NULL COMMENT '创建时间',
    `update_time` datetime    NOT NULL COMMENT '更新时间',
    `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_user` varchar(64) DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `strategy_plan_cfg_uindex` (`strategy_id`, `st`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='策略计划表';