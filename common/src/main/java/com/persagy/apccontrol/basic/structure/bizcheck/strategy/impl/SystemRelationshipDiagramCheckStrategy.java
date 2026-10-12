package com.persagy.apccontrol.basic.structure.bizcheck.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.ListSystemGraphDuplicateDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphDuplicateVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.SystemRelationshipDiagramCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.SystemRelationshipDiagramCheckPublishDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.strategy.BizCheckStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;


@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemRelationshipDiagramCheckStrategy implements BizCheckStrategy<SystemRelationshipDiagramCheckItemDTO> {
    private final DtpRwdClientService dtpRwdClientService;
    private final SystemGraphEditorService systemGraphEditorService;
    
    private static final String GRAPH_TYPE_CODE = "源端供水关系图";
    private static final Predicate<SystemGraphDuplicateVO> IS_PUBLISHED_PREDICATE = 
            vo -> Objects.nonNull(vo) && EnumSystemGraphStatus.PUBLISHED.equals(vo.getStatus());
    
    @Override
    public BizCheckStrategyTypeEnum getStrategyType() {
        return BizCheckStrategyTypeEnum.SYSTEM_RELATIONSHIP_DIAGRAM;
    }

    @Override
    public BizCheckItemDTO<SystemRelationshipDiagramCheckItemDTO> execute(BizCheckContext context) throws ExecutionException {
        logProjectInstance().info("开始执行系统关系图业务检查");
        
        BizCheckItemDTO<SystemRelationshipDiagramCheckItemDTO> checkItemDTO = new BizCheckItemDTO<>();
        SystemRelationshipDiagramCheckItemDTO itemDTO = new SystemRelationshipDiagramCheckItemDTO();
        itemDTO.setPublishList(new ArrayList<>());
        checkItemDTO.setCheckResult(itemDTO);

        try {
            List<DtpRwdObjectDTO> systemInstances = getSystemInstances();

            if (CollectionUtil.isEmpty(systemInstances)) {
                logProjectInstance().warn("未获取到有效的系统实例");
                checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
                itemDTO.setStatus(BizCheckStatusEnum.EXCEPTION);
                return checkItemDTO;
            }

            checkItemDTO.setStatus(BizCheckStatusEnum.COMPLETED);
            itemDTO.setStatus(BizCheckStatusEnum.NORMAL);
            
            processSystemInstances(systemInstances, checkItemDTO, itemDTO);
        } catch (Exception e) {
            logProjectInstance().error("执行系统关系图业务检查时发生异常", e);
            checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
            itemDTO.setStatus(BizCheckStatusEnum.EXCEPTION);
            throw new ExecutionException("系统关系图检查执行异常", e);
        }

        return checkItemDTO;
    }

    /**
     * 处理系统实例列表
     */
    private void processSystemInstances(List<DtpRwdObjectDTO> systemInstances, 
                                      BizCheckItemDTO<SystemRelationshipDiagramCheckItemDTO> checkItemDTO,
                                      SystemRelationshipDiagramCheckItemDTO itemDTO) {
        for (DtpRwdObjectDTO systemInstance : systemInstances) {
            try {
                List<SystemGraphDuplicateVO> systemGraphDuplicateVOS = getSystemGraphDuplicates(systemInstance);
                
                boolean hasPublishedGraph = hasPublishedGraph(systemGraphDuplicateVOS);
                
                // 如果结果为空，或者结果里没有已发布的图，则检查结果异常
                if (!hasPublishedGraph) {
                    checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
                    itemDTO.setStatus(BizCheckStatusEnum.EXCEPTION);
                }

                SystemRelationshipDiagramCheckPublishDTO publishDTO = new SystemRelationshipDiagramCheckPublishDTO(
                        systemInstance.getId(),
                        systemInstance.getLocalName(),
                        systemInstance.getClassCode(),
                        hasPublishedGraph
                );
                itemDTO.getPublishList().add(publishDTO);
                
            } catch (Exception e) {
                checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
                itemDTO.setStatus(BizCheckStatusEnum.EXCEPTION);
            }
        }
    }
    
    /**
     * 获取系统图形重复数据
     */
    private List<SystemGraphDuplicateVO> getSystemGraphDuplicates(DtpRwdObjectDTO systemInstance) {
        ListSystemGraphDuplicateDTO dto = new ListSystemGraphDuplicateDTO();
        dto.setSystemObjectId(systemInstance.getId());
        dto.setGraphTypeCode(GRAPH_TYPE_CODE);
        return systemGraphEditorService.listSystemGraphDuplicate(dto);
    }
    
    /**
     * 检查是否存在已发布的图形
     */
    private boolean hasPublishedGraph(List<SystemGraphDuplicateVO> systemGraphDuplicateVOS) {
        return Objects.nonNull(systemGraphDuplicateVOS) && 
               !systemGraphDuplicateVOS.isEmpty() && 
               systemGraphDuplicateVOS.stream().anyMatch(IS_PUBLISHED_PREDICATE);
    }
    
    /**
     * 获取系统实例
     */
    private List<DtpRwdObjectDTO> getSystemInstances() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(Arrays.asList(DtpRwdClassCodeEnum.ACAC_SYSTEM, DtpRwdClassCodeEnum.ACAI_SYSTEM));
        queryParam.addInfoCode(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), Lists.newArrayList("3"));
        queryParam.setWithColumns(Lists.newArrayList(
                DtpRwdObjectDTO.Fields.id, 
                DtpRwdObjectDTO.Fields.classCode, 
                DtpRwdObjectDTO.Fields.localName
        ));
        return dtpRwdClientService.listObject(queryParam);
    }


    @Override
    public String logTag() {
        return "系统关系图业务检查";
    }
}
