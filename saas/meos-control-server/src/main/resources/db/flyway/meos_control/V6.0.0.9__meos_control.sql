create table if not exists demand_lighting_strategy_config
(
    id            int auto_increment comment '唯一ID' primary key,
    project_id    varchar(64)                        not null comment '项目ID',
    building_id   varchar(64)                        null comment '建筑ID',
    floor_id      varchar(64)                        null comment '楼层ID',
    lighting_type varchar(32)                        not null comment '照明类型',
    calendar_tag  varchar(32)                        not null comment '工作历标签',
    strategy_type varchar(32)                        not null comment '策略类型',
    strategy_name varchar(64) default 'DEFAULT_NAME' not null comment '策略名称',
    config_data   json                               not null comment '配置信息',
    st            varchar(32) default 'valid'        not null comment '状态',
    create_user   varchar(64)                        null comment '创建人',
    update_user   varchar(64)                        null comment '更新人',
    create_time   datetime                           not null comment '创建时间',
    update_time   datetime                           not null comment '更新时间'
) comment '照明需求策略配置表';

call add_table_index('demand_lighting_strategy_config', 'demand_lighting_strategy_config_pk',
                     'create unique index demand_lighting_strategy_config_pk on demand_lighting_strategy_config (project_id, lighting_type, calendar_tag, strategy_type, st, building_id, floor_id);')