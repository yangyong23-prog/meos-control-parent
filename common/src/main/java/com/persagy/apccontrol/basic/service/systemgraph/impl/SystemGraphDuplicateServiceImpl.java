package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.SystemGraphDuplicateMapper;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphData;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumGraphType;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphDuplicateVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import com.persagy.apccontrol.basic.service.systemgraph.*;
import com.persagy.apccontrol.basic.utils.CompressUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author persagy
 * @description 针对表【system_graph_duplicate(系统图副本表)】的数据库操作Service实现
 * @createDate 2024-06-25 11:55:16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphDuplicateServiceImpl extends ServiceImpl<SystemGraphDuplicateMapper, SystemGraphDuplicate>
        implements SystemGraphDuplicateService {

    private final SystemGraphDataService systemGraphDataService;
    private final SystemGraphService systemGraphService;
    private final SystemGraphDetailService systemGraphDetailService;
    private final SystemGraphTemplateStatusService systemGraphTemplateStatusService;

    /**
     * 系统图副本列表
     *
     * @param systemObjectId 系统对象id
     * @param graphTypeCode  模板id
     * @param status         发布状态
     * @return {@link List }<{@link SystemGraphDuplicate }>
     */
    @Override
    public List<SystemGraphDuplicate> listSystemGraphDuplicate(String systemObjectId, String graphTypeCode, EnumSystemGraphStatus status) {
        LambdaQueryWrapper<SystemGraphDuplicate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraphDuplicate::getSystemObjectId, systemObjectId)
                .eq(SystemGraphDuplicate::getGraphTypeCode, graphTypeCode)
                .eq(Objects.nonNull(status), SystemGraphDuplicate::getStatus, status);
        List<SystemGraphDuplicate> list = list(queryWrapper);
        list.sort(Comparator.comparing(SystemGraphDuplicate::getOrder));
        return list;

    }

    /**
     * 保存重复项
     *
     * @param systemObjectId    系统对象id
     * @param graphType         图形类型
     * @param classCode         类代码
     * @param graphTypeCode     图形类型代码
     * @param systemGraphDataId 系统图数据id
     * @param isNotEdit         地位
     * @param generateId
     * @return
     */
    @Override
    public SystemGraphDuplicate saveDuplicate(String systemObjectId, EnumGraphType graphType,
                                              String classCode, String graphTypeCode, Long systemGraphDataId,
                                              Boolean isNotEdit, String generateId) {
        SystemGraphDuplicate duplicate = new SystemGraphDuplicate();
        duplicate.setSystemObjectId(systemObjectId);
        duplicate.setGraphType(graphType);
        duplicate.setName(graphType.getReasonPhrase());
        duplicate.setStatus(EnumSystemGraphStatus.UN_PUBLISHED);
        duplicate.setClassCode(classCode);
        duplicate.setIsNotEdit(isNotEdit);
        duplicate.setGraphTypeCode(graphTypeCode);
        duplicate.setProjectId(ContextUtil.getProjectId());
        duplicate.setSystemGraphDataId(systemGraphDataId);
        duplicate.setGenerateId(generateId);
        duplicate.setOrder(EnumGraphType.ORIGINAL == graphType ? 0 : 1);
        LambdaQueryWrapper<SystemGraphDuplicate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraphDuplicate::getSystemObjectId, systemObjectId).eq(SystemGraphDuplicate::getGraphTypeCode, graphTypeCode)
                .eq(SystemGraphDuplicate::getGraphType, graphType);
        List<SystemGraphDuplicate> list = list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            if (graphType == EnumGraphType.ORIGINAL) {
                //如果是原图，需要删了再存
                List<Long> dupIds = list.stream().map(SystemGraphDuplicate::getId).collect(Collectors.toList());
                removeByIds(dupIds);
                List<Long> dataIds = list.stream().map(SystemGraphDuplicate::getSystemGraphDataId).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(dataIds)) {
                    systemGraphDataService.removeByIds(dataIds);
                }
            } else {
                //如果不是原图，order+1
                Integer order = list.stream().map(SystemGraphDuplicate::getOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
                duplicate.setOrder(order);
            }
        }
        save(duplicate);
        return duplicate;

    }

    @Override
    public SystemGraphDuplicateVO createSystemGraphDuplicate(Long duplicateId) {
        SystemGraphDuplicate duplicate = getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_143c5e15c81d4d1e86b146cb4ef5c2fc"), LocaleMessageUtil.getMessage("key_143c5e15c81d4d1e86b146cb4ef5c2fc"));
        }
        //原图且不能编辑的时候，不能创建副本
        if (duplicate.getGraphType() == EnumGraphType.ORIGINAL && duplicate.getIsNotEdit()) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_e79268dc54344fc385fc28af929e0ef7"), LocaleMessageUtil.getMessage("key_e79268dc54344fc385fc28af929e0ef7"));
        }
        LambdaQueryWrapper<SystemGraphDuplicate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraphDuplicate::getSystemObjectId, duplicate.getSystemObjectId())
                .eq(SystemGraphDuplicate::getGraphTypeCode, duplicate.getGraphTypeCode())
                .eq(SystemGraphDuplicate::getGraphType, EnumGraphType.COPY);
        if (list(queryWrapper).size() > 5) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_554d6667e09d493c922a9bdc287f14cc"), LocaleMessageUtil.getMessage("key_554d6667e09d493c922a9bdc287f14cc"));
        }
        //创建新的数据
        Long systemGraphDataId = duplicate.getSystemGraphDataId();
        Long newSystemGraphDataId = null;
        if (systemGraphDataId != null) {
            SystemGraphData systemGraphData = systemGraphDataService.getById(systemGraphDataId);
            systemGraphData.setId(null);
            systemGraphDataService.save(systemGraphData);
            newSystemGraphDataId = systemGraphData.getId();
        }

        SystemGraphDuplicate newDuplicate = saveDuplicate(duplicate.getSystemObjectId(), EnumGraphType.COPY,
                duplicate.getClassCode(), duplicate.getGraphTypeCode(), newSystemGraphDataId,
                false, duplicate.getGenerateId());
        //创建新的细节数据
        List<SystemGraphDetail> systemGraphDetails = systemGraphDetailService.listSystemGraphDetailByDuplicateId(duplicateId);
        List<SystemGraphDetail> batchList = new ArrayList<>();
        systemGraphDetails.forEach(detail -> {
            detail.setId(null);
            detail.setSystemGraphDuplicateId(newDuplicate.getId());
            batchList.add(detail);
        });
        if (CollectionUtils.isNotEmpty(batchList)) {
            systemGraphDetailService.saveBatch(batchList);
        }

        return SystemGraphDuplicateVO.toVO(newDuplicate);
    }

    @Override
    public Boolean publishSystemGraph(Long duplicateId) {
        SystemGraphDuplicate duplicate = getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"));
        }
        if (duplicate.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_8d58a03dca3544d48eff886645c7bdb9"), LocaleMessageUtil.getMessage("key_8d58a03dca3544d48eff886645c7bdb9"));
        }
        LambdaQueryWrapper<SystemGraphDuplicate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraphDuplicate::getSystemObjectId, duplicate.getSystemObjectId())
                .eq(SystemGraphDuplicate::getGraphTypeCode, duplicate.getGraphTypeCode())
                .eq(SystemGraphDuplicate::getClassCode, duplicate.getClassCode());
        List<SystemGraphDuplicate> list = list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (SystemGraphDuplicate data : list) {
                if (data.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
                    SystemGraphDuplicate newData = new SystemGraphDuplicate();
                    newData.setId(data.getId());
                    newData.setStatus(EnumSystemGraphStatus.UN_PUBLISHED);
                    updateById(newData);
                    systemGraphDataService.updateSystemGraphDataStatusById(data.getSystemGraphDataId(), EnumSystemGraphStatus.UN_PUBLISHED);
                }
            }
        }

        SystemGraphDuplicate newDuplicate = new SystemGraphDuplicate();
        newDuplicate.setId(duplicateId);
        newDuplicate.setStatus(EnumSystemGraphStatus.PUBLISHED);
        Boolean bool = updateById(newDuplicate);
        if (bool) {
            SystemGraph graph = systemGraphService.getSystemGraph(duplicate.getSystemObjectId(), duplicate.getClassCode(), duplicate.getGraphTypeCode());
            String graphName = systemGraphTemplateStatusService.getTemplateName(duplicate.getGraphTypeCode(), duplicate.getSystemObjectId());
            //如果为空，需要新增系统图
            if (graph == null) {
                systemGraphService.saveSystemGraph(graphName, duplicate.getGraphTypeCode(), duplicate.getClassCode(),
                        duplicate.getSystemObjectId(), duplicate.getId());
            } else {
                systemGraphService.updateSystemGraphDuplicateIdById(graph.getId(), duplicateId, graphName);
            }
            //更新系统图数据状态
            systemGraphDataService.updateSystemGraphDataStatusById(duplicate.getSystemGraphDataId(), EnumSystemGraphStatus.PUBLISHED);
        }
        return bool;

    }

    @Override
    public Boolean updateDuplicateDataById(Long duplicateId, Long systemGraphDataId) {
        SystemGraphDuplicate duplicate = this.getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"));
        }
        Long oldSystemGraphDataId = duplicate.getSystemGraphDataId();
        //删除老数据
        if (oldSystemGraphDataId != null) {
            systemGraphDataService.removeById(oldSystemGraphDataId);
        }
