package com.persagy.apccontrol.basic.model.entity.systemgraph.base.simple;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.IconType;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author mj
 * @date 2022/7/13 11:02
 */
@ApiModel(value = "SimpleObjectVo", description = "简单对象")
@Data
@EqualsAndHashCode
public class SimpleObjectVo {
    @Schema(description = "id", required = true)
    private String id;
    @Schema(description = "设备类编码", required = true)
    private String classCode;
    @Schema(description = "设备类名称", required = true)
    private String className;
    @Schema(description = "对象本地名称", required = true)
    private String localName;
    @Schema(description = "对象类型", required = true)
    private DmpObjTypeEnum objType = DmpObjTypeEnum.EQUIPMENT;
    @Schema(description = "对象图标类型")
    private IconType iconType;
    @Schema(description = "空间对象用的树节点code")
    private String nodeCode;

}
