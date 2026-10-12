package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdControlInfoCodeTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 物理世界-孪生控制对象响应
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpRwdTwinControlObjectResponse extends BaseResponse {

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 所属系统ID, 如果无系统ID则为对象ID
     */
    private String systemId;

    /**
     * 点位列表
     */
    private List<Point> points;

    /**
     * 物理世界-孪生控制信息点响应
     *
     * @author JiYinchuan
     * @since 5.240601.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class Point extends BaseResponse {

        /**
         * 信息点编码
         */
        private String code;

        /**
         * 信息的名称
         */
        private String name;

        /**
         * 数据类型
         */
        @JsonAlias("origDataType")
        private String dataType;

        /**
         * 点位类型
         */
        private DtpRwdControlInfoCodeTypeEnum pointType;

    }
}
