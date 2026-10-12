package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import lombok.Data;

import java.util.List;

/**
 * 风险事件响应
 *
 * @author wxf
 * @since 7.250201.0
 */
@Data
public class DangerEventResponse {

    private int current;

    private int pages;

    private List<DangerEventInstance> records;

}
