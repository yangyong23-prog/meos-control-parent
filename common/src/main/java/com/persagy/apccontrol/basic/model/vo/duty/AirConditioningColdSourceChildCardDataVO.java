package com.persagy.apccontrol.basic.model.vo.duty;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * 空调冷源子卡片
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AirConditioningColdSourceChildCardDataVO extends BasicsChildCardDataVO {

    @Schema(description = "系统对象")
    private DtpRwdObjectDTO system;

    @Schema(description = "是否存在系统图")
    private Boolean isExistsSystemGraph;

    @Schema(description = "图形类型代码")
    private String graphTypeCode;

    @Schema(description = "异常事件数量")
    private Integer exceptionEventCount;

    @Schema(description = "信息点值")
    private Map<String, InfoCodeValueVO> infoCodeAndValue;

}
