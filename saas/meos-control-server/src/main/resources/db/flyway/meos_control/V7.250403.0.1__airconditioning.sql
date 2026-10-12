CREATE TABLE IF NOT EXISTS equip_record
(
    `id`           int                    NOT NULL AUTO_INCREMENT,
    group_code     varchar(50)            null comment '集团编码',
    project_id     varchar(50)            null comment '项目ID',
    system_id      varchar(100)           null comment '系统ID',
    system_code    varchar(50)            null comment '系统编码',
    obj_id         varchar(100)           null comment '设备对象ID',
    `class_code`   varchar(50) DEFAULT NULL COMMENT '对象类编码',
    change_content json                   null comment '指令下发内容',
    exec_status    int         default -1 null comment '执行状态: 默认 -1 没有执行;0：执行失败;1：执行成功',
    creator        varchar(50)            null comment '创建人',
    creation_time  varchar(14)            null comment '创建时间',
    modified_time  varchar(50)            null comment '修改时间',
    modifier       varchar(50)            null comment '修改人',
    extend         json                   null comment '扩展字段',
    info_code      varchar(200) as (json_unquote(json_extract(`change_content`, '$.infoCode'))),
    PRIMARY KEY (`id`) USING BTREE,
    KEY `equip_record_group_code_project_id_obj_id_exec_status_index` (project_id, obj_id, exec_status) USING BTREE COMMENT '对象执行状态',
    KEY `idx_info_code` (info_code, exec_status) USING BTREE COMMENT '信息点编码'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  ROW_FORMAT = DYNAMIC COMMENT = '设备异步指令下发表';
