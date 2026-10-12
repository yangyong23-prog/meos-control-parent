package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:37
 */
@Data
@Schema(description = "运行标准看板-项目营业时间基本信息配置")
public class BusinessTimeObjectVo {

    @Schema(description = "工休日标签")
    private List<String> calendarTags;

    @Schema(description = "项目营业时间")
    private List<String> businessTimeRange;

    @Schema(description = "特殊营业时间对象集合")
    private List<BusinessTimeCustomConfigVo> customConfig;

    @Schema(description = "特殊营业时间数量")
    private Integer customConfigCount;
}
