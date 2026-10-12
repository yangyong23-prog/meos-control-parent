package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.UpdateGraphDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;

import java.util.List;

/**
* @author persagy
* @description 针对表【system_graph_data(系统图数据表)】的数据库操作Service
* @createDate 2024-06-25 11:50:39
*/
public interface SystemGraphDataService extends IService<SystemGraphData> {

    /**
     * 编辑系统图
     *
     * @param systemGraphDataId 系统图数据id
     * @param dto               到
     * @return {@link Boolean }
     */
    Boolean editSystemGraph(Long systemGraphDataId, List<UpdateGraphDTO> dto);

    /**
     * 保存系统图数据
     *
     * @param requestParam 请求参数
     * @param vo           vo
     * @param status       地位
     * @return {@link SystemGraphData }
     */
    SystemGraphData saveSystemGraphData(String requestParam, SystemGraphInstanceVo vo, EnumSystemGraphStatus status);

    Boolean updateSystemGraphDataStatusById(Long systemGraphDataId, EnumSystemGraphStatus status);
}
