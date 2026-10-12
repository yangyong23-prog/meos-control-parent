package com.persagy.apccontrol.basic.model.vo.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunStatusVo {

    /**
     * 运行关的数量
     */
    private int off;
    /**
     * 运行开的数量
     */
    private int on;
    /**
     * 无数据的数量
     */
    private int unknown;
}
