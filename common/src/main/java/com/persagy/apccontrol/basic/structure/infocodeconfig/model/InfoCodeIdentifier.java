package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 信息点标识符
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeIdentifier implements Identifier, BaseCheckParamProvider {

    /**
     * 运行监测类型
     */
    @NonNull
    private final RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    @Nullable
    private final RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版类型
     */
    @NonNull
    private final InfoCodeTemplateTypeEnum templateType;

    /**
     * 对象
     */
    @NonNull
    private final DtpRwdObjectDTO object;

    /**
     * 信息点类型
     */
    @NonNull
    private final InfoCodeTypeEnum infoCodeType;

    /**
     * 信息点编码
     */
    @NonNull
    private final String infoCode;

    /**
     * 分组编码
     */
    @NonNull
    private final String groupCode;

}
