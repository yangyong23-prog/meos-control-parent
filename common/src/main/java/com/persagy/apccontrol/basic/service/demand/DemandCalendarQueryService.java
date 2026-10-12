package com.persagy.apccontrol.basic.service.demand;

import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.DemandInstantiationDateTagQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateDateSettingQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateTypeVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.service.BizComponent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 需求管理-需求日历模版服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getTemplate</b> - 查询模版</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public interface DemandCalendarQueryService extends BizComponent<DemandCalendarQueryService> {

    // ------------------------- 查询相关 -------------------------

    /**
     * 根据日期判断是否应用模版
     *
     * @param templateId 模版ID
     * @param date       应用日期
     * @return 判断应用日期是否应用模版ID
     * @since 5.240602.0
     */
    boolean isApplyTemplateByDate(@NonNull Long templateId, @NonNull LocalDate date);

    /**
     * 查询模版
     *
     * <p>当模版不存在时进行初始化
     *
     * @param param 查询参数
     * @return 模版
     * @since v6.0.0
     */
    DemandTemplateVO getTemplate(@NonNull @Valid DemandTemplateQueryParam param);

    /**
     * 根据日期查询日期配置
     *
     * @param templateType 模版类型
     * @param date         日期
     * @return 模版日期配置
     * @since v6.0.0
     */
    @Nullable
    DemandTemplateDateSettingVO getDateSetting(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date);

    /**
     * 查询模版类型列表
     *
     * @param param 查询参数
     * @return 模版类型列表
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandTemplateTypeVO> listTemplateType(@NonNull @Valid DemandTemplateTypeQueryParam param);

    /**
     * 查询模版列表
     *
     * <p>当模版不存在时进行初始化
     *
     * @param param 查询参数
     * @return 模版列表
     * @since v6.0.0
     */
    List<DemandTemplateVO> listTemplate(@NonNull @Valid DemandTemplateBatchQueryParam param);

    /**
     * 查询日期实例化标签
     *
     * @param param 查询参数
     * @return 日期实例化标签
     * @since 7.250101.0
     */
    List<DemandInstantiationDateTagVO> listInstantiationDateTag(
            @NonNull @Valid DemandInstantiationDateTagQueryParam param);

    /**
     * 查询模版日期配置
     *
     * @param param 查询参数
     * @return 模版日期配置
     * @since v6.0.0
     */
    List<DemandTemplateDateSettingVO> listDateSetting(@NonNull @Valid DemandTemplateDateSettingQueryParam param);

}
