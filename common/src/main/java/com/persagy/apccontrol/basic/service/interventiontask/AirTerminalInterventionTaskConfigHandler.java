package com.persagy.apccontrol.basic.service.interventiontask;

import com.persagy.apccontrol.basic.model.dto.intervention.AirTerminalInterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.OutsideAirTerminalInterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogMeosMsgDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogKeyWordEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory.StatusAndControl_SPEVCD_SPACE;

/**
 * 空调末端干预处理类
 */
@Slf4j
@Service("airTerminalInterventionTaskConfigHandler")
public class AirTerminalInterventionTaskConfigHandler extends AbstractInterventionTaskConfigHandler {
    
    @NonNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.AIR_TERMINAL;
    }
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterventionTaskConfigSaveOrUpdateVO saveOrUpdate(InterventionTaskConfigDTO dto) {
        //数据校验
        InterventionTaskConfigSaveOrUpdateVO vo = check(dto, AirTerminalInterventionTaskConfigDataDTO.class);
        if (vo != null) {
            return vo;
        }
        //保存数据
        return super.saveOrUpdate(dto);
    }
    
    /**
     * 查询空间信息
     * @param voList
     * @return
     */
    private static Map<String, DtpRwdSpaceDTO> getStringDtpRwdSpaceDTOMap(List<InterventionTaskConfigVO> voList) {
        Set<String> spaceIds = new HashSet<>();
        voList.forEach(vo -> spaceIds.addAll(vo.getConfigObject().getInstanceIds()));
        //查询末端服务的空间对象 对没有的数据进行过滤
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = RunningMonitorSecondTypeEnum.AIR_TERMINAL;
        spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.name,
                DtpRwdObjectDTO.Fields.localId,
                DtpRwdObjectDTO.Fields.localName);
        spaceQueryParam.setFilterObjectIds(spaceIds);
        List<DtpRwdSpaceDTO> spaces =
                runningMonitorSecondType.toRunningMonitorTypeEnum().getRunningMonitorStrategy().listSpaces(spaceQueryParam);
        
        return DtpRwdObjectDTO.mapIdAndData(spaces);
    }
    
    /**
     * 按照日查询 查询当日全部的数据
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<OutsideAirTerminalInterventionTaskConfigDataDTO> outsideQueryTaskConfigListList(InterventionTaskConfigOutsideQueryParam queryParam) {
        
        List<OutsideAirTerminalInterventionTaskConfigDataDTO> cfgList = new ArrayList<>();
        //设置时间
        LocalDate beginTime = queryParam.getTaskTime().toLocalDate();
        
        //查询配置
        InterventionTaskConfigQueryParam query = new InterventionTaskConfigQueryParam();
        query.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);
        query.setInstanceType(DemandInstanceTypeEnum.SPACE);
        //查询一天的数据
        query.setBeginTime(beginTime.atStartOfDay());
        query.setEndTime(beginTime.atStartOfDay().plusDays(1));
        
        List<InterventionTaskConfigVO> voList = super.queryList(query);
        
        if (CollectionUtils.isEmpty(voList)) {
            return cfgList;
        }
        
        //数据过滤 查询空间信息
        Map<String, DtpRwdSpaceDTO> spaceMap = getStringDtpRwdSpaceDTOMap(voList);
        if (MapUtils.isEmpty(spaceMap)) {
            return cfgList;
        }
        //组装结果
        voList.forEach(vo -> {
            List<AirTerminalInterventionTaskConfigDataDTO> cfgDataList = getAirTerminalInterventionTaskConfigDataDTOS(vo);
            if (CollectionUtils.isEmpty(cfgDataList)) {
                return;
            }
            vo.getConfigObject().getInstanceIds().forEach(spaceId -> {
                if (spaceMap.containsKey(spaceId)) {
                    OutsideAirTerminalInterventionTaskConfigDataDTO cfg = new OutsideAirTerminalInterventionTaskConfigDataDTO();
                    cfg.setConfig(cfgDataList.get(0));
                    cfg.setBeginTime(vo.getBeginTime());
                    cfg.setEndTime(vo.getEndTime());
                    cfg.setSpaceId(spaceId);
                    cfgList.add(cfg);
                }
            });
        });
        
        
        return cfgList;
    }
    
    @Override
    public List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam queryParam) {
        StopWatch stopWatch = new StopWatch("InterventionTaskConfigQuery");
        
        stopWatch.start("查询基础干预任务配置");
        List<InterventionTaskConfigVO> voList = super.queryList(queryParam);
        stopWatch.stop();
        
        if (Collections.isEmpty(voList)) {
            return voList;
        }
        
        stopWatch.start("查询空间信息");
        Map<String, DtpRwdSpaceDTO> spaceMap = getStringDtpRwdSpaceDTOMap(voList);
        stopWatch.stop();
        
        stopWatch.start("处理所有干预配置");
        voList.forEach(vo -> {
            List<DtpRwdObjectDTO> instance = new ArrayList<>();
            vo.getConfigObject().getInstanceIds().forEach(spaceId -> {
                if (spaceMap.containsKey(spaceId)) {
                    instance.add(spaceMap.get(spaceId));
                }
            });
            vo.setInstance(instance);
            
            vo.setConfigData(vo.getConfigData());
        });
        //如果没有实力对象则删除
        voList.removeIf(vo -> CollectionUtils.isEmpty(vo.getInstance()));
        
        stopWatch.stop();
        
        
        if (log.isInfoEnabled()) {
            log.info("干预任务配置查询总耗时:\n{}", stopWatch.prettyPrint());
        }
        return voList;
    }
    
    private List<AirTerminalInterventionTaskConfigDataDTO> getAirTerminalInterventionTaskConfigDataDTOS(InterventionTaskConfigVO vo) {
        return objectMapper.convertValue(
                vo.getConfigData(),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, AirTerminalInterventionTaskConfigDataDTO.class));
    }
    
    //保存日志
    @Override
    public void addLogMsg(InterventionTaskConfig cfg, RunningLogKeyWordEnum logKeyWord) {
        NowRunningLogCategory firstLevelCategory = NowRunningLogCategory.StatusAndControl_SPEVCD;
        String name = "\"" + cfg.getTaskName() + "\"";
        runningLogAddService.saveByMeosMsg(RunningLogMeosMsgDTO.buildManual(cfg.getTaskId().toString(), name,
                logKeyWord.getReasonPhrase(),
                firstLevelCategory, StatusAndControl_SPEVCD_SPACE.getCode(), RunningLogSourceTypeEnum.MEOS_AIR_TERMINAL_INTERVENTION_TASK));
    }
    
    
}
