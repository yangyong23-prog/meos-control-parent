
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author 薛涛
 * @Description 
 * @Date 15:58 2024/9/3
 * @Param 
 * @return 
 **/
@Data
public class SubstationObjectRwdStatistics {



    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地ID")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;


    @Schema(description = "负载率")
    private String lULoadRate;
}