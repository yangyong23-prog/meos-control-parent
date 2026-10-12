package com.persagy.apccontrol.common.service.demand;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.param.demand.template.*;
import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTagEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 需求管理-需求日历模版服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getTemplate</b> - 查询模版</li>
 *     <li><b>getDateSetting</b> - 根据日期查询日期配置</li>
 *     <li><b>listTemplate</b> - 查询模版列表</li>
 *     <li><b>listDateSetting</b> - 查询模版日期配置</li>
 *     <li><b>editTemplate</b> - 编辑模版</li>
 *     <li><b>clearCalendar</b> - 清除日历配置</li>
 *     <li><b>applyTemplateByDate</b> - 根据日期应用模版</li>
 *     <li><b>checkTemplateName</b> - 检查模版名称</li>
 *     <li><b>checkExistsDateSettingInFuture</b> - 检查未来日期是否存在配置</li>
 *     <li><b>initTemplate</b> - 初始化模版</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandCalendarService extends BizComponent<DemandCalendarService> {

    /**
     * 初始化模版标签
     */
    List<String> INITIALIZE_TAGS = Lists.newArrayList(
            DemandTemplateTagEnum.WORKDAY.value(), DemandTemplateTagEnum.DAYOFF.value(),
            DemandTemplateTagEnum.HOLIDAY.value(), DemandTemplateTagEnum.ANNIVERSARY.value());

    /**
     * 默认启用标签
     */
    List<String> DEFAULT_ENABLED_TAG = Lists.newArrayList(DemandTemplateTagEnum.WORKDAY.value());

    // ------------------------- 编辑相关 -------------------------

    /**
     * 编辑模版
     *
     * @param param 编辑参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean editTemplate(@NonNull @Valid DemandTemplateUpdateParam param);

    // ------------------------- 删除相关 -------------------------

    /**
     * 删除模版
     *
     * @param param 删除参数
     * @return 是否成功
     * @since 7.250301.0
     */
    boolean delTemplate(@NonNull @Valid DemandTemplateRemoveParam param);

    // ------------------------- 操作相关 -------------------------

    /**
     * 清除日历配置
     *
     * @param param 清除参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean clearCalendar(@NonNull @Valid DemandTemplateClearCalendarParam param);

    /**
     * 根据日期应用模版
     *
     * @param param 应用参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean applyTemplateByDate(@NonNull @Valid DemandTemplateApplyByDateParam param);

    /**
     * 根据模版重置需求日历
     *
     * @param projectId              项目ID
     * @param templateIds            模版ID
     * @param dates                  待清除的日期, 为空时则清除所有未来日期
     * @param resetCustomizedSetting 是否重置自定义配置, 为 {@code false} 时则仅更新系统配置
     * @since v6.0.0
     */
    void resetTemplateCalendarByTemplate(
            @NonNull String projectId,
            @Nullable Collection<Long> templateIds,
            @Nullable Set<LocalDate> dates,
            boolean resetCustomizedSetting);

    /**
     * 检查模版名称
     *
     * @param param 检查参数
     * @return 是否存在
     * @since v6.0.0
     */
    KeyValueVO checkTemplateName(@NonNull @Valid DemandTemplateNameCheckParam param);

    /**
     * 检查未来日期是否存在配置
     *
     * @param param 检查参数
     * @return 是否存在
     * @since v6.0.0
     */
    KeyValueVO checkExistsDateSettingInFuture(@NonNull @Valid DemandTemplateExistsDateSettingInFutureCheckParam param);

    /**
     * 初始化模版
     *
     * @param projectId 项目ID
     * @return 已初始化模版标签
     * @since v6.0.0
     */
    Map<DemandTemplateTypeEnum, List<String>> initTemplate(@NonNull String projectId);

}
