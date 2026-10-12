package com.persagy.apccontrol.basic.controller.shellconfig.param;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/8 6:02 PM
 */
@Data
public class QueryShellConfigByInfoCodeParam {

    @NotNull(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    @Schema(name = "项目id")
    private String projectId;

    @Nullable
    @Schema(name = "对象id")
    private String objectId;

    @Nullable
    @Schema(name = "信息点编码")
    private String infoCode;

    @Nullable
    @Schema(name = "对象类型")
    private String classCode;

    @Nullable
    @Schema(name = "壳配置编码")
    private String code;

    public void check() {
        if (StringUtils.isEmpty(this.code) && StringUtils.isEmpty(this.infoCode)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d26b9abdd7a04daeabd962959df039e8"));
        }
    }
}
