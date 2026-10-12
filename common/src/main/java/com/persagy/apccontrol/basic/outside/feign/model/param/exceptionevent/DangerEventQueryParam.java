package com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent;

import com.persagy.apccontrol.basic.model.enumtype.dangerevent.StatusEnum;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import lombok.Data;

import java.util.List;

/**
 * 异常事件查询参数
 *
 * @author wxf
 * @since 7.250201.0
 */
@Data
public class DangerEventQueryParam extends PageQuery {

    /**
     * 当前页
     */
    private int current = 1;

    /**
     * 每页条数
     */
    private int size = 1000;

    /**
     * 风险事件实例id
     */
    private List<String> riskEventInstanceTreeIds;

    /**
     * 对象实例id
     */
    private List<String> rwdInstanceIds;


    /**
     * 风险状态
     */
    private List<StatusEnum> status;
}
