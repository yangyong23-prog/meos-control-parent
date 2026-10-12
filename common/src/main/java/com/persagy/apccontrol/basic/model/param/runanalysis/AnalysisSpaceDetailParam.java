package com.persagy.apccontrol.basic.model.param.runanalysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class AnalysisSpaceDetailParam extends RunAnalysisBaseParam {

    /**
     * 空间id
     */
    @NotBlank(message = "{key_04c419cafc72438487f7698c8b2cbadd}")
    private String spaceId;

}
