package com.persagy.apccontrol.basic.model.vo.debugtool;

import com.persagy.apccontrol.basic.model.base.YesNoEnum;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信息点配置的bdtp交付信息状态VO
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/1/22 10:56
 * @since v7.250102.0
 */
@Data
@Schema(title = "调试工具-信息点配置的bdtp交付信息状态VO")
public class DebugToolInfoCodeConfigStatusVO {

    @Schema(description = "信息点编码")
    private String infoCode;

    @Schema(description = "信息点名称")
    private String infoCodeName;

    @Schema(description = "绑点状态")
    private YesNoEnum bindingPointStatus;

    @Schema(description = "规则状态")
    private YesNoEnum instantiationRuleStatus;

    @Schema(description = "最新一条数值")
    private Object dataValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "最新数据的时间")
    private LocalDateTime updateTime;

    @Schema(description = "数据过期时间")
    private LocalDateTime dataIndicateTime;

    @Schema(description = "分精度规则")
    private String minuteDisplay;

    @Schema(description = "展示样式")
    private String showStyle;

    @Schema(description = "数据源")
    private List<DtpKnowledgeInfoCdeDataSourceVO> dataSources;

}
