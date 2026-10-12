package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRwdObjectQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.mapper.CacheRwdObjectMapper;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 14:53
 **/
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdObjectRepositoryImpl extends ServiceImpl<CacheRwdObjectMapper, CacheRwdObject> implements CacheRwdObjectRepository, BizComponent<CacheRwdObjectRepository> {

    @Override
    public String logTag() {
        return "CacheRwdObjectRepositoryImpl";
    }

    public List<CacheRwdObject> getByCustomIndexKey(@NonNull String customIndexKey) {
        logProjectInstance().debug("获取缓存对象", Pair.of("customIndexKey", customIndexKey));
        String projectId = ContextUtil.getProjectId();
        List<CacheRwdObject> result = new ArrayList<>();
        int pageSize = 2000;
        int pageNo = 1;
        List<CacheRwdObject> pageRecords;

        do {
            Page<CacheRwdObject> page = new Page<>(pageNo, pageSize, false);
            pageRecords = this.lambdaQuery()
                    .eq(CacheRwdObject::getProjectId, projectId)
                    .eq(CacheRwdObject::getCustomIndexKey, customIndexKey)
                    .page(page)
                    .getRecords();
            if (pageRecords != null && !pageRecords.isEmpty()) {
                result.addAll(pageRecords);
            }
            pageNo++;
        } while (pageRecords != null && pageRecords.size() == pageSize);

        return result;
    }

    @Override
    public List<CacheRwdObject> list(@NonNull CacheRwdObjectQueryDaoParam queryDaoParam) {
        return this.lambdaQuery()
                .select(queryDaoParam.toSelectFunctions())
                .eq(CacheRwdObject::getProjectId, ContextUtil.getProjectId())
                .eq(queryDaoParam.getObjectType() != null,
                        CacheRwdObject::getObjectType, queryDaoParam.getObjectType())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getObjectTypes()),
                        CacheRwdObject::getObjectType, queryDaoParam.getObjectTypes())
                .eq(StringUtils.isNotBlank(queryDaoParam.getObjectId()),
                        CacheRwdObject::getObjectId, queryDaoParam.getObjectId())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getObjectIds()),
                        CacheRwdObject::getObjectId, queryDaoParam.getObjectIds())
                .eq(queryDaoParam.getRunningMonitorType() != null,
                        CacheRwdObject::getRunningMonitorType, queryDaoParam.getRunningMonitorType())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getRunningMonitorTypes()),
                        CacheRwdObject::getRunningMonitorType, queryDaoParam.getRunningMonitorTypes())
                .eq(StringUtils.isNotBlank(queryDaoParam.getCustomIndexKey()),
                        CacheRwdObject::getCustomIndexKey, queryDaoParam.getCustomIndexKey())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getCustomIndexKeys()),
                        CacheRwdObject::getCustomIndexKey, queryDaoParam.getCustomIndexKeys())
                .list();
    }

    @Override
    public int removeByCustomIndexKey(String customIndexKey) {
        LambdaUpdateWrapper<CacheRwdObject> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CacheRwdObject::getProjectId, ContextUtil.getProjectId())
                .eq(CacheRwdObject::getCustomIndexKey, customIndexKey);
        return this.baseMapper.delete(updateWrapper);
    }

    @Override
    public CacheRwdObject getOneByRunningMonitorType(RunningMonitorTypeEnum runningMonitorType) {
        if (runningMonitorType == null) {
            return null;
        }
        return this.lambdaQuery()
                .eq(CacheRwdObject::getProjectId, ContextUtil.getProjectId())
                .eq(CacheRwdObject::getRunningMonitorType, runningMonitorType)
                .orderByDesc(BaseDO::getUpdateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public Map<RunningMonitorTypeEnum, Long> countGroupByMonitorType() {
        String projectId = ContextUtil.getProjectId();
        Map<RunningMonitorTypeEnum, Long> result = new HashMap<>();

        QueryWrapper<CacheRwdObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("running_monitor_type as running_monitor_type", "count(*) as count")
                .eq("project_id", projectId)
                .groupBy("running_monitor_type");

        List<Map<String, Object>> maps = this.baseMapper.selectMaps(queryWrapper);

        for (Map<String, Object> map : maps) {
            String monitorTypeValue = (String) map.get("running_monitor_type");
            Long count = ((Number) map.get("count")).longValue();
            RunningMonitorTypeEnum monitorType = RunningMonitorTypeEnum.analyze(monitorTypeValue);
            if (monitorType != null) {
                result.put(monitorType, count);
            }
        }

        return result;
    }
}
