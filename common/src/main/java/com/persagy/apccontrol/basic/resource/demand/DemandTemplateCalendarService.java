package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarUpdateDaoParam;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 需求模版日历服务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandTemplateCalendarService extends BaseService<DemandTemplateCalendar> {

    /**
     * 查询需求模版日历
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 需求模版日历
     * @since v6.0.0
     */
    DemandTemplateCalendar get(@NonNull String projectId, @NonNull DemandTemplateCalendarQueryDaoParam param);

    /**
     * 查询需求模版日历
     *
     * @param param 查询参数
     * @return 需求模版日历
     * @since 7.260102.0
     */
    default List<DemandTemplateCalendar> list(@NonNull DemandTemplateCalendarQueryDaoParam param) {
        return this.list(ContextUtil.getProjectId(), param);
    }

    /**
     * 查询需求模版日历
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 需求模版日历
     * @since v6.0.0
     */
    List<DemandTemplateCalendar> list(@NonNull String projectId, @NonNull DemandTemplateCalendarQueryDaoParam param);

    /**
     * 新增需求模版日历
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(@NonNull String projectId, @NonNull @Valid DemandTemplateCalendarSaveDaoParam param);

    /**
     * 新增需求模版日历
     *
     * @param projectId 项目ID
     * @param params    新增参数, 为空时直接返回 {@code false}
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(@NonNull String projectId, @Nullable @Valid Collection<DemandTemplateCalendarSaveDaoParam> params);

    /**
     * 编辑需求模版日历
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean edit(@NonNull String projectId, @NonNull @Valid DemandTemplateCalendarUpdateDaoParam param);

    /**
     * 编辑需求模版日历
     *
     * @param projectId 项目ID
     * @param params    编辑参数, 为空时直接返回 {@code false}
     * @return 是否成功
     * @since v6.0.0
     */
    boolean edit(@NonNull String projectId, @Nullable @Valid Collection<DemandTemplateCalendarUpdateDaoParam> params);

}
