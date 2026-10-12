package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.client.EnergyBudgetManageServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.service.EnergyBudgetServerService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.EnergyTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/26 11:30
　　 * @description: 能耗预算查询
*/
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EnergyBudgetServerServiceImpl implements EnergyBudgetServerService {

    private final EnergyBudgetManageServerClient energyBudgetManageServerClient;

    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String PROJECT_ID = "projectId";
    private static final String ENERGY_TYPE = "energyType";
    private static final String LOG_TAG = "能耗预算服务";
    @Override
    public List<EnergyDetailResponse> dayEnergyDetail(LocalDateTime startDate, LocalDateTime endDate,EnergyTypeEnum energyType) {
        //从上下文中获取集团编码和项目ID
        String projectId = ContextUtil.getProjectId();
        String groupCode = ContextUtil.getGroupCode();
        JSONObject param = getJsonObject(startDate, endDate,energyType);
        //查春数据
        List<List<EnergyDetailResponse>> resultList = query(Boolean.FALSE, null, projectId, LocaleMessageUtil.getMessage("key_query_energy_budget_dayEnergyDetail"), param,
                () -> energyBudgetManageServerClient.dayEnergyDetail(groupCode, projectId, param.toString()), null);
        return ObjectUtil.isEmpty(resultList)? Lists.newArrayList(): resultList.get(0);
    }

    @Override
    public List<EnergyDetailResponse> crossDayEnergyDetail(LocalDateTime startDate, LocalDateTime endDate, EnergyTypeEnum energyType) {
        //从上下文中获取集团编码和项目ID
        String projectId = ContextUtil.getProjectId();
        String groupCode = ContextUtil.getGroupCode();
        JSONObject param = getJsonObject(startDate, endDate,energyType);
        //查询数据
        List<List<EnergyDetailResponse>> resultList = query(Boolean.FALSE, null, projectId, LocaleMessageUtil.getMessage("key_query_energy_budget_weekEnergyDetail"), param,
                () -> energyBudgetManageServerClient.crossDayEnergyDetail(groupCode, projectId, param.toString()), null);
        return  ObjectUtil.isEmpty(resultList)? Lists.newArrayList(): resultList.get(0);
    }

    @Override
    public List<EnergyItemInfoListResponse> itemInfoList(EnergyTypeEnum energyType) {
        String projectId = ContextUtil.getProjectId();
        String groupCode = ContextUtil.getGroupCode();

        JSONObject param = new JSONObject();
        param.put(PROJECT_ID, ContextUtil.getProjectId());
        //能耗类型 1电，2水
        param.put(ENERGY_TYPE,energyType.getValue());
        List<List<EnergyItemInfoListResponse>> resultList = query(Boolean.FALSE, null, projectId, LocaleMessageUtil.getMessage("key_query_energy_budget_itemInfoList"), param,
                () -> energyBudgetManageServerClient.itemInfoList(groupCode, projectId, param.toString()), null);
        return  ObjectUtil.isEmpty(resultList)? Lists.newArrayList(): resultList.get(0);
    }

    @Override
    public EnergyTopItemInfoListResponse queryTopItem(EnergyTypeEnum energyType) {
        String projectId = ContextUtil.getProjectId();
        String groupCode = ContextUtil.getGroupCode();

        JSONObject param = new JSONObject();
        param.put(PROJECT_ID, ContextUtil.getProjectId());
        //能耗类型 1电，2水
        param.put(ENERGY_TYPE,energyType.getValue());
        List<EnergyTopItemInfoListResponse> resultList = query(Boolean.FALSE, null, projectId, LocaleMessageUtil.getMessage("key_query_energy_budget_itemInfoList"), param,
                () -> energyBudgetManageServerClient.queryTopItem(groupCode, projectId, param.toString()), null);
        return  ObjectUtil.isEmpty(resultList)? null : resultList.get(0);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 18:22
    　　 * @description: 构造公共的请求参数
     */
    @NotNull
    private JSONObject getJsonObject(LocalDateTime startDate, LocalDateTime endDate,EnergyTypeEnum energyType) {
        JSONObject param = new JSONObject();
        param.put(START_DATE, DateUtils.date2Str(startDate,YYYY_MM_DD_HH_MM_SS));
        param.put(END_DATE, DateUtils.date2Str(endDate,YYYY_MM_DD_HH_MM_SS));
        param.put(PROJECT_ID, ContextUtil.getProjectId());
        //能耗类型 1电，2水
        param.put(ENERGY_TYPE, energyType.getValue());
        return param;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }

}
