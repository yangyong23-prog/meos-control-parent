package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.abstracts.AbstractGraphLegendServer;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.LegendNodeDto;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GraphLegendServerCloud extends AbstractGraphLegendServer {

    private final GraphLegendService graphLegendService;
    private final GraphIconService graphIconService;
    @Value("${system.legend.icon.host:${external-url.bdtp.dtp-relational-graph.service:}}")
    private String legendIconHost;

    /**
     * 初始化图图例和图标
     *
     * @param systemGraphData 系统图数据
     */
    @Override
    public void initGraphLegendAndIcons(SystemGraphInstanceVo systemGraphData) {
        Map<String, GraphLegendVo> legendUrlMap = graphLegendService.getGraphLegendMap();
        Map<String, List<GraphIconVo>> iconMap = graphIconService.getGraphIconMap();
        //处理图标信息
        final Map<String, LegendIconVO> legendIconMap = bulidIconMap(systemGraphData);
        List<EquipmentNodeVo> equipmentNodeVos = systemGraphData.getEquipmentNodeVos();
        if (CollectionUtils.isNotEmpty(equipmentNodeVos)) {
            equipmentNodeVos.forEach(nodeVo -> {
                String legendOriId = nodeVo.getLegendOriId();
                if (legendUrlMap.containsKey(legendOriId)) {
                    GraphLegendVo graphLegendVo = legendUrlMap.get(legendOriId);
                    //构建图标信息
                    buildIconUrlVos(graphLegendVo, iconMap);
                    nodeVo.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(nodeVo.getLegendOriId())) {
                    LegendNodeDto legendNode = nodeVo.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(nodeVo.getLegendOriId()));
                    nodeVo.setLegendNode(legendNode);
                }
            });
        }
        List<EquipmentNodeVo> passByVos = systemGraphData.getPassByVos();
        if (CollectionUtils.isNotEmpty(passByVos)) {
            passByVos.forEach(nodeDto -> {
                if (legendUrlMap.containsKey(nodeDto.getLegendOriId())) {
                    GraphLegendVo graphLegendVo = legendUrlMap.get(nodeDto.getLegendOriId());
                    nodeDto.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(nodeDto.getLegendOriId())) {
                    LegendNodeDto legendNode = nodeDto.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(nodeDto.getLegendOriId()));
                    nodeDto.setLegendNode(legendNode);
                }
            });
        }
        List<GraphGroupVo> groupVos = systemGraphData.getGroupVos();
        if (CollectionUtils.isNotEmpty(groupVos)) {
            groupVos.forEach(groupVo -> {
                String legendOriId = groupVo.getLegendOriId();
                if (legendUrlMap.containsKey(legendOriId)) {
                    GraphLegendVo graphLegendVo = legendUrlMap.get(legendOriId);
                    groupVo.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(groupVo.getLegendOriId())) {
                    LegendNodeDto legendNode = groupVo.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(groupVo.getLegendOriId()));
                    groupVo.setLegendNode(legendNode);
                }
            });
        }
    }

    @Override
    public String getHost() {
        return legendIconHost;
    }

    public void buildIconUrlVos(GraphLegendVo vo, Map<String, List<GraphIconVo>> iconMap) {
        String legendId = vo.getLegendId();
        List<GraphIconVo> icons = iconMap.get(legendId);
        if (CollectionUtils.isNotEmpty(icons)) {
            vo.setIconVoList(icons);
        }
    }
}
