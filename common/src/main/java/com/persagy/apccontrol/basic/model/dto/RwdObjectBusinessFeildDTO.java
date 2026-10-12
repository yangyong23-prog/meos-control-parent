package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 中台物理世界对象，在本方所使用的静态属性字段，本实体类目前仅用于日志记录
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/5 16:36
 */
@Getter
@Setter
@ToString
public class RwdObjectBusinessFeildDTO {

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "对象本地编码")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "风机定位")
    private String fanPos;

    @Schema(description = "功能类型")
    private String funcType;

    @Schema(description = "安装类型")
    private String installType;

    @Schema(description = "摄像头类型")
    private String cameraType;

    @Schema(description = "系统ID，或者模块编码")
    private String systemId;

    @Schema(description = "系统类编码，或者模块编码")
    private DtpRwdSystemClassCodeEnum systemType;

    public RwdObjectBusinessFeildDTO() {

    }

    public RwdObjectBusinessFeildDTO(String objectId, String localId, String localName, String classCode, String objType) {
        this.objectId = objectId;
        this.localId = localId;
        this.localName = localName;
        this.classCode = classCode;
        this.objType = objType;
    }

    /**
     * 复制当前对象所属的模块信息
     *
     * @return
     */
    public void wirteObjectClassBelongModelType() {
        if (LocalCacheConstant.AIR_TERMINAL_OBJECT_CLASS.contains(classCode)) {
            if (enableAirTerminalInstallType()) {
                this.systemId = DtpRwdSystemClassCodeEnum.AIR_TERMINAL.value();
                this.systemType = DtpRwdSystemClassCodeEnum.AIR_TERMINAL;
            }
        }
        if (LocalCacheConstant.LIGHT_MONITORING_OBJECT_CLASS.contains(classCode)) {
            this.systemId = DtpRwdSystemClassCodeEnum.LIGHT_MONITORING.value();
            this.systemType = DtpRwdSystemClassCodeEnum.LIGHT_MONITORING;
        }
        if (LocalCacheConstant.SECURITY_OBJECT_CLASS.contains(classCode)) {
            if (enableSecurityCameraType()) {
                this.systemId = DtpRwdSystemClassCodeEnum.SECURITY.value();
                this.systemType = DtpRwdSystemClassCodeEnum.SECURITY;
            }
        }
        if (LocalCacheConstant.VENTILATION_OBJECT_CLASS.contains(classCode)) {
            if (enableVentilationFunType()) {
                this.systemId = DtpRwdSystemClassCodeEnum.VENTILATION.value();
                this.systemType = DtpRwdSystemClassCodeEnum.VENTILATION;
            }
        }
        if (LocalCacheConstant.KITCHEN_RANGE_HOOD_OBJECT_CLASS.contains(classCode)) {
            if (enableKitchenRangeHoodFunType()) {
                this.systemId = DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD.value();
                this.systemType = DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD;
            }
        }
    }

    /**
     * 摄像头仅取 类型为 1  2  3 的
     *
     * @return
     */
    public boolean enableSecurityCameraType() {
        if ("SPVSCM".equals(classCode) && !("1".equals(cameraType)) || "2".equals(cameraType) || "3".equals(cameraType)) {
            return false;
        }
        return true;
    }

    /**
     * 空调机组仅取 类型为 1 2 的
     *
     * @return
     */
    public boolean enableAirTerminalInstallType() {
        if ("ACATAH".equals(classCode) && !("1".equals(installType) || "2".equals(installType))) {
            return false;
        }
        return true;
    }

    /**
     * 通风下，通风风机仅取 类型为 1 2
     *
     * @return
     */
    public boolean enableVentilationFunType() {
        if ("ACVTSF".equals(classCode) && !("1".equals(funcType) || "2".equals(funcType))) {
            return false;
        }
        return true;
    }

    /**
     * 厨房排油烟下，通风风机仅取 类型为 5 的
     *
     * @return
     */
    public boolean enableKitchenRangeHoodFunType() {
        if ("ACVTSF".equals(classCode) && !"5".equals(funcType)) {
            return false;
        }
        return true;
    }

}
