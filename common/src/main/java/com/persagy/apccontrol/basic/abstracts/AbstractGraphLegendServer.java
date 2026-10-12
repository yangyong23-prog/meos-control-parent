package com.persagy.apccontrol.basic.abstracts;

import com.persagy.apccontrol.basic.model.vo.systemgraph.InfoValueVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendIconVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractGraphLegendServer {

    /**
     * 初始化图图例和图标
     *
     * @param vo vo
     */
    public abstract void initGraphLegendAndIcons(SystemGraphInstanceVo vo);

    public abstract String getHost();

    protected Map<String, LegendIconVO> bulidIconMap(SystemGraphInstanceVo vo) {
        //处理图标信息
        List<LegendIconVO> legendIcons = vo.getLegendIcons();
        final Map<String, LegendIconVO> legendIconMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(legendIcons)) {
            legendIcons.forEach(e -> {
                List<InfoValueVO> infoValues = e.getInfoValues();
                if (CollectionUtils.isEmpty(infoValues)) {
                    return;
                }
                infoValues.forEach(infoValueVO -> {
                    infoValueVO.setHost(getHost());
                });
            });
            legendIconMap.putAll(legendIcons.stream().collect(Collectors.toMap(LegendIconVO::getLegendOriId, Function.identity(), (v1, v2) -> v2)));
        }
        return legendIconMap;
    }

}
