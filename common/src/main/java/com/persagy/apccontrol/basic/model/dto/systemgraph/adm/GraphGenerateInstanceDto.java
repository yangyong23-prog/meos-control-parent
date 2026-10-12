package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.vo.systemgraph.EquipmentNodeVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendIconVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 关系图实例
 *
 * @author houyuanzhi
 * @date 2023/6/1 11:53
 */
@Data
public class GraphGenerateInstanceDto {

    /**
     * 模板id
     */
//    private String templateId;

//    private String templateOriId;

    private String generateId;

    /**
     * 画布
     */
    private FrameDto frame;

    private List<AreaDto> areas;

    /**
     * 设备列表
     */
    private List<EquipmentNodeDto> equipmentNodes;

    /**
     * 主管道（用于合并管线，计算管道二通三通）
     */
    private List<MainPipeDto> mainPipes;


    private List<GraphGroupDto> groups;

    /**
     * 线路dto
     */
    private List<LineDto> lines;

    private List<EquipmentNodeDto> passBys;

    /**
     * 全量返回值
     */
    private JSONObject infoObject;

    /**
     * 放大倍数  240901 已弃用
     */
    @Deprecated
    private Integer legendScaleRatio;

    /**
     * 字体大小
     */
    private Integer fontSize;
    /**
     * 附件列表
     **/
    private Map<String, List<EquipmentNodeVo>> attachPairMap;

    private List<LineDto> linePaths;

    private List<ParallelLine> parallelLines;

    @Schema(description = "图例信息列表")
    private List<LegendIconVO> legendIcons;

}
