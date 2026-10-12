package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统图编辑信息点定义
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/6/27 15:12
 * @since v5.240502.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemGraphEditorInfoCodeVO extends RcInfoCodeCfg {

    @ApiModelProperty("iot数据")
    private String ipValue;

}
