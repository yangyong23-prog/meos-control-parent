package com.persagy.apccontrol.basic.model.vo.lighting;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 查询-需求变化
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:56
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "查询-需求变化")
public class LightingHoverSpaceAbnormalDataVO {

    /**
     * 异常事件信息
     */
    private List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoList;

    /**
     * 异常设备信息，key为设备id，value为jsonObject，包含实例名称，实例异常信息
     */
    private List<JSONObject> deviceInfo;

    /**
     * 标签信息
     */
    private LightingHoverTagVO tags;

    @Schema(description = "参考面照度")
    private String workPlaneLux;

}
