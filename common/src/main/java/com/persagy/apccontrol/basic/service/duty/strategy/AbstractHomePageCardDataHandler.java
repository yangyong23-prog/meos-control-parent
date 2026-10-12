package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.map.MapUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateTypeVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ChildCardDataVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.adapter.HomePageCardIotDataAdapter;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.lang.NonNull;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 值班首页查询策略
 */
public abstract class AbstractHomePageCardDataHandler<T extends BasicsCardVO> implements HomePageCardDataHandler<T> {

    @Resource
    DemandCalendarQueryService demandCalendarQueryService;
    @Resource
    private AbstractCommonServer abstractCommonServer;
    @Resource
    private HomePageCardIotDataAdapter homePageCardIotDataAdapter;

    /**
     * 构建大卡片的 异常事件/离线/控反不一致数量
     *
     * @param dutyHomePageGroup 聚合信息
     */
    protected CardTagVO buildCardTagVO(DutyHomePageGroupDTO dutyHomePageGroup, Set<String> objIds) {
        CardTagVO vo = new CardTagVO();
        if (dutyHomePageGroup == null) {
            return vo;
        }
        //填充大卡片信息
        vo.setErrorEventTotal(getExceptionEventCountByObjIds(objIds));
        vo.setOfflineTotal(dutyHomePageGroup.getOffLineTotal());
        vo.setOrderFailAlarmTotal(dutyHomePageGroup.getAlarmTotal());
        return vo;
    }

    /**
     * 调用聚合接口获取统计数据
     *
     * @param map key 分组名称 val分组查询对象信息
     * @return 聚合统计结果
     */
    protected DutyHomePageGroupDTO queryHomePageCardGroupIotData(Map<String, ObjectIdAndForceCodeQuery> map) {
        return homePageCardIotDataAdapter.queryHomePageCardGroupIotData(map, getRunningMonitorType());
    }


    /**
     * 需求模版查询
     * 工作日/店庆日
     *
     * @return templateType.getValue 和模版的名称
     */
    protected Map<String, DemandTemplateVO> getDemandTemplateType(@NonNull List<DemandTemplateTypeEnum> templateTypeList) {
        Map<String, DemandTemplateVO> map = new HashMap<>();
        templateTypeList.forEach(templateType -> {

            if (templateType == null) {
                return;
            }

            DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(templateType, DateUtils.now().toLocalDate());
            if (dateSetting != null && dateSetting.getRelTemplate() != null) {
                map.put(templateType.getValue(), dateSetting.getRelTemplate());
            }
        });
        return map;
    }

    protected void setCardDateDemandTemplateType(CardDateVO<ChildCardDataVO> vo, Map<String, DemandTemplateVO> nameMap) {

        if (vo == null || CollectionUtils.isEmpty(vo.getChildCardList())) {
            return;
        }
        vo.getChildCardList().forEach(card -> {
            if (card == null) {
                return;
            }
            card.setDemandTemplate(nameMap.get(card.getNameCode()));
        });
    }


    /**
     * 小卡片数据查询
     * 聚合接口统计干预/运行数量
     *
     * @param subStat key 小卡片名称 val 卡片统计信息
     */
    protected List<ChildCardDataVO> buildChildCardDateVO(Map<String, DutyHomePageDTO> subStat) {
        List<ChildCardDataVO> list = new ArrayList<>();
        if (MapUtil.isEmpty(subStat)) {
            return new ArrayList<>();
        }
        subStat.forEach((key, value) -> {
            if (value == null || value.getTotal() == 0) {
                return;
            }
            ChildCardDataVO vo = new ChildCardDataVO();
            vo.setName(key);
            vo.setEquipmentTotal(value.getTotal());
            vo.setRunningTotal(value.getRunTotal());
            vo.setInterveneTotal(value.getManualTotal());
            vo.setLessOpenTotal(value.getLessTotal());
            vo.setMoreOpenTotal(value.getMoreTotal());
            list.add(vo);
        });
        return list;
    }

    /**
     * 获取过滤后的对象 id
     */
    Set<String> filtrationdtpRwdCircuitList(List<DtpRwdCircuitDTO> dtpRwdCircuitList) {
        //对象过滤
        filtrationMonitoringSurveillanceStatus(dtpRwdCircuitList);
        if (CollectionUtils.isEmpty(dtpRwdCircuitList)) {
            return new HashSet<>();
        }
        Set<String> publicLightingObjIds = new HashSet<>();
        dtpRwdCircuitList.stream().map(DtpRwdCircuitDTO::getId).forEach(publicLightingObjIds::add);
        return publicLightingObjIds;
    }

    /**
     * 对DtpRwdObjectDTO进行信息点过滤
     * 过滤 monitoringSurveillanceStatus = 2或3 的数据
     *
     * @param list 需要过滤的参数
     */
    public void filtrationMonitoringSurveillanceStatus(List<? extends DtpRwdObjectDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //遍历list删除里面 undefinedProperties的 key 未 monitoringSurveillanceStatus 不含有2或3的数据
        list.removeIf(item ->
                MapUtil.isNotEmpty(item.getUndefinedProperties()) && "2".equals(item.getUndefinedProperties().get(CommonConst.MONITORING_SURVEILLANCE_STATUS)) &&
                        !"3".equals(item.getUndefinedProperties().get(CommonConst.MONITORING_SURVEILLANCE_STATUS)));
    }

    /**
     * 查询异常事件数量
     *
     * @param objIds 对象 ID
     */
    int getExceptionEventCountByObjIds(Collection<String> objIds) {
        int count = 0;
        if (CollectionUtils.isEmpty(objIds)) {
            return count;
        }
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s =
                getRunningMonitorType().getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2();
        Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> highRisk =
                x -> x.stream().filter(item -> item.getImportanceDegree() == ImportanceDegreeEnum.HIGH).collect(Collectors.toList());
        List<ExceptionEventBaseInfoVO> riskControlTargetConditions = abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(objIds, riskMeasureAimLevel2s);
        if (CollectionUtils.isNotEmpty(riskControlTargetConditions)) {
            riskControlTargetConditions = highRisk.apply(riskControlTargetConditions);
        }
        return riskControlTargetConditions.size();
    }

    /**
     * 获取 code 和 name 的拼接
     * 用于给前端返回卡片名称的时候 返回name和code
     * 对应代码位置 com/persagy/apccontrol/basic/model/vo/duty/ChildCardDataVO.java:44
     *
     * @param code code
     * @param name name
     * @return code 和 name 的拼接
     */
    public String getCodeAndeName(String code, String name) {
        return code + CommonConst.COLON + name;
    }

    /**
     * 查询需求日历模版类型编码
     *
     * @param demandType
     * @return
     */
    public Set<String> queryDemandTemplateType(@NonNull DemandTypeEnum demandType) {
        //需求日历模版类型
        List<DemandTemplateTypeVO> demandTemplateType = demandCalendarQueryService.listTemplateType(
                new DemandTemplateTypeQueryParam(ContextUtil.getProjectId(), demandType));
        //获取 code
        Set<String> set = new HashSet<>();
        demandTemplateType.forEach(
                demandTemplate -> demandTemplate.getTemplateTypes().forEach(
                        templateType -> set.add(templateType.getKey().toString())));

        return set;
    }

}
