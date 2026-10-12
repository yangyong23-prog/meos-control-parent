package com.persagy.apccontrol.basic.structure.intervention.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 解除干预指令
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@AllArgsConstructor
public class ReleaseInterventionCommand {

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 信息点编码
     *
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#CONTROL_DEMAND
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#ORD_LIGHT_CONTROL_DEMAND
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#AD_LIGHT_CONTROL_DEMAND
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#SIGN_LIGHT_CONTROL_DEMAND
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#DEC_LIGHT_CONTROL_DEMAND
     * @see com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum#CONST_LIGHT_CONTROL_DEMAND
     */
    private String infoCode;

}
