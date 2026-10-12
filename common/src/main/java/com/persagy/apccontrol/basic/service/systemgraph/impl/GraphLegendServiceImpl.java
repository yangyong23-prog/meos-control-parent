package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.GraphLegendMapper;
import com.persagy.apccontrol.basic.model.dto.DmpQueryStringsDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconUpdateDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphLegendUpdateDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendAnchor;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.AttachTypeNodeDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphLegendDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphLegendSelectDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphTypeTreeDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.factory.GraphIconFactory;
import com.persagy.apccontrol.basic.model.dto.systemgraph.factory.GraphLegendFactory;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphLegend;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendGuestVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphTypeTreeVo;
import com.persagy.apccontrol.basic.outside.feign.client.DmpFileServiceClient;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRelationalGraphClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DmpFileBodyDto;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DtpFileSimpleInfoDto;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.dto.systemgraph.factory.GraphLegendFactory.toEntity;
import static com.persagy.apccontrol.basic.model.dto.systemgraph.factory.GraphLegendFactory.toGuestVo;

/**
 * @author persagy
 * @description 针对表【graph_legend】的数据库操作Service实现
 * @createDate 2024-07-15 15:33:14
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GraphLegendServiceImpl extends BaseServiceImpl<GraphLegendMapper, GraphLegend>
        implements GraphLegendService {

    private final DtpRelationalGraphClient admRelGraphClient;
    private final DmpFileServiceClient dmpFileServiceClient;
    private final GraphIconService graphIconService;

    /**
     * 查询本地数据库中所有图例id
     *
     * @return
     */
    @Override
    public List<GraphLegendVo> queryAllGraphLegend() {
        List<GraphLegend> graphLegendPos = this.list();
        return graphLegendPos.stream().map(GraphLegendFactory::toVo).collect(Collectors.toList());
    }

    /**
     * 获取图形图例图 key->graphLegend
     *
     * @return {@link Map }<{@link String }, {@link GraphLegend }>
     */
    @Override
    public Map<String, GraphLegendVo> getGraphLegendMap() {
        List<GraphLegendVo> graphLegends = queryAllGraphLegend();
        return graphLegends.stream()
                .collect(Collectors.toMap(GraphLegendVo::getLegendId, it -> it, (v1, v2) -> v1));
    }

    /**
     * 查询并更新图例及状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGraphLegend() {
        CustomerResponse<List<GraphLegendDto>> listCustomerResponse = admRelGraphClient.graphLegendSelect(new GraphLegendSelectDto());
        List<GraphLegendDto> graphLegendDtos = CustomerResponse.handleResult(listCustomerResponse);
        //查询bdtp所有图例数据
        Map<String, GraphLegendDto> legendIdToDtoMap = graphLegendDtos
                .stream()
                .filter(it -> it.getClassCode() != null)
                .collect(Collectors.toMap(GraphLegendDto::getOriId, it -> it));

        //查询aim本地所有图例数据
        Map<String, GraphLegend> legendIdToEntityMap = this.list()
                .stream()
                .collect(Collectors.toMap(GraphLegend::getLegendId, it -> BeanUtil.copyProperties(it, GraphLegend.class), (v1, v2) -> v1));
        //查询所有小附件对象类
        List<String> attachTypeClassCodes = new LinkedList<>();
        CustomerResponse.handleResult(admRelGraphClient.attachTypeList()).forEach(it -> getAttachTypeList(attachTypeClassCodes, it));

        Set<String> legendIds = new HashSet<>(legendIdToDtoMap.keySet());
        legendIds.addAll(legendIdToEntityMap.keySet());

        List<GraphLegend> graphLegends = new LinkedList<>();
        legendIds.forEach((graphLegendId ->
        {
            GraphLegend graphLegend = toEntity(legendIdToDtoMap.getOrDefault(graphLegendId, null), legendIdToEntityMap.getOrDefault(graphLegendId, null), attachTypeClassCodes);
            if (graphLegend != null && !LegendSourceStatus.DELETED.equals(graphLegend.getStatus())) {
                graphLegends.add(graphLegend);
            }
        }));

        List<GraphLegend> entityList = BeanUtil.copyToList(graphLegends, GraphLegend.class);
        this.lambdaUpdate().remove();
        this.saveBatch(entityList);
        //更新图标
//        graphIconService.syncGraphIcon();
    }

    public void getAttachTypeList(List<String> classCodes, AttachTypeNodeDto attachTypeNodeDto) {
        classCodes.add(attachTypeNodeDto.getClassCode());

        if (CollectionUtils.isNotEmpty(attachTypeNodeDto.getChildren())) {
            attachTypeNodeDto.getChildren().forEach(it -> getAttachTypeList(classCodes, it));
        }
    }

    @Override
    public List<GraphLegendVo> queryLegendList(String classCode) {
        if (StringUtils.isEmpty(classCode)) {
            return new LinkedList<>();
        }
        List<GraphLegend> graphLegendPos = this.lambdaQuery()
                .eq(GraphLegend::getClassCode, classCode)
                .list();
        List<GraphLegendVo> legendVos = graphLegendPos
                .stream()
                .map(GraphLegendFactory::toVo)
                .collect(Collectors.toList());
        legendVos.forEach(vo -> {
            List<GraphIcon> iconList = graphIconService.getLegendByLegendId(vo.getLegendId());
            if (CollectionUtils.isEmpty(iconList)) {
                GraphIcon graphIcon = new GraphIcon();
                graphIcon.setLegendName(LocaleMessageUtil.getMessage("key_1f44623b840d44b199b70a6239a60063"));
                graphIcon.setLegendId(vo.getLegendId());
                graphIcon.setFileUrl(vo.getFileUrl());
                graphIcon.setLegendCode(vo.getLegendCode());
                iconList.add(graphIcon);
            }
            List<GraphIconVo> graphIconVolist = iconList.stream().map(GraphIconFactory::toVo).collect(Collectors.toList());
            vo.setIconVoList(graphIconVolist);
        });
        return legendVos;
    }

    @Override
    public void updateLegend(String legendId, GraphLegendUpdateDto graphLegendUpdateDto) {
        GraphLegend graphLegendPo = this.lambdaQuery()
                .eq(GraphLegend::getLegendId, legendId)
                .one();
        if (graphLegendPo == null) {
            return;
        }

        if (StringUtils.isNotEmpty(graphLegendUpdateDto.getFileUrl())) {
            graphLegendPo.setFileUrl(graphLegendUpdateDto.getFileUrl());
            graphLegendPo.setPicUrl(queryPicUrl(graphLegendUpdateDto.getFileUrl()));
        }
        if (CollectionUtils.isNotEmpty(graphLegendUpdateDto.getAnchors())) {
            graphLegendPo.setAnchors(BeanUtil.copyToList(graphLegendUpdateDto.getAnchors(), LegendAnchor.class));
        }

        graphLegendPo.setStatus(LegendSourceStatus.DEFAULT);
        this.updateByIdIgnore(graphLegendPo);

        List<GraphIconVo> iconVoList = graphLegendUpdateDto.getIconVoList();
        if (CollectionUtils.isNotEmpty(iconVoList) && iconVoList.size() > 1) {
            for (GraphIconVo graphIconVo : iconVoList) {
                GraphIconUpdateDto dto = new GraphIconUpdateDto();
                dto.setFileUrl(graphIconVo.getFileUrl());
                dto.setPicUrl(queryPicUrl(graphIconVo.getFileUrl()));
                dto.setLegendName(graphIconVo.getLegendName());
                dto.setClassCode(graphIconVo.getClassCode());
                dto.setLegendId(graphIconVo.getLegendId());
                graphIconService.updateLegend(dto);
            }
        }
    }

    private String queryPicUrl(String fileUrl) {
        if (fileUrl.contains("http")) {
            return fileUrl;
        }
        CustomerResponse<List<DtpFileSimpleInfoDto>> response = dmpFileServiceClient.querySimpleFiles(new DmpQueryStringsDto(), new DmpFileBodyDto().setFileIds(fileUrl));
        List<DtpFileSimpleInfoDto> dtos = CustomerResponse.handleResult(response);
        if (CollectionUtils.isNotEmpty(dtos)) {
            return dtos.get(0).getPersisFileDownloadUrl();
        }
        return null;
    }

    @Override
    public List<GraphTypeTreeVo> typeTree() {
        List<GraphTypeTreeDto> graphTypeTreeDtos = CustomerResponse.handleResult(admRelGraphClient.graphCommonTree());

        //查询所有需要修改的对象类
        List<String> updateClassCodes = this.lambdaQuery()
                .isNull(GraphLegend::getFileUrl)
                .or()
                .in(GraphLegend::getStatus, Arrays.asList(LegendSourceStatus.UPDATED, LegendSourceStatus.DELETED))
                .list().stream().map(GraphLegend::getClassCode).distinct().collect(Collectors.toList());

        List<GraphTypeTreeVo> results = new LinkedList<>();
        graphTypeTreeDtos.forEach(dto -> {
            results.add(traversalTypeTree(dto, updateClassCodes));
        });

        return results;

    }

    public GraphTypeTreeVo traversalTypeTree(GraphTypeTreeDto graphTypeTreeDto, List<String> updateClassCodes) {
        GraphTypeTreeVo vo = BeanUtil.copyProperties(graphTypeTreeDto, GraphTypeTreeVo.class);
        if (updateClassCodes.contains(vo.getClassCode())) {
            vo.setUpdateMark(true);
        }

        if (CollectionUtils.isNotEmpty(graphTypeTreeDto.getChildren())) {
            List<GraphTypeTreeVo> children = new LinkedList<>();
            graphTypeTreeDto.getChildren().forEach(child -> {
                children.add(traversalTypeTree(child, updateClassCodes));
            });
            vo.setChildren(null);
            vo.setChildrenVo(children);
        }

        return vo;
    }

    @Override
    public void deleteByLegendId(String legendId) {
        this.lambdaUpdate()
                .eq(GraphLegend::getLegendId, legendId)
                .remove();
    }

    @Override
    public List<GraphLegendGuestVo> queryLegendListGuest(String classCode) {
        if (StringUtils.isEmpty(classCode)) {
            return new LinkedList<>();
        }
        List<GraphLegend> graphLegendPos = this.lambdaQuery()
                .eq(GraphLegend::getClassCode, classCode)
                .list();
        List<String> fileIds = graphLegendPos.stream()
                .map(GraphLegend::getFileUrl)
                .collect(Collectors.toList());
        DmpFileBodyDto fileQueryDto = new DmpFileBodyDto();
        fileQueryDto.setFileIds(fileIds);
        Map<String, String> fileIdToUrlMap = dmpFileServiceClient.querySimpleFiles(new DmpQueryStringsDto(), fileQueryDto).getContent()
                .stream()
                .collect(Collectors.toMap(DtpFileSimpleInfoDto::getId, DtpFileSimpleInfoDto::getPersisFileDownloadUrl));
        return graphLegendPos
                .stream()
                .filter(it -> StringUtils.isNotBlank(it.getFileUrl()))
                .map(it -> toGuestVo(it, fileIdToUrlMap.get(it.getFileUrl())))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean initPicUrl() {
        List<GraphLegend> graphLegendPos = this.list();
        List<String> fileIds = graphLegendPos.stream()
                .map(GraphLegend::getFileUrl).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        DmpFileBodyDto fileQueryDto = new DmpFileBodyDto();
        fileQueryDto.setFileIds(fileIds);
        Map<String, String> fileIdToUrlMap = dmpFileServiceClient.querySimpleFiles(new DmpQueryStringsDto(), fileQueryDto).getContent()
                .stream()
                .collect(Collectors.toMap(DtpFileSimpleInfoDto::getId, DtpFileSimpleInfoDto::getPersisFileDownloadUrl,(v1, v2) -> v1));
        List<GraphLegend> updateList = graphLegendPos.stream().map(it -> {
            GraphLegend graphLegend = new GraphLegend();
            graphLegend.setId(it.getId());
            graphLegend.setSize(it.getSize());
            String url = fileIdToUrlMap.get(it.getFileUrl());
            if (StringUtils.isNotEmpty(url)) {
                graphLegend.setPicUrl(url);
            } else {
                graphLegend.setPicUrl(it.getFileUrl());
            }
            graphLegend.clearUpdateParameter();
            return graphLegend;
        }).collect(Collectors.toList());
        return updateBatchById(updateList);
    }
}




