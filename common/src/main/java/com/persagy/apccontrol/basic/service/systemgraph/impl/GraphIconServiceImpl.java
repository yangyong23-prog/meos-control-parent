package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.GraphLegendMapper;
import com.persagy.apccontrol.basic.model.dto.DmpQueryStringsDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconDeleteDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconUpdateDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphLegend;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.DiagramIconChangeConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import com.persagy.apccontrol.basic.outside.feign.client.DmpFileServiceClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DmpFileBodyDto;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DtpFileSimpleInfoDto;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.mapper.GraphIconMapper;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.dto.systemgraph.factory.GraphIconFactory.toVo;

/**
 * @author persagy
 * @description 针对表【graph_icon(图标库)】的数据库操作Service实现
 * @createDate 2024-07-15 15:33:07
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GraphIconServiceImpl extends ServiceImpl<GraphIconMapper, GraphIcon>
        implements GraphIconService {

    private final GraphLegendMapper graphLegendMapper;
    private final DmpFileServiceClient dmpFileService;
    private final CommonConfigService commonConfigService;

    /**
     * 查询本地数据库中所有图例id
     *
     * @return {@link List}<{@link GraphIcon}>
     */
    @Override
    public List<GraphIconVo> queryAllGraphIcon() {
        List<GraphIcon> graphIconPos = this.list();
        return graphIconPos
                .stream()
                .map(it -> toVo(it))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<GraphIconVo>> getGraphIconMap() {
        List<GraphIconVo> graphIcons = queryAllGraphIcon();
        return graphIcons.stream().collect(Collectors.groupingBy(GraphIconVo::getLegendId));
    }

    /**
     * 获取配置并入库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGraphIcon() {
        //获取图标配置
        List<DiagramIconChangeConfigVo> diagramIconChangeConfig = commonConfigService.getDiagramIconChangeConfig();
        if (CollectionUtils.isEmpty(diagramIconChangeConfig)) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_951e4a3cb6d84cff8e3e497742b5b38e"));
        }
        Set<String> classCodeSet = diagramIconChangeConfig.stream().map(DiagramIconChangeConfigVo::getClassCode).collect(Collectors.toSet());
        Map<String, List<DiagramIconChangeConfigVo>> configVoMap = diagramIconChangeConfig.stream().collect(Collectors.groupingBy(DiagramIconChangeConfigVo::getClassCode));

        //查询aim本地所有图例数据
        Map<String, List<GraphLegend>> legendMap = graphLegendMapper.selectList(new LambdaQueryWrapper<>())
                .stream()
                .filter(it -> classCodeSet.contains(it.getClassCode()))
                .collect(Collectors.groupingBy(GraphLegend::getClassCode));

        Map<String, GraphIcon> graphIconPoMap = this.list().stream().collect(Collectors.toMap(e -> e.getLegendId() + e.getClassCode() + e.getLegendCode(), Function.identity(), (v1, v2) -> v1));

        List<GraphIcon> graphIcons = new LinkedList<>();
        configVoMap.forEach((k, v) -> {
            List<GraphLegend> legendPoList = legendMap.get(k);
            if (CollectionUtils.isEmpty(legendPoList)) {
                return;
            }
            for (DiagramIconChangeConfigVo vo : v) {
                String legendCode = vo.getLegendCode();
                String classCode = vo.getClassCode();
                for (GraphLegend graphLegendPo : legendPoList) {
                    String legendId = graphLegendPo.getLegendId();
                    String key = legendId + classCode + legendCode;
                    GraphIcon graphIconPo = graphIconPoMap.get(key);
                    GraphIcon graphIcon = new GraphIcon();
                    if (graphIconPo != null && StringUtils.isNotBlank(graphIconPo.getFileUrl())) {
                        graphIcon.setFileUrl(graphIconPo.getFileUrl());
                        graphIcon.setPicUrl(graphIconPo.getPicUrl());
                    } else {
                        graphIcon.setFileUrl(graphLegendPo.getFileUrl());
                        graphIcon.setPicUrl(graphLegendPo.getPicUrl());
                    }
                    graphIcon.setLegendId(legendId);
                    graphIcon.setCondition(JSON.toJSONString(vo.getInfos()));
                    graphIcon.setClassCode(k);
                    graphIcon.setStatus(LegendSourceStatus.DEFAULT);
                    graphIcon.setLegendName(vo.getLegendName());
                    graphIcon.setLegendCode(legendCode);
                    graphIcons.add(graphIcon);

                }
            }

        });
        this.remove(new LambdaQueryWrapper<>());
        this.saveBatch(BeanUtil.copyToList(graphIcons, GraphIcon.class));
    }

    @Override
    public List<GraphIconVo> queryLegendList(String classCode) {
        if (StringUtils.isEmpty(classCode)) {
            return new LinkedList<>();
        }
        List<GraphIcon> graphIconPos = this.lambdaQuery()
                .eq(GraphIcon::getClassCode, classCode)
                .list();
        return graphIconPos
                .stream()
                .map(it -> toVo(it))
                .collect(Collectors.toList());
    }

    @Override
    public void updateLegend(GraphIconUpdateDto graphIconUpdateDto) {
        GraphIcon graphIconPo = this.lambdaQuery()
                .eq(GraphIcon::getClassCode, graphIconUpdateDto.getClassCode())
                .eq(GraphIcon::getLegendName, graphIconUpdateDto.getLegendName())
                .eq(GraphIcon::getLegendId, graphIconUpdateDto.getLegendId())
                .one();
        if (graphIconPo == null) {
            return;
        }

        if (graphIconUpdateDto.getFileUrl() != null) {
            graphIconPo.setFileUrl(graphIconUpdateDto.getFileUrl());
            graphIconPo.setPicUrl(graphIconUpdateDto.getPicUrl());
        }

        graphIconPo.setStatus(LegendSourceStatus.DEFAULT);
        this.updateById(graphIconPo);

    }


    @Override
    public void delete(GraphIconDeleteDto deleteDto) {
        LambdaQueryWrapper<GraphIcon> qw = new LambdaQueryWrapper();
        qw.eq(StringUtils.isBlank(deleteDto.getLegendId()), GraphIcon::getLegendId, deleteDto.getLegendId())
                .eq(StringUtils.isBlank(deleteDto.getClassCode()), GraphIcon::getClassCode, deleteDto.getClassCode())
                .eq(StringUtils.isBlank(deleteDto.getLegendName()), GraphIcon::getLegendName, deleteDto.getLegendName());
        this.remove(qw);
    }

    @Override
    public List<GraphIcon> getLegendByLegendId(String legendId) {
        List<GraphIcon> graphIconPo = this.lambdaQuery()
                .eq(GraphIcon::getLegendId, legendId)
                .list();
        return BeanUtil.copyToList(graphIconPo, GraphIcon.class);
    }

    @Override
    public Boolean initPicUrl() {
        List<GraphIcon> graphIconPos = this.list();
        List<String> fileIds = graphIconPos.stream()
                .map(GraphIcon::getFileUrl).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        DmpFileBodyDto fileQueryDto = new DmpFileBodyDto();
        fileQueryDto.setFileIds(fileIds);
        Map<String, String> fileIdToUrlMap = dmpFileService.querySimpleFiles(new DmpQueryStringsDto(), fileQueryDto).getContent()
                .stream()
                .collect(Collectors.toMap(DtpFileSimpleInfoDto::getId, DtpFileSimpleInfoDto::getPersisFileDownloadUrl,(v1, v2) -> v1));
        List<GraphIcon> updateList = graphIconPos.stream().map(it -> {
            GraphIcon icon = new GraphIcon();
            icon.setId(it.getId());
            String url = fileIdToUrlMap.get(it.getFileUrl());
            if (StringUtils.isNotEmpty(url)) {
                icon.setPicUrl(url);
            } else {
                icon.setPicUrl(it.getFileUrl());
            }

            return icon;
        }).collect(Collectors.toList());
        return updateBatchById(updateList);
    }
}




