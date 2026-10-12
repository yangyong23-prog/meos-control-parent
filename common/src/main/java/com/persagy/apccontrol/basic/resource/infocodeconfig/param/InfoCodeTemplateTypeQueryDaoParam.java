package com.persagy.apccontrol.basic.resource.infocodeconfig.param;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Set;

/**
 * 信息点模版类型查询参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTemplateTypeQueryDaoParam
        extends BaseRepositoryQueryParam<InfoCodeTemplateType>
        implements BaseCheckParamProvider {

    /**
     * 模版ID
     */
    private Long templateTypeId;

    /**
     * 模版ID
     */
    private Set<Long> templateTypeIds;

    /**
     * 运行监测类型
     */
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测类型
     */
    private Set<RunningMonitorTypeEnum> runningMonitorTypes;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 运行监测二级类型
     */
    private Collection<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes;

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
     * 对象类型
     */
    private Collection<DtpRwdObjectTypeEnum> objectTypes;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 设备类编码
     */
    private Collection<String> classCodes;

    /**
     * 排除设备类编码
     */
    private Collection<String> excludeClassCodes;

}
