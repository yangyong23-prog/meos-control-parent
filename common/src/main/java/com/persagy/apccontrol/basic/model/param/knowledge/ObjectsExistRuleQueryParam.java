package com.persagy.apccontrol.basic.model.param.knowledge;

import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 对象绑点信息查询
 */
@Data
@Schema(title = "对象绑点信息查询查询参数")
public class ObjectsExistRuleQueryParam {

    @Schema(description = "对象类型信息点筛选")
    @NotNull(message = "{key_ca3b62b001334782873d2fcbcaea80b7}")
    private List<ClassCode> criteria;

    @Schema(description = "筛选的象ID")
    private List<String> objIds;

    @Schema(description = "规则来源，为空的时候，云端查持久化规则，边缘端查询孪生规则")
    private TerminalEnum  terminal;


}
