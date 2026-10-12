package com.persagy.apccontrol.basic.resource.demand.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigResultMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigResultService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultBusinessKey;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 需求配置预计算结果服务实现
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigResultServiceImpl
        extends BaseServiceImpl<DemandConfigResultMapper, DemandConfigResult>
        implements DemandConfigResultService {

    private static final int INSERT_BATCH_SIZE = 500;

    private static final int QUERY_BATCH_SIZE = 500;

    @Override
    public @NonNull List<DemandConfigResult> list(@NonNull DemandConfigResultRepositoryQueryParam param) {
        if (this.shouldPartition(param.getIds())) {
            return this.listByIdPartitions(param);
        }
        if (this.shouldPartition(param.getBusinessKeys())) {
            return this.listByBusinessKeyPartitions(param);
        }
        if (this.shouldPartition(param.getDemandInstanceIds())) {
            return this.listByDemandInstanceIdPartitions(param);
        }
        if (this.shouldPartitionInstanceTypeAndInstanceIds(param.getInstanceTypeAndInstanceIds())) {
            return this.listByInstanceTypeAndInstanceIdPartitions(param);
        }
        return this.listWithoutPartition(param);
    }

    @Override
    public @NonNull List<Long> listIds(@NonNull DemandConfigResultRepositoryQueryParam param, int limit) {
        return super.getBaseMapper().listIds(ContextUtil.getProjectId(), param, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertResults(@NonNull Collection<DemandConfigResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        List<DemandConfigResult> resultList = new ArrayList<>(results);
        // 结果表批量写入走手写XML，绕过MyBatis-Plus标准insert流程；
        // 先补齐主键和审计字段，再按固定大小分片，避免saveBatch在大数据量下过慢。
        for (DemandConfigResult result : resultList) {
            super.fillInsertDefaultValue(result);
        }
        for (List<DemandConfigResult> resultPartition : Lists.partition(
                resultList, DemandConfigResultServiceImpl.INSERT_BATCH_SIZE)) {
            super.getBaseMapper().insertBatch(resultPartition);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeResultByIds(@NonNull Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return super.removeByIds(ids);
    }

    /**
     * 判断集合查询条件是否需要分片。
     *
     * @param values 查询条件集合
     * @return 是否需要分片
     * @since v7.260701.0
     */
    private boolean shouldPartition(@Nullable Collection<?> values) {
        return CollectionUtils.isNotEmpty(values)
                && values.size() > DemandConfigResultServiceImpl.QUERY_BATCH_SIZE;
    }

    /**
     * 判断实例类型和实例ID查询条件是否需要分片。
     *
     * @param instanceTypeAndInstanceIds 实例类型和实例ID映射
     * @return 是否需要分片
     * @since v7.260701.0
     */
    private boolean shouldPartitionInstanceTypeAndInstanceIds(
            @Nullable Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds) {
        if (instanceTypeAndInstanceIds == null || instanceTypeAndInstanceIds.isEmpty()) {
            return false;
        }
        for (Set<String> instanceIds : instanceTypeAndInstanceIds.values()) {
            if (this.shouldPartition(instanceIds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按 ID 分片查询结果。
     *
     * @param param 查询参数
     * @return 查询结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listByIdPartitions(
            @NonNull DemandConfigResultRepositoryQueryParam param) {
        Collection<Long> ids = param.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> results = new ArrayList<>();
        for (List<Long> idPartition : Lists.partition(
                new ArrayList<>(ids), DemandConfigResultServiceImpl.QUERY_BATCH_SIZE)) {
            DemandConfigResultRepositoryQueryParam partitionParam = this.copyQueryParam(param);
            partitionParam.setIds(idPartition);
            results.addAll(this.listWithoutPartition(partitionParam));
        }
        return results;
    }

    /**
     * 按业务键分片查询结果。
     *
     * @param param 查询参数
     * @return 查询结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listByBusinessKeyPartitions(
            @NonNull DemandConfigResultRepositoryQueryParam param) {
        Collection<DemandConfigResultBusinessKey> businessKeys = param.getBusinessKeys();
        if (CollectionUtils.isEmpty(businessKeys)) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> results = new ArrayList<>();
        for (List<DemandConfigResultBusinessKey> businessKeyPartition : Lists.partition(
                new ArrayList<>(businessKeys), DemandConfigResultServiceImpl.QUERY_BATCH_SIZE)) {
            DemandConfigResultRepositoryQueryParam partitionParam = this.copyQueryParam(param);
            partitionParam.setBusinessKeys(businessKeyPartition);
            results.addAll(this.listWithoutPartition(partitionParam));
        }
        return results;
    }

    /**
     * 按需求实例 ID 分片查询结果。
     *
     * @param param 查询参数
     * @return 查询结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listByDemandInstanceIdPartitions(
            @NonNull DemandConfigResultRepositoryQueryParam param) {
        Collection<String> demandInstanceIds = param.getDemandInstanceIds();
        if (CollectionUtils.isEmpty(demandInstanceIds)) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> results = new ArrayList<>();
        for (List<String> demandInstanceIdPartition : Lists.partition(
                new ArrayList<>(demandInstanceIds), DemandConfigResultServiceImpl.QUERY_BATCH_SIZE)) {
            DemandConfigResultRepositoryQueryParam partitionParam = this.copyQueryParam(param);
            partitionParam.setDemandInstanceIds(demandInstanceIdPartition);
            results.addAll(this.listWithoutPartition(partitionParam));
        }
        return results;
    }

    /**
     * 按实例类型和实例 ID 分片查询结果。
     *
     * <p>显式传入上千实例ID时，XML会展开为 OR + IN。这里按类型逐段查询，避免单条SQL过大；
     * 空实例ID集合表示“该类型全部结果”，保持原查询语义，不做分片。</p>
     *
     * @param param 查询参数
     * @return 查询结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listByInstanceTypeAndInstanceIdPartitions(
            @NonNull DemandConfigResultRepositoryQueryParam param) {
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = param.getInstanceTypeAndInstanceIds();
        if (instanceTypeAndInstanceIds == null || instanceTypeAndInstanceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> results = new ArrayList<>();
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : instanceTypeAndInstanceIds.entrySet()) {
            DemandConfigResultRepositoryQueryParam partitionParam = this.copyQueryParam(param);
            Set<String> instanceIds = entry.getValue();
            if (!this.shouldPartition(instanceIds)) {
                Map<DemandInstanceTypeEnum, Set<String>> partitionMap = new LinkedHashMap<>();
                partitionMap.put(entry.getKey(), instanceIds);
                partitionParam.setInstanceTypeAndInstanceIds(partitionMap);
                results.addAll(this.listWithoutPartition(partitionParam));
                continue;
            }
            for (List<String> instanceIdPartition : Lists.partition(
                    new ArrayList<>(instanceIds), DemandConfigResultServiceImpl.QUERY_BATCH_SIZE)) {
                Map<DemandInstanceTypeEnum, Set<String>> partitionMap = new LinkedHashMap<>();
                partitionMap.put(entry.getKey(), new LinkedHashSet<>(instanceIdPartition));
                partitionParam = this.copyQueryParam(param);
                partitionParam.setInstanceTypeAndInstanceIds(partitionMap);
                results.addAll(this.listWithoutPartition(partitionParam));
            }
        }
        return results;
    }

    /**
     * 不分片直接查询结果。
     *
     * @param param 查询参数
     * @return 查询结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listWithoutPartition(
            @NonNull DemandConfigResultRepositoryQueryParam param) {
        return super.getBaseMapper().list(ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    /**
     * 拷贝查询参数。
     *
     * @param source 原查询参数
     * @return 新查询参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultRepositoryQueryParam copyQueryParam(
            @NonNull DemandConfigResultRepositoryQueryParam source) {
        DemandConfigResultRepositoryQueryParam target = new DemandConfigResultRepositoryQueryParam();
        target.setDate(source.getDate());
        target.addColumns(source.getSelectColumns());
        target.setId(source.getId());
        target.setIds(source.getIds());
        target.setDemandTemplateType(source.getDemandTemplateType());
        target.setDemandTemplateTypes(source.getDemandTemplateTypes());
        target.setDemandInstanceType(source.getDemandInstanceType());
        target.setDemandInstanceTypes(source.getDemandInstanceTypes());
        target.setDemandInstanceId(source.getDemandInstanceId());
        target.setDemandInstanceIds(source.getDemandInstanceIds());
        target.setInstanceTypeAndInstanceIds(this.copyInstanceTypeAndInstanceIds(source.getInstanceTypeAndInstanceIds()));
        target.setRelTemplateId(source.getRelTemplateId());
        target.setRelTemplateIds(source.getRelTemplateIds());
        target.setRelSceneId(source.getRelSceneId());
        target.setRelSceneIds(source.getRelSceneIds());
        target.setRelSceneIdIsNull(source.getRelSceneIdIsNull());
        target.setIsShielded(source.getIsShielded());
        target.setBusinessKeys(source.getBusinessKeys());
        return target;
    }

    /**
     * 拷贝实例类型和实例ID查询范围。
     *
     * @param source 实例类型和实例ID映射
     * @return 拷贝后的实例类型和实例ID映射
     * @since v7.260701.0
     */
    private @Nullable Map<DemandInstanceTypeEnum, Set<String>> copyInstanceTypeAndInstanceIds(
            @Nullable Map<DemandInstanceTypeEnum, Set<String>> source) {
        if (source == null) {
            return null;
        }
        Map<DemandInstanceTypeEnum, Set<String>> target = new LinkedHashMap<>();
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : source.entrySet()) {
            Set<String> value = entry.getValue();
            if (value != null) {
                target.put(entry.getKey(), new LinkedHashSet<>(value));
                continue;
            }
            target.put(entry.getKey(), value);
        }
        return target;
    }
}
