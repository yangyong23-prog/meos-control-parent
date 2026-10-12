package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconDeleteDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconUpdateDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* @author persagy
* @description 针对表【graph_icon(图标库)】的数据库操作Service
* @createDate 2024-07-15 15:33:07
*/
public interface GraphIconService extends IService<GraphIcon> {

    List<GraphIconVo> queryAllGraphIcon();

    Map<String, List<GraphIconVo>> getGraphIconMap();

    @Transactional(rollbackFor = Exception.class)
    void syncGraphIcon();

    List<GraphIconVo> queryLegendList(String classCode);

    void updateLegend(GraphIconUpdateDto graphIconUpdateDto);

    void delete(GraphIconDeleteDto deleteDto);

    List<GraphIcon> getLegendByLegendId(String legendId);

    Boolean initPicUrl();
}
