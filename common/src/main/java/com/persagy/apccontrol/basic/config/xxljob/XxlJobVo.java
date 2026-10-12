package com.persagy.apccontrol.basic.config.xxljob;

import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @Author : yuyao
 * @Date : 2022/9/11 16:07
 * @Description ：XxlJobVo
 */
@Schema(description = "XxlJobVo")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class XxlJobVo {

    @Schema(description = "任务描述")
    private String jobDesc;
    @Schema(description = "时间配置")
    private String scheduleConf;
    @Schema(description = "任务Handle")
    private String jobHandle;
    @Schema(description = "参数")
    private String executorParam;
    @Schema(description = "执行参数")
    private ExecutorBlockStrategyEnum executorBlockStrategy;
    @Schema(description = "失败重试次数")
    private String executorFailRetryCount;

    public XxlJobVo() {
    }
    
    
    public XxlJobVo(String jobDesc, String scheduleConf, String jobHandle, ExecutorBlockStrategyEnum executorBlockStrategy,
                    String executorParam) {
        this.jobDesc = jobDesc;
        this.scheduleConf = scheduleConf;
        this.jobHandle = jobHandle;
        this.executorParam = executorParam;
        this.executorBlockStrategy = executorBlockStrategy;
        this.executorFailRetryCount = "0";
    }
}
