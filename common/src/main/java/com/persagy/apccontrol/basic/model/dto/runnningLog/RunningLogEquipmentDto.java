package com.persagy.apccontrol.basic.model.dto.runnningLog;

import lombok.Data;

@Data
public class RunningLogEquipmentDto {

    private String objectId;

    private String objectName;
    
    //对象类型
    private String objectCategory;
}
