package com.persagy.apccontrol.basic.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 空调需求时间
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/7/24 14:15
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AirConditionDemandDTO {

    /**
     * 当前时间
     */
    private LocalDateTime dateTime;

    /**
     * 是否有需求
     */
    private Boolean enable;

}
