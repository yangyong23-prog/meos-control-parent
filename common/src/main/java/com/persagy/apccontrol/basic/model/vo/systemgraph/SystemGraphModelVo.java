package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/9/15 15:39
 */
@Data
public class SystemGraphModelVo {
    @ApiModelProperty("设备实例列表")
    List<DtpRwdObjectDTO> equipments;

    @ApiModelProperty("空间实例列表")
    List<DtpRwdObjectDTO> spaces;

    @ApiModelProperty("系统实例列表")
    List<DtpRwdObjectDTO> systems;
}
