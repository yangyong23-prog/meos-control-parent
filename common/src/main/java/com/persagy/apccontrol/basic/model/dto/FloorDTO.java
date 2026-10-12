package com.persagy.apccontrol.basic.model.dto;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 楼层dto
 *
 * @author MAF
 * @date 2023/12/12
 */
@Data
@ToString(callSuper = true)
public class FloorDTO extends SystemDTO {

    @Schema(description = "楼层ID")
    private String floorId;

    public void check1() {
        ParameterCheck.isTrue(StringUtils.isBlank(getProjectId()), LocaleMessageUtil.getMessage("key_d4be1b60bd32403287b0a47366af7092"));
//        ParameterCheck.isTrue(StringUtils.isBlank(getSystemId()) && StringUtils.isBlank(floorId), "系统id和楼层id不能同时为空");
    }
}
