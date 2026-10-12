package com.persagy.apccontrol.basic.resource.infocodeconfig.param;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * 信息点模版类型删除参数
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeTemplateTypeRemoveDaoParam implements BaseCheckParamProvider {

    /**
     * 运行监测类型
     */
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    @Nullable
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版编码
     */
    private InfoCodeTemplateTypeEnum templateType;

    /**
     * 模版编码
     */
    private Collection<InfoCodeTemplateTypeEnum> templateTypes;

    /**
     * 对象类型
     */
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 设备类编码
     */
    private Collection<String> classCodes;

    /**
     * 信息点过滤条件
     */
    private String infoCodeFilter;

}
