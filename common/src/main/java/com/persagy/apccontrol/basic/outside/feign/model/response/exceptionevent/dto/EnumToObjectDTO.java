package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto;

import lombok.Data;

/**
 * 枚举对象dto，风险事件有些枚举可以动态调整，我们不用枚举接受，直接获取他们返回的数据
 *
 * @author MuAnFu
 * @date 2025-09-26
 */
@Data
public class EnumToObjectDTO {

    /** 编码 */
    private String code;
    /** 描述 */
    private String desc;
    /** 名字 */
    private String name;
}
