package com.persagy.apccontrol.basic.outside.dto.riskevent;

import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 14:19
 */
@Getter
@Setter
@ToString
public class CheckDeviceInfoDTO {

    @Schema(description = "项目编码")
    private String projectId;

    @Schema(description = "是否需要IOT数据")
    private Boolean resultIot = false;

    @Schema(description = "对象集合")
    private List<ObjectClassCodeDTO> iotDataRequestVos;

    /**
     * 图名
     */
    @ApiModelProperty("图名")
    private String graphTypeCode;

    /**
     * hover状态
     */
    @ApiModelProperty("hover状态")
    private String hover;

    /**
     * 对象类型
     */
    @ApiModelProperty("对象类型")
    private ObjectTypeEnum objectType;

}
