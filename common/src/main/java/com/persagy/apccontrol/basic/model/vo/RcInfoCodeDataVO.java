package com.persagy.apccontrol.basic.model.vo;

import com.persagy.apccontrol.basic.model.entity.RcEqInfoCodeCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author yuyao
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RcInfoCodeDataVO extends RcEqInfoCodeCfg {


    private String val;

}
