# ------------------------- optimize table cache_info_code_template -------------------------
# 模板缓存按项目内唯一 Key 查询，不能依赖包含运行监测类型的旧索引做项目范围扫描。
call add_table_index('cache_info_code_template', 'idx_custom_key',
                     'create index idx_custom_key on cache_info_code_template (project_id, custom_key);');
call drop_table_index('cache_info_code_template', 'idx_2');

# ------------------------- create table cache_object -------------------------
create table if not exists cache_object
(
    id                             varchar(64)  not null comment '对象ID' primary key,
    project_id                     varchar(50)  not null comment '项目ID',
    local_name                     varchar(100) null comment '对象名称',
    class_code                     varchar(32)  not null comment '对象类编码',
    object_type                    varchar(32)  not null comment '对象类型',
    floor_id                       varchar(64)  null comment '楼层ID',
    building_id                    varchar(64)  null comment '建筑ID',
    monitoring_surveillance_status varchar(64) null comment '监测监控状态码',
    object_data                    json         null comment '对象扩展数据',
    create_time                    datetime(6)  not null comment '创建时间',
    create_user                    varchar(64)  null comment '创建人',
    update_time                    datetime(6)  not null comment '更新时间',
    update_user                    varchar(64)  null comment '更新人'
) comment '缓存对象表';
call add_table_index('cache_object', 'idx_project',
                     'create index idx_project on cache_object (project_id);');
call add_table_index('cache_object', 'idx_monitoring_status',
                     'create index idx_monitoring_status on cache_object (project_id, monitoring_surveillance_status);');
call add_table_index('cache_object', 'idx_building',
                     'create index idx_building on cache_object (project_id, building_id);');
call add_table_index('cache_object', 'idx_floor',
                     'create index idx_floor on cache_object (project_id, floor_id);');

# ------------------------- create table cache_object_key_rel -------------------------
create table if not exists cache_object_key_rel
(
    id          bigint       not null comment 'ID' primary key,
    project_id  varchar(50)  not null comment '项目ID',
    cache_key   varchar(255) not null comment '缓存键',
    object_id   varchar(64)  not null comment '对象ID',
    create_time datetime(6)  not null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    update_time datetime(6)  not null comment '更新时间',
    update_user varchar(64)  null comment '更新人'
) comment '缓存键与对象关系表';
call add_table_index('cache_object_key_rel', 'idx_cache_key',
                     'create index idx_cache_key on cache_object_key_rel (project_id, cache_key);');
call add_table_index('cache_object_key_rel', 'idx_object',
                     'create index idx_object on cache_object_key_rel (project_id, object_id);');

# ------------------------- create table cache_object_rel -------------------------
create table if not exists cache_object_rel
(
    id            bigint      not null comment 'ID' primary key,
    project_id    varchar(50) not null comment '项目ID',
    object_id     varchar(64) not null comment '对象ID',
    graph_code    varchar(64) not null comment '图谱编码',
    rel_code      varchar(64) not null comment '关系编码',
    rel_object_id varchar(64) not null comment '关联对象ID',
    create_time   datetime(6) not null comment '创建时间',
    create_user   varchar(64) null comment '创建人',
    update_time   datetime(6) not null comment '更新时间',
    update_user   varchar(64) null comment '更新人'
) comment '缓存对象关系表';
call add_table_index('cache_object_rel', 'idx_object',
                     'create index idx_object on cache_object_rel (project_id, object_id, graph_code, rel_code);');
call add_table_index('cache_object_rel', 'idx_rel_object',
                     'create index idx_rel_object on cache_object_rel (project_id, rel_object_id, graph_code, rel_code);');

# ------------------------- create table cache_object_model_view -------------------------
create table if not exists cache_object_model_view
(
    id          varchar(64)  not null comment '对象ID' primary key,
    project_id  varchar(50)  not null comment '项目ID',
    local_name  varchar(100) null comment '对象名称',
    class_code  varchar(32)  not null comment '对象类编码',
    object_type varchar(32)  not null comment '对象类型',
    building_id varchar(64)  null comment '建筑ID',
    floor_id    varchar(64)  null comment '楼层ID',
    view_data   json         null comment '模型视图扩展数据',
    create_time datetime(6)  not null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    update_time datetime(6)  not null comment '更新时间',
    update_user varchar(64)  null comment '更新人'
) comment '缓存对象模型视图表';
call add_table_index('cache_object_model_view', 'idx_project',
                     'create index idx_project on cache_object_model_view (project_id);');
call add_table_index('cache_object_model_view', 'idx_building',
                     'create index idx_building on cache_object_model_view (project_id, building_id);');
call add_table_index('cache_object_model_view', 'idx_floor',
                     'create index idx_floor on cache_object_model_view (project_id, floor_id);');

# ------------------------- create table cache_template_object_detail -------------------------
create table if not exists cache_template_object_detail
(
    id           bigint       not null comment 'ID' primary key,
    project_id   varchar(50)  not null comment '项目ID',
    template_key varchar(255) not null comment '模板键',
    object_id    varchar(64)  not null comment '对象ID',
    info_code    varchar(64)  not null comment '信息点编码',
    result_json  json         not null comment '取值结果',
    create_time  datetime(6)  not null comment '创建时间',
    create_user  varchar(64)  null comment '创建人',
    update_time  datetime(6)  not null comment '更新时间',
    update_user  varchar(64)  null comment '更新人'
) comment '缓存模板对象取值明细表';
call add_table_index('cache_template_object_detail', 'idx_template_object_info',
                     'create index idx_template_object_info on cache_template_object_detail (project_id, template_key, object_id, info_code);');

# ------------------------- create table cache_rule -------------------------
create table if not exists cache_rule
(
    id          bigint      not null comment 'ID' primary key,
    project_id  varchar(50) not null comment '项目ID',
    object_id   varchar(64) not null comment '对象ID',
    info_code   varchar(64) not null comment '信息点编码',
    type        varchar(32) not null comment '规则类型',
    create_time datetime(6) not null comment '创建时间',
    create_user varchar(64) null comment '创建人',
    update_time datetime(6) not null comment '更新时间',
    update_user varchar(64) null comment '更新人'
) comment '缓存规则表';
call add_table_index('cache_rule', 'idx_project_object_rule',
                     'create index idx_project_object_rule on cache_rule (project_id, object_id, info_code, type);');

# ------------------------- create table cache_rule_key_rel -------------------------
create table if not exists cache_rule_key_rel
(
    id                          bigint       not null comment 'ID' primary key,
    project_id                  varchar(50)  not null comment '项目ID',
    cache_key                   varchar(255) not null comment '缓存键',
    rule_id                     bigint       not null comment '规则ID',
    create_time                 datetime(6)  not null comment '创建时间',
    create_user                 varchar(64)  null comment '创建人',
    update_time                 datetime(6)  not null comment '更新时间',
    update_user                 varchar(64)  null comment '更新人'
) comment '缓存键与规则关系表';
call add_table_index('cache_rule_key_rel', 'idx_cache_key',
                     'create index idx_cache_key on cache_rule_key_rel (project_id, cache_key, rule_id);');
call add_table_index('cache_rule_key_rel', 'idx_rule',
                     'create index idx_rule on cache_rule_key_rel (project_id, rule_id, cache_key);');
