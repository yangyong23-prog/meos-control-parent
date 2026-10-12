package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 工作历响应
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarResponse extends BaseResponse {

    /**
     * 工作历ID
     */
    private String id;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 工作历名称
     */
    private String name;

    /**
     * 工作历关联对象
     */
    private List<LinkObject> linkObjects;


    /**
     * 工作历关联对象
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class LinkObject extends BaseResponse {

        /**
         * 对象ID
         */
        private String objectId;

        /**
         * 对象名称
         */
        private String objectName;

        /**
         * 对象类型
         */
        private MidCalendarObjectTypeEnum objType;

    }
}
