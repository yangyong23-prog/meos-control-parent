package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:37
 */
@Data
@Schema(description = "运行标准看板-项目营业时间基本信息-特殊营业时间对象-引用配置")
public class ReferenceSpaceVo {

    @Schema(description = "引用空间数")
    private Integer spaceTotal;

    @Schema(description = "引用空间详情")
    private List<ReferenceSpaceDetailVo> spaceDetail;

    @Schema(description = "引用空间id列表")
    private List<String> objIds;

}
