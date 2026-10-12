package com.persagy.apccontrol.task.param;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;

/**
 * 需求配置结果初始化任务参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
public class DemandConfigResultInitTaskParam {

    /**
     * 指定初始化集团编码，为空时初始化所有有效集团。
     */
    private @Nullable LinkedHashSet<@NonNull String> groupCodes;

    /**
     * 指定初始化项目ID，为空时初始化集团下所有有效项目。
     */
    private @Nullable LinkedHashSet<@NonNull String> projectIds;

    /**
     * 指定初始化的需求类型，为空时初始化所有已支持类型。
     *
     * <p>升级场景下可只传入新增类型以避免重复执行已完成类型的全量初始化。
     * 例如 {@code {"demandTypes": ["VENTILATION"]}} 仅初始化通风需求。</p>
     *
     * @since v7.260801.0
     */
    private @Nullable LinkedHashSet<@NonNull DemandTypeEnum> demandTypes;

}
