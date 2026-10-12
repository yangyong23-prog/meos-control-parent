package com.persagy.apccontrol.basic.structure.cacherwd.model.vo;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 缓存统计结果 VO
 *
 * @author wxf
 * @since 7.260302.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缓存统计结果")
public class CacheRwdStatisticsVO {

    /**
     * 按运行监测类型分组的缓存数量统计
     * key: 运行监测类型枚举值
     * value: 该类型的缓存记录数量
     */
    @Schema(description = "按运行监测类型分组的缓存数量统计")
    private Map<RunningMonitorTypeEnum, Long> countByMonitorType;

    /**
     * 缓存总数量
     */
    @Schema(description = "缓存总数量")
    private Long totalCount;

}
