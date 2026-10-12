package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemGraphVO {

    private Long id;
    /**
     * 设备类code
     */
    private String classCode;

    /**
     * 系统实例id
     */
    private String systemObjectId;

    /**
     * 关系图类
     */
    private String graphTypeCode;

    /**
     * 系统图数据id
     */
    private Long systemGraphDuplicateId;

    /**
     * 关系图图名称
     */
    private String graphName;

    public static SystemGraphVO toVO(SystemGraph po) {
        return SystemGraphVO.builder()
                .id(po.getId())
                .classCode(po.getClassCode())
                .systemObjectId(po.getSystemObjectId())
                .graphTypeCode(po.getGraphTypeCode())
                .systemGraphDuplicateId(po.getSystemGraphDuplicateId())
                .graphName(po.getGraphName())
                .build();
    }

    public static List<SystemGraphVO> toVOList(List<SystemGraph> po) {
        List<SystemGraphVO> list = new ArrayList<>();
        for (SystemGraph systemGraph : po) {
            list.add(toVO(systemGraph));
        }
        return list;
    }
}
