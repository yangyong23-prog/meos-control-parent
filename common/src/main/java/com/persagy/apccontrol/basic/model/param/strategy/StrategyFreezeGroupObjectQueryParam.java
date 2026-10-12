package com.persagy.apccontrol.basic.model.param.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * 策略对象查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "策略对象查询参数")
public class StrategyFreezeGroupObjectQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "中央供冷系统ID, 为空时查询所有")
    private Collection<String> systemIds;

    @Schema(description = "空调末端系统ID, 为空时查询所有")
    private Collection<String> acatSystemIds;

    @Schema(description = "对象ID, 为空时查询所有")
    private String objectId;

    /**
     * 设置系统ID
     *
     * @return 当前实例
     * @since v5.7.0
     */
    public StrategyFreezeGroupObjectQueryParam setSystemId(String systemId) {
        this.systemIds = Optional.ofNullable(this.systemIds).orElseGet(ArrayList::new);
        this.systemIds.add(systemId);
        return this;
    }
}
