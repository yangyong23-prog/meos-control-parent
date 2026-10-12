package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview;

import lombok.Data;

import java.util.Map;

/**
 * 空间运行结果 DTO
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Data
public class SpaceEnvironmentalQualityDTO {

    /**
     * 温度达标率
     */
    private String tdbComplianceRate;

    /**
     * 标签计数
     */
    private Map<String, Integer> tagCount;

    /**
     * 空间环境质量结果
     *
     * @return 空间环境质量结果
     * @since 7.260601.0
     */
    public static SpaceEnvironmentalQualityDTO empty() {
        return new SpaceEnvironmentalQualityDTO();
    }
}
