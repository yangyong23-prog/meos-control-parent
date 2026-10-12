package com.persagy.apccontrol.basic.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/7 5:32 PM
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShellConfigBasic {

    @NotNull(message = "{key_21c11641de254d4ab73160e22253017a}")
    private String code;
    @NotNull(message = "{key_464506e488e34dfcb0dda9ba06137142}")
    private String value;
    @Nullable
    private String objectId;
    @Nullable
    private String objectName;
}
