package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.abstracts.AbstractGraphLegendServer;
import com.persagy.apccontrol.basic.convert.GraphIconConvert;
import com.persagy.apccontrol.basic.convert.GraphLegendConvert;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.LegendNodeDto;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GraphLegendServerEdge extends AbstractGraphLegendServer {

    private static final Logger log = LoggerFactory.getLogger(GraphLegendServerEdge.class);
    private final GraphLegendService graphLegendService;
    private final GraphIconService graphIconService;
    private final GraphLegendConvert graphLegendConvert;
    private final GraphIconConvert graphIconConvert;
    @Value("${system.legend.icon.host:}")
    private String legendIconHost;

    @Value("${edge.graph.pic.host:}")
    private String legendHost;

    @Override
    public void initGraphLegendAndIcons(SystemGraphInstanceVo systemGraphData) {
        Map<String, GraphLegendVo> legendUrlMap = graphLegendService.getGraphLegendMap();
        Map<String, List<GraphIconVo>> iconMap = graphIconService.getGraphIconMap();
        Map<String, LegendIconVO> legendIconMap = bulidIconMap(systemGraphData);
        List<EquipmentNodeVo> equipmentNodeVos = systemGraphData.getEquipmentNodeVos();
        if (CollectionUtils.isNotEmpty(equipmentNodeVos)) {
            equipmentNodeVos.forEach(nodeVo -> {
                String legendOriId = nodeVo.getLegendOriId();
                if (legendUrlMap.containsKey(legendOriId)) {
                    GraphLegendVo graphLegendVo = graphLegendConvert.clone(legendUrlMap.get(legendOriId));
                    //构建图标信息
                    buildIconUrlVos(graphLegendVo, iconMap);
                    String fileUrl = graphLegendVo.getFileUrl();
                    String newUrl = replaceFileUrl(fileUrl);
                    if (StringUtils.isNotEmpty(newUrl)) {
                        graphLegendVo.setFileUrl(newUrl);
                    }
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
                    GraphLegendVo graphLegendVo = graphLegendConvert.clone(legendUrlMap.get(nodeDto.getLegendOriId()));
                    String fileUrl = graphLegendVo.getFileUrl();
                    String newUrl = replaceFileUrl(fileUrl);
                    if (StringUtils.isNotEmpty(newUrl)) {
                        graphLegendVo.setFileUrl(newUrl);
                    }
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
                    GraphLegendVo graphLegendVo = graphLegendConvert.clone(legendUrlMap.get(legendOriId));
                    String fileUrl = graphLegendVo.getFileUrl();
                    String newUrl = replaceFileUrl(fileUrl);
                    if (StringUtils.isNotEmpty(newUrl)) {
                        graphLegendVo.setFileUrl(newUrl);
                    }
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
        List<GraphIconVo> icons = graphIconConvert.listClone(iconMap.get(legendId));
        if (CollectionUtils.isNotEmpty(icons)) {
            icons.forEach(iconVo -> {
                if (iconVo.getFileUrl() != null) {
                    String fileUrl = replaceFileUrl(iconVo.getFileUrl());
                    if (StringUtils.isNotEmpty(fileUrl)) {
                        iconVo.setFileUrl(fileUrl);
                    }
                }
            });
            vo.setIconVoList(icons);
        }
    }

    /**
     * 替换文件url
     *
     * @param fileUrl 文件url
     * @return {@link String }
     */
    private String replaceFileUrl(String fileUrl) {
        if (StringUtils.isNotEmpty(fileUrl) && fileUrl.startsWith("https://")) {
            //替换fileURL的域名
            String replace = fileUrl.replace("https://", "");
            String path = replace.substring(replace.indexOf("/"));
            if (StringUtils.isBlank(legendHost)) {
                log.error("====>legendHost is Blank,请在nacos中配置变量edge.graph.pic.host");
            } else {
                //判断host后面有没有/，有就去掉
                if (legendHost.endsWith("/")) {
                    legendHost = legendHost.substring(0, legendHost.length() - 1);
                }
                return legendHost + path;
            }
        }
        return null;
    }
}
