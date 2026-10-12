package com.persagy.apccontrol.basic.model.dto.systemgraph.factory;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphLegend;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author houyuanzhi
 * @date 2023/8/11 18:36
 */
public class GraphIconFactory {

    public static GraphIcon toEntity(GraphLegend po, Map<String, String> conditionMap) {
        GraphIcon result = BeanUtil.copyProperties(po, GraphIcon.class);
        result.setStatus(LegendSourceStatus.DEFAULT);
        result.setCondition(JSON.toJSONString(conditionMap));
        result.setId(null);
        return result;
    }

    public static GraphIcon toEntity(GraphIcon po) {
        GraphIcon result = new GraphIcon();
        BeanUtil.copyProperties(po, result);
        if (StringUtils.isNotBlank(po.getPicUrl())) {
            result.setFileUrl(po.getPicUrl());
        }
        return result;
    }

    public static GraphIconVo toVo(GraphIcon po) {
        GraphIconVo result = new GraphIconVo();
        BeanUtil.copyProperties(po, result);
        if (StringUtils.isNotBlank(po.getPicUrl())) {
            result.setFileUrl(po.getPicUrl());
        }
        return result;
    }

}
