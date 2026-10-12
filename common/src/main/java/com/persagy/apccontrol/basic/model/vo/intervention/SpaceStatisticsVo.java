package com.persagy.apccontrol.basic.model.vo.intervention;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需求空间统计
 *
 * @author yuecaipu
 * @date 2024/7/23 16:18
 * @since v5.240402.00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceStatisticsVo {

    /**
     * 空间总数
     */
    private Integer total;

    /**
     * 与当前对象的当前需求一致的空间数量
     */
    private Integer matchingCount;

    /**
     * 与当前对象的当前需求不一致，且当前按自动需求运行的空间数量
     */
    private Integer autoCount;

    /**
     * 与当前对象的当前需求不一致，且当前按人工干预需求运行的空间数量
     */
    private Integer interventionCount;


    public void addMatchingCount() {
        if (null == this.matchingCount) {
            this.matchingCount = 0;
        }
        this.matchingCount++;
    }

    public void addAutoCount() {
        if (null == this.autoCount) {
            this.autoCount = 0;
        }
        this.autoCount++;
    }

    public void addInterventionCount() {
        if (null == this.interventionCount) {
            this.interventionCount = 0;
        }
        this.interventionCount++;
    }

}
