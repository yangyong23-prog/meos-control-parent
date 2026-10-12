package com.persagy.apccontrol.basic.resource.infocodeconfig.param;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 信息点分组配置参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfoCodeConfigQueryDaoParam
        extends BaseRepositoryQueryParam<InfoCodeConfig>
        implements BaseCheckParamProvider {

    /**
     * 信息点类型
     */
    private Collection<InfoCodeTypeEnum> infoCodeTypes;

    /**
     * 关联分组编码
     */
    private Collection<String> relGroupCodes;

    /**
     * 关联模版类型ID不能为空
     */
    private Collection<Long> relTemplateTypeIds;

}