//        //删除细节数据
//        List<SystemGraphDetail> systemGraphDetails = systemGraphDetailService.listSystemGraphDetailByDuplicateId(duplicateId);
//        if (CollectionUtils.isNotEmpty(systemGraphDetails)) {
//            List<Long> detailIds = systemGraphDetails.stream().map(SystemGraphDetail::getId).collect(Collectors.toList());
//            systemGraphDetailService.removeByIds(detailIds);
//        }
        duplicate = new SystemGraphDuplicate();
        duplicate.setId(duplicateId);
        duplicate.setSystemGraphDataId(systemGraphDataId);
        return updateById(duplicate);
    }

    @Override
    public Boolean editDuplicateNameById(Long duplicateId, String duplicateName) {
        SystemGraphDuplicate duplicate = this.getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"), LocaleMessageUtil.getMessage("key_37fa06d362804a08be8fa8461f2fc024"));
        }
        if (duplicate.getGraphType() == EnumGraphType.ORIGINAL) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_74babf4aff19472ea731e7e2b9283a67"), LocaleMessageUtil.getMessage("key_74babf4aff19472ea731e7e2b9283a67"));
        }
        if (StringUtils.isBlank(duplicateName)) {
            duplicateName = EnumGraphType.COPY.getReasonPhrase();
        }
        duplicate.setName(duplicateName);
        return updateById(duplicate);
    }

    /**
     * 根据副本id获取系统图数据
     *
     * @param duplicateIds 重复ID
     * @return {@code List<SystemGraphData> }
     * @since 7.250501.0
     */
    @Override
    public Map<Long, SystemGraphInstanceVo> mapSystemGraphDataByDuplicateIds(@NotNull Collection<Long> duplicateIds) {
        if (CollectionUtils.isEmpty(duplicateIds)) {
            return Collections.emptyMap();
        }
        List<SystemGraphDuplicate> systemGraphDuplicates = listByIds(duplicateIds);
        if (CollectionUtils.isEmpty(systemGraphDuplicates)) {
            return Collections.emptyMap();
        }
        List<Long> systemGraphDataIdList = systemGraphDuplicates.stream().map(SystemGraphDuplicate::getSystemGraphDataId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, Long> graphDataIdDuplicateIdMap = systemGraphDuplicates.stream().collect(Collectors.toMap(SystemGraphDuplicate::getSystemGraphDataId, SystemGraphDuplicate::getId, (k1, k2) -> k1));
        Map<Long, SystemGraphInstanceVo> systemGraphInstanceVoMap = new HashMap<>();
        systemGraphDataService.listByIds(systemGraphDataIdList).stream()
                .filter(e -> StringUtils.isNotBlank(e.getData()))
                .forEach(systemGraphData -> {
                    SystemGraphInstanceVo systemGraphInstanceVo = JSON.parseObject(CompressUtil.decompress(systemGraphData.getData()), SystemGraphInstanceVo.class);
                    Long dupId = graphDataIdDuplicateIdMap.get(systemGraphData.getId());
                    systemGraphInstanceVoMap.put(dupId, systemGraphInstanceVo);
                });
        return systemGraphInstanceVoMap;

    }
}




