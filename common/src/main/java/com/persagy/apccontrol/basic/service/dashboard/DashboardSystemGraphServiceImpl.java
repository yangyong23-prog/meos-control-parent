package com.persagy.apccontrol.basic.service.dashboard;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.param.dashboard.QuerySystemCardParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.SystemCardVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.MenuConfigVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphDuplicateService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardSystemGraphServiceImpl implements DashboardSystemGraphService {

    private final CommonConfigService commonConfigService;
    private final DtpRwdClientService dtpRwdClientService;
    private final SystemGraphService systemGraphService;
    private final SystemGraphDuplicateService systemGraphDuplicateService;


    /**
     * 列表系统通过第一位菜单代码
     *
     * @param menuCode 菜单代码
     * @param param
     * @return {@code List<SystemVO> }
     * @since 7.250501.0
     */
    @Override
    public List<SystemCardVO> listSystemByFirstMenuCode(String menuCode, QuerySystemCardParam param) {
        Set<String> classCodeSet = new HashSet<>();
        List<SystemGraph> systemGraphs = querySystemGraphList(menuCode, classCodeSet);
        if (CollectionUtils.isEmpty(systemGraphs)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam rwdObjectQueryParam = new DtpRwdObjectQueryParam().setClassCode(classCodeSet)
                .addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
        if (param.isFilterMonitorStatus()) {
            rwdObjectQueryParam.filterMonitoringSurveillanceStatus();
        }
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(rwdObjectQueryParam);
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            return Collections.emptyList();
        }
        Map<String, DtpRwdObjectDTO> objectIdObjectMap = dtpRwdObjectDTOS.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (v1, v2) -> v1));
        Set<String> objectIdSet = dtpRwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        //去除无效的系统图
        systemGraphs.removeIf(e -> !objectIdSet.contains(e.getSystemObjectId()) || Objects.isNull(e.getSystemGraphDuplicateId()));
        if (CollectionUtils.isEmpty(systemGraphs)) {
            return Collections.emptyList();
        }
        Map<Long, Long> systemGraphIdDuplicateIdMap = systemGraphs.stream().collect(Collectors.toMap(SystemGraph::getId, SystemGraph::getSystemGraphDuplicateId, (v1, v2) -> v1));
        Map<Long, SystemGraphInstanceVo> dupIdSystemGraphInstanceVoMap = systemGraphDuplicateService.mapSystemGraphDataByDuplicateIds(systemGraphIdDuplicateIdMap.values());
        if (MapUtils.isEmpty(dupIdSystemGraphInstanceVoMap)) {
            return Collections.emptyList();
        }
        List<SystemCardVO> systemVOList = new ArrayList<>();
        //组装结果
        Map<String, SystemGraph> objectIdSystemGraphIdMap = systemGraphs.stream().collect(Collectors.toMap(SystemGraph::getSystemObjectId, Function.identity(), (v1, v2) -> v1));
        for (Map.Entry<String, DtpRwdObjectDTO> entry : objectIdObjectMap.entrySet()) {
            DtpRwdObjectDTO dtpRwdObjectDTO = entry.getValue();
            String objectId = entry.getKey();
            SystemCardVO cardVO = new SystemCardVO();
            BeanUtils.copyProperties(dtpRwdObjectDTO, cardVO);
            SystemGraph systemGraph = objectIdSystemGraphIdMap.get(objectId);
            cardVO.setIsExistsSystemGraph(false);
            if (Objects.nonNull(systemGraph)) {
                SystemGraphInstanceVo instanceVo = dupIdSystemGraphInstanceVoMap.get(systemGraphIdDuplicateIdMap.get(systemGraph.getId()));
                if (Objects.nonNull(instanceVo)) {
                    cardVO.setIsExistsSystemGraph(true);
                    cardVO.setGraphTypeCode(systemGraph.getGraphTypeCode());
                    cardVO.setGraphObjectIds(systemGraphService.getAllEquipmentInSystemGraph(instanceVo));
                }
            }
            //当时查询所有实例的时候，不管系统图是否存在都返回
            if (param.isAllInstance()) {
                systemVOList.add(cardVO);
            } else if (cardVO.getIsExistsSystemGraph()) {
                //否则只返回有系统图的对象
                systemVOList.add(cardVO);
            }
        }
        return systemVOList;
    }

    /**
     * 查询系统图列表
     *
     * @param menuCode 菜单编码
     * @return {@code List<SystemGraph> }
     * @since 7.250501.0
     */
    private List<SystemGraph> querySystemGraphList(String menuCode, Set<String> classCodeSet) {
        List<MenuConfigVo> menuConfig = commonConfigService.getMenuConfig();
        if (CollectionUtils.isEmpty(menuConfig)) {
            return Collections.emptyList();
        }
        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        if (CollectionUtils.isEmpty(graphConfig)) {
            return Collections.emptyList();
        }
        Map<String, List<MenuConfigVo>> menuCodeMenuConfigListMap = menuConfig.stream().collect(Collectors.groupingBy(MenuConfigVo::getMenuCode));
        List<MenuConfigVo> menuConfigVos = menuCodeMenuConfigListMap.get(menuCode);
        if (CollectionUtils.isEmpty(menuConfigVos)) {
            return Collections.emptyList();
        }
        //获取到图名列表
        Set<String> groupNames = menuConfigVos.stream().map(MenuConfigVo::getImageOrProductName).collect(Collectors.toSet());
        //如果是配电，需要去掉变压器监测图，然后添加低压变配电关系图
        if (menuCode.equals("SE")) {
            groupNames.add("低压变配电关系图");
        }
        Set<String> graphCodeTypes = new HashSet<>();
        Map<String, GraphConfigVo> graphNameGraphConfigMap = graphConfig.stream().collect(Collectors.toMap(GraphConfigVo::getMapTitle, Function.identity()));
        groupNames.forEach(graphName -> {
            GraphConfigVo graphConfigVo = graphNameGraphConfigMap.get(graphName);
            if (Objects.isNull(graphConfigVo)) {
                return;
            }
            classCodeSet.add(graphConfigVo.getDiagramBaseObjectCode());
            graphCodeTypes.add(graphConfigVo.getDiagramTypeOfTheDiagram());
        });
        if (CollectionUtils.isEmpty(classCodeSet) || CollectionUtils.isEmpty(graphCodeTypes)) {
            return Collections.emptyList();
        }
        return systemGraphService.listSystemGraph(null, classCodeSet, graphCodeTypes);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "系统图dashboard-Service";
    }
}
