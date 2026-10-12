package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphLegendUpdateDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphLegend;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendGuestVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphTypeTreeVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* @author persagy
* @description 针对表【graph_legend】的数据库操作Service
* @createDate 2024-07-15 15:33:14
*/
public interface GraphLegendService extends BaseService<GraphLegend> {

    List<GraphLegendVo> queryAllGraphLegend();

    Map<String, GraphLegendVo> getGraphLegendMap();

    @Transactional(rollbackFor = Exception.class)
    void syncGraphLegend();

    List<GraphLegendVo> queryLegendList(String classCode);

    void updateLegend(String legendId, GraphLegendUpdateDto graphLegendUpdateDto);

    List<GraphTypeTreeVo> typeTree();

    void deleteByLegendId(String legendId);

    List<GraphLegendGuestVo> queryLegendListGuest(String classCode);

    Boolean initPicUrl();
}
