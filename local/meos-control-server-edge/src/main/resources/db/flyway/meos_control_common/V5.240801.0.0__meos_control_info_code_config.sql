# ------------------------- object_info_code_template_type -------------------------
create table if not exists `object_info_code_template_type`
(
    `id`                   bigint      not null comment 'id',
    `running_monitor_type` varchar(32) not null comment '运行监测类型',
    `template_type`        varchar(32) not null comment '模版类型',
    `object_type`          varchar(64) not null comment '对象类型',
    `class_code`           varchar(64) not null comment '设备类编码',
    `info_code_filter`     varchar(128) default null comment '信息点筛选',
    `create_time`          datetime    not null comment '创建时间',
    `create_user`          varchar(64)  default null comment '创建人',
    `update_time`          datetime    not null comment '更新时间',
    `update_user`          varchar(64)  default null comment '更新人',
    primary key (`id`),
    unique key `object_info_code_template_type_pk` (`running_monitor_type`, `template_type`, `class_code`,
                                                    `info_code_filter`),
    key `object_info_code_template_type_idx` (`running_monitor_type`, `template_type`, `object_type`, `class_code`)
) comment ='对象模版类型配置';
replace into `object_info_code_template_type`
select *
from equipment_info_code_template_type;

# ------------------------- object_info_code_group -------------------------
create table if not exists `object_info_code_group`
(
    `id`            bigint      not null comment 'id',
    `group_code`    varchar(64) not null comment '分组编码',
    `group_name`    varchar(64) not null comment '分组名称',
    `sequence`      int         not null comment '分组排序',
    `rel_parent_id` bigint      default null comment '关联上级分组id',
    `create_time`   datetime    not null comment '创建时间',
    `create_user`   varchar(64) default null comment '创建人',
    `update_time`   datetime    not null comment '更新时间',
    `update_user`   varchar(64) default null comment '更新人',
    primary key (`id`),
    unique key `object_info_code_group_pk` (`group_code`)
) comment ='对象信息点分组配置表';
replace into `object_info_code_group`
select *
from equipment_info_code_group;

# ------------------------- object_info_code_config -------------------------
create table if not exists `object_info_code_config`
(
    `id`                      bigint      not null comment 'id',
    `info_code`               varchar(64) not null comment '信息点编码',
    `info_code_type`          varchar(32) not null comment '信息点类型',
    `tack_value_logic`        json         default null comment '取值表达式',
    `show_style`              varchar(256) default null comment '展示样式',
    `sequence`                int         not null comment '信息点顺序',
    `rel_group_code`          varchar(64) not null comment '分组编码',
    `rel_info_code_config_id` bigint       default null comment '关联信息点配置id',
    `rel_template_type_id`    bigint      not null comment '关联模版类型id',
    `create_time`             datetime    not null comment '创建时间',
    `create_user`             varchar(64)  default null comment '创建人',
    `update_time`             datetime    not null comment '更新时间',
    `update_user`             varchar(64)  default null comment '更新人',
    primary key (`id`),
    key `object_info_code_config_idx` (`info_code_type`, `info_code`, `rel_group_code`, `rel_template_type_id`)
) comment ='对象信息点配置表';
replace into `object_info_code_config`
select *
from equipment_info_code_config;

# ------------------------- object_info_code_group -------------------------
call add_table_column('object_info_code_group', 'show_style',
                      'alter table `object_info_code_group` add `show_style` json null comment ''展示样式'' after `group_name`;');

# ------------------------- object_info_code_config -------------------------
call add_table_column('object_info_code_config', 'info_code_name',
                      'alter table `object_info_code_config` add `info_code_name` varchar(64) not null comment ''信息的名称'' after `info_code`;')