package com.persagy.apccontrol.basic.model.vo.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * 查询-运行播报统计
 *
 * @author yuecaipu
 * @date 2024/11/5 13:58
 * @since v5.241101.00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor()
public class TodayLogSummaryVo {

    /**
     * 自控日志数量
     */
    private Long autoCount;

    /**
     * 所有日志数量
     */
    private Long total;

    /**
     * 最新3条自动日志
     */
    private List<RunningLogHomePageVO> latestContent;
    
    //按照时间排序只取前三条LatestContent
    public void reorder() {
        // 按照时间排序
        latestContent.sort(Comparator.comparing(RunningLogHomePageVO::getCreateTime).reversed());
        
        // 只保留最新的三条记录
        if (latestContent.size() > 3) {
            this.latestContent = latestContent.subList(0, 3);
        }
    }
    
    /**
     * 合并结果
     *
     * @param logSummaryVo
     */
    public void combinedResult(TodayLogSummaryVo logSummaryVo) {
        if (logSummaryVo == null) {
            return;
        }
        //合并结果
        setAutoCount(getAutoCount() + logSummaryVo.getAutoCount());
        setTotal(getTotal() + logSummaryVo.getTotal());
        
        // 处理最新内容列表
        List<RunningLogHomePageVO> otherLatestContent = logSummaryVo.getLatestContent();
        if (otherLatestContent == null || otherLatestContent.isEmpty()) {
            return;
        }
        
        List<RunningLogHomePageVO> currentLatestContent = getLatestContent();
        if (currentLatestContent == null) {
            setLatestContent(otherLatestContent);
        } else {
            currentLatestContent.addAll(otherLatestContent);
        }
        
        // 重新排序以保留最新的三条记录
        reorder();
    }
    
    public Long getTotal() {
        return total == null ? 0L : total;
    }
    
    public Long getAutoCount() {
        return autoCount == null ? 0L : autoCount;
    }
}
