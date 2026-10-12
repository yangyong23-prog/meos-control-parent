package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import lombok.Data;
import lombok.ToString;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/23 15:35
　　 * @description: 能耗详情
*/
@Data
@ToString(callSuper = true)
public class DifyResponse  {

    /**
     * 状态
     */
    private String status;

    /**
     * 消息
     */
    private String message;


}
