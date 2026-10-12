package com.persagy.apccontrol.task;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeNowService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 照明需求计算
 */
@Component("demandConfigLightingComputeTaskHandler")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigLightingComputeTaskHandler implements TecXxJobHandler {
    
    private final DemandConfigLightingComputeNowService demandConfigLightingComputeNowService;
    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandSceneGateway demandSceneGateway;
    
    @Override
    public String getHandlerName() {
        return LocaleMessageUtil.getMessage("key_06abaee4de914771b23bc2cd2fba993b");
    }
    
    @Override
    public Future<String> processAsync(String groupCode, ProjectDTO projectDTO) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectDTO.getProjectId());
        
        log.info("开始执行:{}-{}-{}", this.getHandlerName(), ContextUtil.getGroupCode(), ContextUtil.getProjectId());
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> extracted(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> extracted(DemandTemplateTypeEnum.PARKING_LOT_LIGHTING),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> extracted(DemandTemplateTypeEnum.BUILDING_LIGHTING),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> extracted(DemandTemplateTypeEnum.LANDSCAPE_LIGHTING),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> extracted(DemandTemplateTypeEnum.SIGN_LIGHTING),
                BusinessThreadPoolExecutor.ttlExecutor()));
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return new AsyncResult<>(this.getHandlerName() + LocaleMessageUtil.getMessage("key_2fa808b2f7ea4112a5ed1750c743f957") + groupCode + ";projectId" + ContextUtil.getProjectId());
    }
    
    private void extracted(DemandTemplateTypeEnum type) {
        List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(type);
        //查询照明场景
        if (CollectionUtils.isNotEmpty(templateIds)) {
            List<DemandSceneEntity> scenes = demandSceneGateway.listScenesByTemplateId(new HashSet<>(templateIds));
            if (CollectionUtils.isNotEmpty(scenes)) {
                scenes.forEach(scene -> {
                    DemandConfigLightingComputeParam demandConfigLightingComputeParam =
                            new DemandConfigLightingComputeParam();
                    demandConfigLightingComputeParam.setTemplateId(scene.getRelTemplateId());
                    demandConfigLightingComputeParam.setSceneId(scene.getId());
                    demandConfigLightingComputeNowService.resultCompute(demandConfigLightingComputeParam);
                });
            }
        }
    }
    
    @Override
    public void process(String groupCode, ProjectDTO projectDTO) {
    }
    
}