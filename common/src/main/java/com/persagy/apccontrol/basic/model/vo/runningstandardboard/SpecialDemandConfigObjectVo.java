package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 16:38
 */
@Data
@Schema(description = "运行标准看板-特殊需求配置")
public class SpecialDemandConfigObjectVo {

    @Schema(name = "特殊空间总数")
    private Integer specialTotal;

    @Schema(name = "建筑模块")
    private List<SpecialDemandConfigObjectDetailVo> buildModule = new ArrayList<>();

    @Schema(name = "楼层模块")
    private List<SpecialDemandConfigObjectDetailVo> floorModule = new ArrayList<>();

    @Schema(name = "功能类型模块")
    private List<SpecialDemandConfigObjectDetailVo> functionModule = new ArrayList<>();

    @Schema(name = "空间模块")
    private List<SpecialDemandConfigObjectDetailVo> spaceModule = new ArrayList<>();

}
