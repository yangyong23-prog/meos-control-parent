package com.persagy.apccontrol.basic.model.dto.rwd;


import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class CustomBasePropertyDTO extends BasePropertyDTO {

    // 是否可用
    private Boolean enable;

}
