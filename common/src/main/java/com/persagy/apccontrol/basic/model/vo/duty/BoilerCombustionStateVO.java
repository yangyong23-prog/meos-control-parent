package com.persagy.apccontrol.basic.model.vo.duty;

import com.persagy.apccontrol.basic.model.enumtype.dutyHome.CombustionStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 锅炉燃烧状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/11/15 15:49
 * @since v5.241002.0
 */
@Data
public class BoilerCombustionStateVO {

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "锅炉燃烧状态")
    private CombustionStateEnum combustionStateEnum;

}
