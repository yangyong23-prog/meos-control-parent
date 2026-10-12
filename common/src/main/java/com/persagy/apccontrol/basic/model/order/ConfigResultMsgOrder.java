package com.persagy.apccontrol.basic.model.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author : yuyao
 * @Date : 2022/3/23 9:51 AM
 * @Description ：
 */
@Schema(description = "配置日志结果信息")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
public class ConfigResultMsgOrder {


    @Schema(description = "操作对象设备本地名称")
    private String objectName;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "修改前内容")
    private String beforeModify;

    @Schema(description = "classCode对应名称")
    private String classCodeName;
}
