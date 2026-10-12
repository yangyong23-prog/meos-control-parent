-- ============================================================
-- 清理 cache_* 表重复缓存数据（v7.260802.0 修复补充）
-- 任务: 08-24-cache-template-duplicate-fix
-- 说明:
--   1. 执行前必须先备份（导出整表或受影响项目数据）
--   2. 保留每组业务键中 id 最大（最新）的一条，其余删除
--   3. 兼容 MySQL / 人大金仓（Kingbase）
--   4. 执行后建议对受影响项目重新触发一次缓存重建，确保缓存一致
-- ============================================================

-- ============================================================
-- 1. cache_info_code_template
-- 重复判定: (project_id, custom_key)
-- 修复: CacheRebuild 锁 + 去重 Collectors.toMap
-- ============================================================

-- 预检查
-- select project_id, custom_key, count(*) as duplicate_cnt
-- from cache_info_code_template
-- group by project_id, custom_key
-- having count(*) > 1
-- order by duplicate_cnt desc;

-- 清理
delete from cache_info_code_template
where id not in (
    select keep_id
    from (
        select max(id) as keep_id
        from cache_info_code_template
        group by project_id, custom_key
        having count(*) > 1
    ) duplicate_groups
);

-- 清理后核对
-- select project_id, custom_key, count(*)
-- from cache_info_code_template
-- group by project_id, custom_key
-- having count(*) > 1;

-- ============================================================
-- 2. cache_object_key_rel
-- 重复判定: (project_id, cache_key, object_id)
-- 修复: CacheObjectModelWriteServiceImpl replaceModelCacheKey/replaceObjectRelations 加锁
-- ============================================================

-- 预检查
-- select project_id, cache_key, object_id, count(*) as duplicate_cnt
-- from cache_object_key_rel
-- group by project_id, cache_key, object_id
-- having count(*) > 1
-- order by duplicate_cnt desc;

-- 清理
delete from cache_object_key_rel
where id not in (
    select keep_id
    from (
        select max(id) as keep_id
        from cache_object_key_rel
        group by project_id, cache_key, object_id
        having count(*) > 1
    ) duplicate_groups
);

-- 清理后核对
-- select project_id, cache_key, object_id, count(*)
-- from cache_object_key_rel
-- group by project_id, cache_key, object_id
-- having count(*) > 1;

-- ============================================================
-- 3. cache_object_rel
-- 重复判定: (project_id, object_id, graph_code, rel_code, rel_object_id)
-- 修复: CacheObjectModelWriteServiceImpl replaceModelCacheKey/replaceObjectRelations 加锁
-- ============================================================

-- 预检查
-- select project_id, object_id, graph_code, rel_code, rel_object_id,
--        count(*) as duplicate_cnt
-- from cache_object_rel
-- group by project_id, object_id, graph_code, rel_code, rel_object_id
-- having count(*) > 1
-- order by duplicate_cnt desc;

-- 清理
delete from cache_object_rel
where id not in (
    select keep_id
    from (
        select max(id) as keep_id
        from cache_object_rel
        group by project_id, object_id, graph_code, rel_code, rel_object_id
        having count(*) > 1
    ) duplicate_groups
);

-- 清理后核对
-- select project_id, object_id, graph_code, rel_code, rel_object_id,
--        count(*)
-- from cache_object_rel
-- group by project_id, object_id, graph_code, rel_code, rel_object_id
-- having count(*) > 1;