package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.List;

/**
 * 设备对象iot数据
 *
 * @author yang
 * @date 2024/3/8 16:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备对象iot数据")
@EqualsAndHashCode(callSuper = false)
public class ObjectIotVo {

    /**
     * 对象id（设备或系统）
     */
    private String objectId;

    /**
     * iot信息
     */
    private List<IotInfoCodeFieldVo> iotInfoCodeFieldVos;

}
