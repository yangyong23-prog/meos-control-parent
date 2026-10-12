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
public class SpecialBusinessTimeQuoteCountVo {


    @Schema(description = "工作历id")
    private String workId;

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "工作历名称")
    private String name;

    @Schema(description = "引用数")
    private Integer num;

    @Schema(description = "引用得空间ids")
    private List<String> objects;

}
