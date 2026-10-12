package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.vo.KeyValueChildrenVO;
import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 照明-照明回路信息点VO
 * *
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-照明回路信息点VO")
public class LightingCircuitInfoCodeVO {

    @Schema(description = "功能类型定义")
    private InfoCodeDefineModel funcType;

    @Schema(description = "灯具类型定义")
    private InfoCodeDefineModel lampFuncType;

    @Schema(description = "功能类型关联灯具类型")
    private List<KeyValueChildrenVO<KeyValueVO>> funcTypeAndLampFuncTypes;

}
