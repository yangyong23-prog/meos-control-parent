package com.persagy.apccontrol.basic.model.param.humiture;

import com.persagy.apccontrol.basic.model.vo.KeyValueChildrenVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 温湿度管理实例树参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "温湿度管理实例树参数")
public class HumitureTargetInstanceTreeParam extends KeyValueChildrenVO<HumitureTargetInstanceTreeParam> {

    @Schema(description = "忽略空间ID, 仅当生效范围为 [楼层] 时传入")
    private Set<String> ignoreSpaceIds;

}
