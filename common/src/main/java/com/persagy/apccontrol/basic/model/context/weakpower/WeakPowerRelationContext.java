package com.persagy.apccontrol.basic.model.context.weakpower;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.client.RequestServerException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 弱电关系DTO
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "弱电关系DTO")
public class WeakPowerRelationContext {

    private final DtpRwdConvert convert = SpringUtil.getBean(DtpRwdConvert.class);

    @Schema(description = "运行监测二级类型")
    @NotNull(message = "{key_4a1af0119a4f4fdc8f6cfe10e1ea451e}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "设备类编码, 顶节点为空, 子节点必传")
    private DtpRwdClassCodeEnum classCode;

    @Schema(description = "对象ID, 必传")
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private Set<String> objectIds;

    @Schema(description = "查询参数")
    @NotNull(message = "{key_32dce10bbb084d04892ee03219e0a55f}")
    private DtpRwdObjectQueryParam queryParam;

    /**
     * 填充对象查询参数
     *
     * @return 对象查询参数
     * @since 7.250301.0
     */
    @Nullable
    public DtpRwdObjectQueryParam fillObjectQueryParam() {
        switch (this.runningMonitorSecondType) {
            case INTELLIGENT_BUILDING_AUTOMATION:
                return intelligentBuildingAutomation();
            case ENERGY_MANAGEMENT:
                return energyManagement();
            case INDOOR_ENVIRONMENT_MONITORING:
                return indoorEnvironmentMonitoring();
            case LIGHTING_MONITORING:
                return lightingMonitoring();
            default:
                throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_0e89e855675947bc96309701a40352b8"));
        }
    }

    /**
     * 照明监测系统
     *
     * @return 对象查询参数
     * @since 7.250301.0
     */
    @Nullable
    private DtpRwdObjectQueryParam lightingMonitoring() {
        if (this.runningMonitorSecondType != RunningMonitorSecondTypeEnum.LIGHTING_MONITORING) {
            throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82649c5ecce6422fb48747eebd630513"));
        }
        DtpRwdObjectQueryParam queryParam = convert.copy(this.queryParam);
        switch (this.classCode) {
            case WELM:
                // 系统 -> 服务器
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WEICSV
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(objectIds));
            case WEICSV:
                // 服务器 ->网关
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WENCGW
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WENCGW:
                // 网关 -> DDC直接数字控制器/数据采集器
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WEFCDD, DtpRwdClassCodeEnum.WEFCDC
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDD:
                // DDC直接数字控制器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.LIGHTING_CONTROLLER, DtpRwdClassCodeEnum.WESSID, DtpRwdClassCodeEnum.WESSIL
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDC:
                // 数据采集器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.LIGHTING_CONTROLLER, DtpRwdClassCodeEnum.WESSID, DtpRwdClassCodeEnum.WESSIL
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            default:
                return null;
        }
    }

