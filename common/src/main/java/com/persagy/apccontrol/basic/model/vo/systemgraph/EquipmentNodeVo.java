package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.EquipmentNodeDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.SystemGraphType;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/6/25 14:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EquipmentNodeVo extends EquipmentNodeDto {


    private List<GraphLegendVo> legendUrlVo;

    private SystemGraphType systemGraphType = SystemGraphType.NONE;

    private String styleCode;

    /**
     * 别名
     */
    @Schema(description = "别名")
    private String aliasName;

    /**
     * 滤镜状态 因为要添加滤镜表达
     */
    @Schema(description = "滤镜状态对象")
    private FilterStatusVO filterStatusVO;

    @Schema(description = "滤镜状态")
    private String filterStatus;

    @Schema(description = "信息点配置列表")
    private List<RcInfoCodeCfg> fixedInfoCodes;

    @ApiModelProperty("交互配置")
    private InteractiveConfigVo interactiveConfigVo;

    /**
     * 小附件放大倍数
     */
    private Double scale;

    private String className;

    @Schema(description = "抽屉所有信息点列表")
    private List<RcInfoCodeCfg> drawerAllInfoCodes;

    /** web自定义数据,前端存储，前端消费 */
    private Object webCustomData;

}
