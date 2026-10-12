package com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.param.BaseAggregateInitParam;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息点配置-聚合根基础初始化参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BaseInfoCodeInitParam extends BaseAggregateInitParam implements BaseCheckParamProvider {

    /**
     * 分页参数, 为空时不分页
     */
    private @Nullable DefaultListPage<List<InfoCodeAggregate>> page;

    /**
     * 运行监测类型
     */
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */

    private @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版类型ID
     *
     * @see #checkAndSet()
     */
    private Set<Long> templateTypeIds;

    /**
     * 模版类型
     *
     * @see #checkAndSet()
     */
    private Collection<InfoCodeTemplateTypeEnum> templateTypes;

    /**
     * 模版类型模型
     */
    private Collection<InfoCodeTemplateTypeModel> templateTypeModels;

    /**
     * 是否强制模版, 仅在传入 [templateTypeModels] 且仅存在一个情况下使用
     */
    private Boolean forceMathTemplate;

    /**
     * 是否过滤自定义分组信息点配置, 默认过滤
     */
    private @Nullable Boolean filterCustomGroupInfoCodeConfig;

    /**
     * 分组编码
     */
    private @Nullable Collection<String> groupCodes;

    /**
     * 分组过滤参数, 用于替换上面两个参数
     */
    private @Nullable Collection<InfoCodeConfigQueryConfigValueObject> filterGroups;

    /**
     * 分组排除参数, 用于替换上面两个参数
     */
    private @Nullable Collection<InfoCodeConfigQueryConfigValueObject> excludeGroups;

    /**
     * 信息点类型
     */
    private @Nullable Collection<InfoCodeTypeEnum> infoCodeTypes;

    /**
     * 是否查询IOT信息点规则, 默认查询
     */
    private Boolean queryIotInfoCodeRule;

    /**
     * 是否不过滤IOT规则, 默认过滤
     */
    private @Nullable Boolean notFilterIotInfoCodeRule;

    /**
     * 是否过滤结果对象都不存在规则的IOT信息点, 当 [notFilterIotInfoCodeRule] 不为true时生效
     */
    private @Nullable Boolean filterBothNotExistIotInfoCodeRule;

    /**
     * 是否填充信息点定义
     */
    private @Nullable Boolean fillInfoCodeDefineModel;

    /**
     * 是否填充信息点数据
     */
    private @Nullable Boolean fillInfoCodeDataModel;

    /**
     * 占位符参数, 用于填充信息点数据时传递额外参数
     */
    private @Nullable Map<String, Object> placeholders;

    @Override
    public void checkAndSet() {
        if (CollectionUtils.isEmpty(this.templateTypeIds)
                && CollectionUtils.isEmpty(this.templateTypes)
                && CollectionUtils.isEmpty(this.templateTypeModels)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_00cd7f9ebc314e1f9de0cb41f8e37a54"));
        }
    }
}
