package com.persagy.apccontrol.basic.model.vo.systemgraph;

import cn.hutool.json.JSONObject;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphGenerateInstanceDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/6/25 14:08
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SystemGraphInstanceVo extends GraphGenerateInstanceDto {

    @ApiModelProperty("系统图名称")
    private String name;

    @ApiModelProperty("系统实例名称")
    private String instanceName;

    private List<EquipmentNodeVo> equipmentNodeVos;

    private List<EquipmentNodeVo> passByVos;

    private List<GraphGroupVo> groupVos;

    @ApiModelProperty("管道节点信息")
    private List<LineNodeVo> lineNodeVos;

    @ApiModelProperty("管道信息（匹配样式）")
    private List<LineVo> lineVos;

    @ApiModelProperty("主管道（母线，立管，环管等）不展示辅助线")
    private List<MainPipeVo> mainPipeVos;

    @ApiModelProperty("区域(楼层，竖井)")
    private List<AreaVo> areaVos;

    @ApiModelProperty("文本框")
    private JSONObject textMap;

    @Schema(description = "系统图细节vo")
    private List<SystemGraphDetailVO> systemGraphDetailVOs;

    @Schema(description = "是否更新图纸细节")
    private Boolean isUpdateDetail;

    /**
     * 1. 完全展示整个图纸
     * 2. 100%缩放比例展示图纸
     * 3. 以图纸的宽度适配展示图纸
     * 4. 以图纸的高度适配展示图纸
     * 不填默认就是1
     */
    @Schema(description = "展示适配方式")
    private Integer displayMode;


    @Schema(description = "抽屉所有信息点列表")
    private List<RcInfoCodeCfg> drawerAllInfoCodes;

    @Schema(description = "图例信息列表")
    private List<LegendIconVO> legendIcons;

    /**
     * 自定义内容
     * 前端更新，前端消费,后端不消费
     */
    private Object customizeContent;
}
