package com.persagy.apccontrol.common.task;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 手动设定点位下发的执行结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2022/12/12 16:01
 */
@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ObjectCommandSendResult {

    // 执行结果
    private boolean success;

    // 对象ID
    private String objectId;

    // 信息点编码
    private String infoCode;

    // 失败信息
    private String exception;

}
