package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdCircuitVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 照明-照明回路详情VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-照明回路详情VO")
public class LightingCircuitDetailVO extends DtpRwdCircuitVO {

    @Schema(description = "照明回路标签")
    private LightingCircuitTagVO tag;

    @Schema(description = "运行参数")
    private List<InfoCodeValueVO> runningParameters;

    @Schema(description = "设定参数")
    private List<InfoCodeValueVO> settingParameters;

    @Schema(description = "异常事件")
    private List<ExceptionEventBaseInfoVO> abnormalEvents;

    @Schema(description = "关联控制模块")
    private DtpRwdObjectVO relController;

    @Schema(description = "关联低压末端配电柜")
    private DtpRwdObjectVO relDistributionCabinet;

    @Schema(description = "关联空间")
    private DtpRwdObjectVO relSpace;

    @Schema(description = "限制信息点 key信息点,val 信息点值 未交付 返回 NULL, 无数据 返回-1")
    private Map<String, String> restrictInfos;

}
