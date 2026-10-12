package com.persagy.apccontrol.basic.structure.cacherwd.model.vo;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 构建任务状态响应 VO
 *
 * @author wxf
 * @date 2026-03-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheRwdBuildStatusVO {

    /**
     * 运行监测类型
     */
    @Schema(description = "运行监测类型")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 构建状态：true-构建成功/空闲，false-正在构建/失败
     */
    @Schema(description = "构建状态：true-构建成功/空闲，false-正在构建/失败")
    private Boolean isBuilding;

    /**
     * 最后更新时间（从 MySQL 中该 monitorType 对应的数据 updateTime 字段获取）
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

}
