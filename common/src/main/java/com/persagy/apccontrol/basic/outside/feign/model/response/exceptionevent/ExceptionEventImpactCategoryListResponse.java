package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 异常事件实际影响
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExceptionEventImpactCategoryListResponse extends BaseResponse {

    /**
     * 关键性
     *
     * <ul>
     *     <li><b>PIVOTAL</b> - 关键</li>
     *     <li><b>OTHER</b> - 其他</li>
     * </ul>
     */
    private String crucial;

    /**
     * 影响类型ID
     */
    private Long id;

    /**
     * 影响类型名称
     */
    private String name;

}
