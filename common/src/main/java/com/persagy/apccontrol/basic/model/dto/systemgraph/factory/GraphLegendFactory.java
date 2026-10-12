package com.persagy.apccontrol.basic.model.dto.systemgraph.factory;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendAnchor;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendText;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphLegendDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphLegend;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendGuestVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendAnchorVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendTextVo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/11 18:36
 */
public class GraphLegendFactory {

    public static GraphLegend toEntity(GraphLegendDto dto, GraphLegend graphLegend, List<String> attachTypeClassCodes){
        if (dto == null && graphLegend == null){
            return null;
        }
        //图标被bdtp删除的情况
        if (dto == null){
            graphLegend.setStatus(LegendSourceStatus.DELETED);
            return graphLegend;
        }
        GraphLegend result = toEntity(dto,attachTypeClassCodes);

        if (graphLegend == null){
            return result;
        }
        result.setStatus(graphLegend.checkLegendVersion(dto.getLegendVersion()));
        result.setFileUrl(graphLegend.getFileUrl());
        result.setPicUrl(graphLegend.getPicUrl());
        if (LegendSourceStatus.DEFAULT.equals(result.getStatus())){
            result.setAnchors(graphLegend.getAnchors());
        }
        return result;
    }

    private static GraphLegend toEntity(GraphLegendDto dto,List<String> attachTypeClassCodes){
        GraphLegend result = BeanUtil.copyProperties(dto, GraphLegend.class);
        result.setLegendId(dto.getOriId());
        result.setSize(dto.getLegendContent().getSize());
        result.setAnchors(BeanUtil.copyToList(dto.getLegendContent().getAnchors(), LegendAnchor.class));
        result.setTexts(BeanUtil.copyToList(dto.getLegendContent().getTexts(), LegendText.class));
        result.setStatus(LegendSourceStatus.DEFAULT);
        result.setId(null);
        result.setAttachType(attachTypeClassCodes.contains(dto.getClassCode()));
        result.setLegendVersion(dto.getLegendVersion());
        return result;
    }

    public static GraphLegendVo toVo(GraphLegend po){
        GraphLegendVo result = new GraphLegendVo();
        BeanUtil.copyProperties(po,result);
        result.setAnchors(BeanUtil.copyToList(po.getAnchors(), LegendAnchorVo.class));
        result.setTexts(BeanUtil.copyToList(po.getTexts(), LegendTextVo.class));
        if (StringUtils.isNotBlank(po.getPicUrl())){
            result.setFileUrl(po.getPicUrl());
        }
        return result;
    }

    public static GraphLegendGuestVo toGuestVo(GraphLegend po, String fileUrl){
        GraphLegendGuestVo result = new GraphLegendGuestVo();
        result.setLegendName(po.getLegendName());
        result.setFileUrl(po.getFileUrl());
        result.setClassCode(po.getClassCode());
        if (StringUtils.isNotBlank(fileUrl)){
            result.setFileUrl(fileUrl);
        }
        return result;
    }

}
