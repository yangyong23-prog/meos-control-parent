package com.persagy.apccontrol.basic.service.demand;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceSequenceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandTreeInstanceExternalQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandValueSelectConfigVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceSequenceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandTreeInstanceProjectExternalVO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObjectSpaceDTO;
import com.persagy.framework.common.entity.R;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * 需求管理对外服务
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
public interface DemandExternalService extends LogComponent {

    /**
     * 查询需求实例树
     *
     * @param param 查现参数
     * @return 需求实例树
     * @since 5.241202.0
     */
    R<DemandTreeInstanceProjectExternalVO> treeDemandInstance(
            @NonNull @Valid DemandTreeInstanceExternalQueryParam param);

    /**
     * 查询需求实例
     *
     * @param param 查询参数
     * @return 需求实例
     * @since 5.241102.0
     */
    R<Collection<DemandSpaceExternalVO>> listDemandInstances(@NonNull @Valid DemandSpaceExternalQueryParam param);

    /**
     * 查询空间级别的配置时序数据--5min
     *
     * @param param
     * @return
     */
    TreeMap<LocalDate, List<DemandSpaceSequenceExternalVO>> listSpaceDemandConfigSequenceData(DemandSpaceSequenceExternalQueryParam param) throws Exception;

    /**
     * 获取空间级别的需求配置信息
     *
     * @param currentDate
     * @param airSeason                        当前空调季，为空自动查询
     * @param season                           当前空调工况，为空自动查询
     * @param objectSpaceDTOS                  为空自动查询
     * @param filterSeason                     是否校验空调工况，true-过渡季不返回数据，因为不参与计算
     * @param filterSystemServiceSpaceRelation 是否校验空间与冷站、冷热站、热站的服务关系，true-校验，进行数据过滤
     * @param checkInfoCodeRule                是否检查空间存在孪生温度点位规则数据，仅代表查询，不进行数据上的过滤
     * @param queryBusinessTime                是否查询出该空间的营业时间配置，true-查询
     * @return
     */
    List<DemandConfigAirconditioningValueObjectSpaceDTO> getDemandConfigAirconditioningValueObjectSpaceDTOs(LocalDate currentDate, MidCalendarAirSeasonTypeEnum airSeason,
                                                                                                            MidCalendarAcSeasonTypeEnum season, List<DemandConfigAirconditioningValueObjectSpaceDTO> objectSpaceDTOS,
                                                                                                            boolean filterSeason, boolean filterSystemServiceSpaceRelation, boolean checkInfoCodeRule, boolean queryBusinessTime);
    
    
    /**
     * 查询每日价值选择的配置
     *
     * @param param
     * @return
     */
    public List<DemandValueSelectConfigVO> queryEverydayFixDataList(@NonNull DemandValueSelectConfigQueryParam param);
    
    
}