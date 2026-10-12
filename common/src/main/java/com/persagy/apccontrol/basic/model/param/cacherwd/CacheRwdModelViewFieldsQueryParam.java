package com.persagy.apccontrol.basic.model.param.cacherwd;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 缓存模型视图字段批量查询参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Schema(description = "缓存模型视图字段批量查询参数")
public class CacheRwdModelViewFieldsQueryParam {

    /**
     * 一级运行监测类型，用于选择 READY 模型视图缓存或未 READY 实时查询。
     *
     * @since v7.260701.0
     */
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    @Schema(description = "运行监测一级类型", required = true)
    private @NonNull RunningMonitorTypeEnum runningMonitorType;

    /**
     * 待查询的真实对象 ID 集合。
     *
     * @since v7.260701.0
     */
    @NotEmpty(message = "{key_9f4f3dd621fb415e88ff0e83734a6710}")
    @Schema(description = "对象 ID 集合", required = true)
    private @NonNull List<String> objectIds;
}
