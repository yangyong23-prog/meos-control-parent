package com.persagy.apccontrol.basic.model.vo.debugtool;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 调试工具-模版类型和设备类
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
@Data
@Schema(title = "调试工具-模版类型和设备类")
public class DebugToolTemplateTypeAndClassCodeVO {

    @Schema(description = "运行监测类型")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "运行监测类型名称")
    private String runningMonitorTypeName;

    @Schema(description = "设备类编码列表")
    private List<ClassCode> classCodes;

    /**
     * 设备类编码
     *
     * @author JiYinchuan
     * @since 7.250102.0
     */
    @Data
    @Schema(title = "设备类编码")
    public static class ClassCode {

        @Schema(description = "对象类型")
        private DtpRwdObjectTypeEnum objectType;

        @Schema(description = "对象类型名称")
        private String objectTypeName;

        @Schema(description = "设备类编码")
        private String classCode;

        @Schema(description = "设备类编码名称")
        private String classCodeName;

        @Schema(description = "关联模版ID")
        private Set<Long> relTemplateIds;

    }
}
