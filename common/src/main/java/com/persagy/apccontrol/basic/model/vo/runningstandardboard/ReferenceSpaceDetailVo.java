package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:37
 */
@Data
@Schema(description = "运行标准看板-项目营业时间基本信息-特殊营业时间对象-引用对象配置")
public class ReferenceSpaceDetailVo {

    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "建筑本地名称")
    private String buildingLocalName;

    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "楼层本地名称")
    private String floorLocalName;

    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "空间本地名称")
    private String spaceLocalName;

}
