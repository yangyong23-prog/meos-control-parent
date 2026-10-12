package com.persagy.apccontrol.basic.resource.demand;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustom;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustomTimeGroup;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 需求管理-空调需求-定制功能配置服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/20 14:40
 */
public interface DemandConfigAirconditioningCustomService extends BaseService<DemandConfigAirconditioningCustom>, LogComponent {

    /**
     * 获取当前项目的价值选择类型配置
     *
     * @param projectId
     * @param localDate 不传，默认为当天
     * @return
     */
    BasicsConfigTypeEnum getValueSelectType(String projectId, LocalDate localDate);

    /**
     * 查询自定义配置
     *
     * @param param 查询参数
     * @return 自定义配置
     */
    DemandConfigAirconditioningCustomDTO getCustomConfig(BaseDemandTemplateParam param);

    /**
     * 获取当前项目对应价值选择类型的区间配置数据
     *
     * @param templateId
     * @param valueSelectType
     * @return
     */
    List<ValueSelectConfigDto> getValueSelectRangeConfig(Long templateId, BasicsConfigTypeEnum valueSelectType);
    
    /**
     * 查询每日的价值选择配置
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return {@link List }<{@link JSONObject }>
     */
    List<DemandConfigAirconditioningCustomTimeGroup> queryEverydayFixDataList(@NonNull LocalDate startDate,
                                                                              @NonNull LocalDate endDate
    );

    /**
     * 保存或更新当前项目对应价值选择类型的区间配置数据
     *
     * @param templateId
     * @param valueSelectType
     * @param valueSelectRange
     * @return
     */
    boolean saveOrUpdateValueSelectRange(Long templateId, BasicsConfigTypeEnum valueSelectType, List<ValueSelectConfigDto> valueSelectRange);

    /**
     * 获取当前项目模板下的配置项数据
     *
     * @param projectId
     * @param relTemplateId
     * @param date
     * @return
     */
    DemandConfigAirconditioningCustomDTO getConfigData(String projectId, String relTemplateId, LocalDate date);

    /**
     * 获取当前空调工况运行标准配置，并填充壳配置的默认数据
     *
     * @param projectId
     * @param relTemplateId
     * @param localDate
     * @return
     */
    DemandConfigAirconditioningWorkConditionValueObject getShellConfigData(String projectId, Long relTemplateId, LocalDate localDate);

    /**
     * 保存或更新当前项目模板下的配置数据
     *
     * @param projectId
     * @param relTemplateId
     * @param valueSelectType
     * @param workCondition
     * @return
     */
    boolean saveOrUpdateConfigData(String projectId, Long relTemplateId, BasicsConfigTypeEnum valueSelectType, DemandConfigAirconditioningWorkConditionValueObject workCondition);

    /**
     * 清除当前
     *
     * @param projectId
     * @param relTemplateId
     * @return
     */
    boolean clearConfigData(String projectId, String relTemplateId);

    Boolean compatibleValueSelectConfig();
}
