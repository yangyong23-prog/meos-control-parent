package com.persagy.apccontrol.basic.resource.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.DemandConfigLightingResultMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.param.lighting.CircuitConfigResultsQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitConfigResultVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-配置结果服务实现
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Service
public class DemandConfigLightingResultServiceImpl
        extends BaseServiceImpl<DemandConfigLightingResultMapper, DemandConfigLightingResult>
        implements DemandConfigLightingResultService {
    
    
    @Resource
    private DtpRwdClientService dtpRwdClientService;
    
    @Override
    public DemandConfigLightingResult get(
            @NonNull String projectId,
            @NonNull DemandConfigLightingResultQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().get(param);
    }

    @Override
    public List<DemandConfigLightingResult> list(@NonNull String projectId,
                                                 @NonNull DemandConfigLightingResultQueryDaoParam param) {
        param.setProjectId(ContextUtil.getProjectId());
        return getBaseMapper().list(param);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(@NonNull List<DemandConfigLightingResult> param) {
        //构建 ID
        param.forEach(BaseAssignIdDO::clearSaveParameter);
        //批量保存
        this.getBaseMapper().batchSave(param);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCircuitId(@NonNull Long templateId, @NonNull Long sceneId, @NonNull List<String> circuitId) {
        lambdaUpdate().eq(DemandConfigLightingResult::getProjectId, ContextUtil.getProjectId())
                      .eq(DemandConfigLightingResult::getRelDemandTemplateId, templateId)
                      .eq(DemandConfigLightingResult::getRelDemandLightingSceneId, sceneId)
                      .in(DemandConfigLightingResult::getCircuitId, circuitId)
                      .remove();
    }
    
    @Override
    public void deleteByTemplateAndScene(@NonNull Long templateId, Long sceneId) {
        lambdaUpdate().eq(DemandConfigLightingResult::getProjectId, ContextUtil.getProjectId())
                      .eq(DemandConfigLightingResult::getRelDemandTemplateId, templateId)
                      .eq(sceneId != null, DemandConfigLightingResult::getRelDemandLightingSceneId, sceneId)
                      .remove();
    }
    
    
    @Override
    public int cfgCount(@NonNull String projectId, @NonNull DemandConfigLightingResultQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().cfgCount(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByProjectIdAndTemplateIdAndSceneId(@NotNull String projectId, @NotNull Long templateId, Long sceneId) {
        return getBaseMapper().delete(new LambdaQueryWrapper<DemandConfigLightingResult>()
                .eq(DemandConfigLightingResult::getProjectId, projectId)
                .eq(DemandConfigLightingResult::getRelDemandTemplateId, templateId)
                .eq(null != sceneId, DemandConfigLightingResult::getRelDemandLightingSceneId, sceneId));
    }
    
    @Override
    public List<LightingCircuitConfigResultVO> listCircuitConfigResults(CircuitConfigResultsQueryParam queryParam) {
        List<LightingCircuitConfigResultVO> result = new java.util.ArrayList<>();
        List<DemandConfigLightingResult> r = this.lambdaQuery().eq(DemandConfigLightingResult::getProjectId,
                                                         ContextUtil.getProjectId())
                                                 .eq(DemandConfigLightingResult::getRelDemandTemplateId,
                                                         queryParam.getDemandTemplateId())
                                                 .eq(DemandConfigLightingResult::getRelDemandLightingSceneId,
                                                         queryParam.getSceneId())
                                                 .in(CollectionUtils.isNotEmpty(queryParam.getCircuitIds()),
                                                         DemandConfigLightingResult::getCircuitId,
                                                         queryParam.getCircuitIds())
                                                 .list();
        
        if (CollectionUtils.isEmpty(r)) {
            return result;
        }
        //取出所有对象 ID 查询对象名称
        Map<String, String> idAndName = new HashMap<>();
        Set<String> ids = r.stream().map(DemandConfigLightingResult::getConfigSourceObjId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(ids)) {
            DtpRwdObjectQueryParam copyQueryParam = new DtpRwdObjectQueryParam();
            copyQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName));
            copyQueryParam.setId(ids);
            List<DtpRwdObjectDTO> objs = dtpRwdClientService.listObject(copyQueryParam);
            //创建 对象 DI 和名称的映射
            if (CollectionUtils.isNotEmpty(objs)) {
                idAndName = DtpRwdObjectDTO.mapIdAndLocalName(objs);
            }
        }
        for (DemandConfigLightingResult item : r) {
            LightingCircuitConfigResultVO vo = new LightingCircuitConfigResultVO();
            vo.setCircuitId(item.getCircuitId());
            vo.setConfigSourceObjId(item.getConfigSourceObjId());
            vo.setConfigSourceObjName(idAndName.get(item.getConfigSourceObjId()));
            vo.setSettingMode(item.getConfigData().getCircuitSettingMode());
            vo.setConfigSourceType(item.getConfigSourceType());
            result.add(vo);
        }
        return result;
    }
}
