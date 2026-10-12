package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:37
 */
@Data
@Schema(description = "运行标准看板-项目营业时间基本信息-特殊营业时间对象配置")
public class BusinessTimeCustomConfigVo {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象本地编码")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "对象类编码")
    private String classCode;

    @Schema(description = "对象类型")
    private String objectType;


    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "楼层本地名称")
    private String floorLocalName;

    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "建筑本地名称")
    private String buildingLocalName;

  //  @Schema(description = "引用空间详情")
  //  private ReferenceSpaceVo referenceSpaceVo;

    @Schema(description = "特殊营业时间")
    private List<String> businessTimeRange;

}
