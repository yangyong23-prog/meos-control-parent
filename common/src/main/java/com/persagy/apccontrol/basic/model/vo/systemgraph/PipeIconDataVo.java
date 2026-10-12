package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author mj
 * @date 2023/4/12 11:18
 */
@ApiModel(value = "PipeIconVo", description = "")
@Data
public class PipeIconDataVo {
    @Schema(description = "管道类（对应颜色）", required = true)
    private String classCode;
    @Schema(description = "中心点", required = true)
    private Point3D centerPoint;
    @Schema(description = "类型（对应图形）", required = true)
    private PipeIconType type;
    @ApiModelProperty("立管名称")
    private String localName;
    private String id;
    private DmpObjTypeEnum objType = DmpObjTypeEnum.EQUIPMENT;
    private BaseColorVo colorInfo;

    @Getter
    @AllArgsConstructor
    public enum PipeIconType {
        WATER_PIPE(1, "key_a9008280f723418385200a7a07907d53"),
        WIND_PIPE(2, "key_8f6b10c97a874e2a9ea2ea3e5b40e74a"),
        GAS_PIPE(3, "key_d75fbbe46f294860b7f4acff7d16a279"),
        ;
        private int value;
        private String tagName;

        public String getTagName() {
            return LocaleMessageUtil.getMessage(tagName);
        }
    }
}