    /**
     * 室内环境监控系统
     *
     * @return 对象查询参数
     * @since 7.250301.0
     */
    @Nullable
    private DtpRwdObjectQueryParam indoorEnvironmentMonitoring() {
        if (this.runningMonitorSecondType != RunningMonitorSecondTypeEnum.INDOOR_ENVIRONMENT_MONITORING) {
            throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82649c5ecce6422fb48747eebd630513"));
        }
        DtpRwdObjectQueryParam queryParam = convert.copy(this.queryParam);
        switch (this.classCode) {
            case WEIE:
                // 系统 -> 服务器
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WEICSV
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(objectIds));
            case WEICSV:
                // 服务器 ->网关
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WENCGW
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WENCGW:
                // 网关 -> DDC直接数字控制器/数据采集器
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WEFCDD, DtpRwdClassCodeEnum.WEFCDC
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDD:
                // DDC直接数字控制器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WESSCB, DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCT,
                        DtpRwdClassCodeEnum.WESSIL, DtpRwdClassCodeEnum.WESSRH, DtpRwdClassCodeEnum.WESSTH,
                        DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSTP
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDC:
                // 数据采集器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WESSCB, DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCT,
                        DtpRwdClassCodeEnum.WESSIL, DtpRwdClassCodeEnum.WESSRH, DtpRwdClassCodeEnum.WESSTH,
                        DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSTP
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            default:
                return null;
        }
    }

    /**
     * 能源管理系统
     *
     * @return 对象查询参数
     * @since 7.250301.0
     */
    @Nullable
    private DtpRwdObjectQueryParam energyManagement() {
        if (this.runningMonitorSecondType != RunningMonitorSecondTypeEnum.ENERGY_MANAGEMENT) {
            throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82649c5ecce6422fb48747eebd630513"));
        }
        DtpRwdObjectQueryParam queryParam = convert.copy(this.queryParam);
        switch (this.classCode) {
            case WEEM:
                // 系统 -> 服务器
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WEICSV
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(objectIds));
            case WEICSV:
                // 服务器 ->网关
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WENCGW
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WENCGW:
                // 网关 -> DDC直接数字控制器/数据采集器
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WEFCDD, DtpRwdClassCodeEnum.WEFCDC
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDD:
                // DDC直接数字控制器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WESSEE, DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSWM
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDC:
                // 数据采集器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WESSEE, DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSWM
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            default:
                return null;
        }
    }

    /**
     * 楼宇智能化系统
     *
     * @return 楼宇智能化系统
     * @since 7.250301.0
     */
    @Nullable
    private DtpRwdObjectQueryParam intelligentBuildingAutomation() {
        if (this.runningMonitorSecondType != RunningMonitorSecondTypeEnum.INTELLIGENT_BUILDING_AUTOMATION) {
            throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_82649c5ecce6422fb48747eebd630513"));
        }
        DtpRwdObjectQueryParam queryParam = convert.copy(this.queryParam);
        switch (this.classCode) {
            case WEBA:
                // 系统 -> 服务器
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WEICSV
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(objectIds));
            case WEICSV:
                // 服务器 ->网关
                return queryParam.setClassCodeEnum(
                        DtpRwdClassCodeEnum.WENCGW
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WENCGW:
                // 网关 -> DDC直接数字控制器/PLC可编程逻辑控制器/数据采集器/各类传感器
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                                DtpRwdClassCodeEnum.WEFCDD, DtpRwdClassCodeEnum.WEFCPL, DtpRwdClassCodeEnum.WEFCDC,
                                DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSDP,
                                DtpRwdClassCodeEnum.WESSFL, DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSIL,
                                DtpRwdClassCodeEnum.WESSIP, DtpRwdClassCodeEnum.WESSPE, DtpRwdClassCodeEnum.WESSPF,
                                DtpRwdClassCodeEnum.WESSRH, DtpRwdClassCodeEnum.WESSSD, DtpRwdClassCodeEnum.WESSSP,
                                DtpRwdClassCodeEnum.WESSTH, DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSTP,
                                DtpRwdClassCodeEnum.WESSVD, DtpRwdClassCodeEnum.WESSWL, DtpRwdClassCodeEnum.WESSWV
                        )
                ).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDD:
                // DDC直接数字控制器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.ACATAH, DtpRwdClassCodeEnum.ACATFC, DtpRwdClassCodeEnum.ACATFU,
                        DtpRwdClassCodeEnum.ACATVA, DtpRwdClassCodeEnum.ACCCAC, DtpRwdClassCodeEnum.ACCCAH,
                        DtpRwdClassCodeEnum.ACCCCC, DtpRwdClassCodeEnum.ACCCCP, DtpRwdClassCodeEnum.ACCCGH,
                        DtpRwdClassCodeEnum.ACCCGP, DtpRwdClassCodeEnum.ACCCOF, DtpRwdClassCodeEnum.ACCCOP,
                        DtpRwdClassCodeEnum.ACCCOT, DtpRwdClassCodeEnum.ACCCWH, DtpRwdClassCodeEnum.ACCHCH,
                        DtpRwdClassCodeEnum.ACCHHP, DtpRwdClassCodeEnum.ACGEFP, DtpRwdClassCodeEnum.FFSCAF,
                        DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSDP,
                        DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSIL,
                        DtpRwdClassCodeEnum.WESSIP, DtpRwdClassCodeEnum.WESSPE, DtpRwdClassCodeEnum.WESSPF,
                        DtpRwdClassCodeEnum.WESSRH, DtpRwdClassCodeEnum.WESSSD, DtpRwdClassCodeEnum.WESSSP,
                        DtpRwdClassCodeEnum.WESSTH, DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSTP,
                        DtpRwdClassCodeEnum.WESSVD, DtpRwdClassCodeEnum.WESSWL, DtpRwdClassCodeEnum.WESSWV
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCPL:
                // PLC可编程逻辑控制器 -> 各种传感器/被控机电设备
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.SETDRC, DtpRwdClassCodeEnum.TPASAC, DtpRwdClassCodeEnum.WSDHHP,
                        DtpRwdClassCodeEnum.WSDWPP, DtpRwdClassCodeEnum.WSRWPP
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            case WEFCDC:
                // 数据采集器 -> 各种传感器
                return queryParam.setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSDP,
                        DtpRwdClassCodeEnum.WESSFL, DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSIL,
                        DtpRwdClassCodeEnum.WESSIP, DtpRwdClassCodeEnum.WESSPE, DtpRwdClassCodeEnum.WESSPF,
                        DtpRwdClassCodeEnum.WESSRH, DtpRwdClassCodeEnum.WESSSD, DtpRwdClassCodeEnum.WESSSP,
                        DtpRwdClassCodeEnum.WESSTH, DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSTP,
                        DtpRwdClassCodeEnum.WESSVD, DtpRwdClassCodeEnum.WESSWL, DtpRwdClassCodeEnum.WESSWV
                )).setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.WE_NETWORK).setObjFrom(objectIds));
            default:
                return null;
        }
    }
}
