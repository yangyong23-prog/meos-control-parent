package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarPlanContentDTO;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作历计划数据保存参数
 *
 * @author zhangqiankun
 * @since v5.7.0
 */
@Data
public class MidCalendarPlanDataInsertParam {

    public static final int MAX_PLAN_DATA_COUNT = 50;

    /**
     * 不存在对象工作历时, 是否自动创建工作历
     * <ul>
     *     <li><b>1</b> - 是</li>
     *     <li><b>0</b> - 否</li>
     * </ul>
     */
    private Integer autoCreate;

    /**
     * 保存内容, 长度不能超过50条
     */
    @Valid
    @Size(max = MAX_PLAN_DATA_COUNT, message = "{key_42db49f113ed427c99ced788ee9b1aec}")
    private List<PlanData> datas;

    /**
     * 计划数据
     *
     * @author zhangqiankun
     * @since v5.7.0
     */
    @Data
    public static class PlanData {

        /**
         * 对象ID
         */
        private String objId;

        /**
         * 日期时间
         */
        private LocalDateTime currentDay;

        /**
         * 价值计算结果
         */
        @Valid
        private List<MidCalendarPlanContentDTO> planContents;

    }
}
