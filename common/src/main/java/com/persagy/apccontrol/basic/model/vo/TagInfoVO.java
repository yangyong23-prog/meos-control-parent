package com.persagy.apccontrol.basic.model.vo;

import com.persagy.apccontrol.basic.model.enumtype.*;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalCarbonDioxideIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 标签状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/5/23 17:26
 * @since v5.240501.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("标签状态")
public class TagInfoVO {


    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地ID")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "楼层本地ID")
    private String floorLocalId;

    @Schema(description = "楼层本地名称")
    private String floorLocalName;


    @Schema(description = "手自动状态")
    private HandAutomatismEnum manualAutoStatus;

    @Schema(description = "在线离线状态")
    private OnlineStatusEnum onlineStatusEnum;

    @Schema(description = "本地远程")
    private LocalRemoteEnum localRemote;

    @Schema(description = "本地远程")
    private PowerStatusEnum powerStatus;

    @Schema(description = "开关状态")
    private OnOffStateEnum onOffState;

    @Schema(description = "异常状态")
    private List<AbnormalStatusTypeEnum> abnormal;

    @Nullable
    @Schema(description = "品质-温度区间")
    private AirTerminalTemperatureIntervalEnum temperatureInterval;

    @Nullable
    @Schema(description = "品质-CO₂区间")
    private AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval;

    @Nullable
    @Schema(description = "需求状态-温度")
    private AirTerminalDemandStatusEnum temperatureDemandStatus;

    @Nullable
    @Schema(description = "需求状态-CO₂")
    private AirTerminalDemandStatusEnum carbonDioxideDemandStatus;

}
