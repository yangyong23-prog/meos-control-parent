package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 静态信息点取值逻辑模型
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class StaticInfoCodeTackValueLogicModel extends BaseInfoCodeTackValueLogicModel {

    public static final StaticInfoCodeTackValueLogicModel INSTANCE = new StaticInfoCodeTackValueLogicModel();

    @Override
    public String logTag() {
        return "静态信息点取值逻辑模型";
    }
}
