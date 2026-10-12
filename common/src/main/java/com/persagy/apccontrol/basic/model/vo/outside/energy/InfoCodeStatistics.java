
package com.persagy.apccontrol.basic.model.vo.outside.energy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigGroupInformationVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectVO;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @Author 薛涛
 * @Description
 * @Date 15:59 2024/9/3
 * @Param
 * @return
 **/
@Data
public class InfoCodeStatistics {

    private String infoCode;

    private String infoCodeName;

    /**
     * 总数
     */
    private Integer infoCodeObjectCount;

    /**
     * 枚举值对应的数量
     */
    private List<DataSourceStatistic> dataSource;

    private Map<String, DataSourceStatistic> dataSourceMap;


    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.outside.energy.InfoCodeStatistics>
     * @Author 薛涛
     * @Description 统计具体信息点的数量
     * @Date 20:39 2024/9/13
     * @Param [infoCodeObjectVOs]
     **/
    public static List<InfoCodeStatistics> of(List<InfoCodeObjectVO> infoCodeObjectVOs) {
        Map<String, InfoCodeStatistics> infoCodeStatisticsMap = new HashMap<>(16);
        InfoCodeStatistics alarmEventDetailInfoCodeStatistics = null;
        InfoCodeStatistics armingStatusStatistics = null;
        for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOs) {
            List<InfoCodeConfigGroupInformationVO> infoCodeConfigGroupVOs = infoCodeObjectVO.getInfoCodeConfigGroupVOs();
            for (InfoCodeConfigGroupInformationVO infoCodeConfigGroupVO : infoCodeConfigGroupVOs) {
                List<InfoCodeDataSourceDefineModel> dataSource = infoCodeConfigGroupVO.getDataSource();
                String infoCode = infoCodeConfigGroupVO.getInfoCode();
                if (infoCode.equals("alarmeventdetail")) {
                    alarmEventDetailInfoCodeStatistics = ofAlarmEventDetail(alarmEventDetailInfoCodeStatistics, infoCodeConfigGroupVO);
                    continue;
                }
                if (infoCode.equals("armingStatus")) {
                    armingStatusStatistics = ofArmingStatus(armingStatusStatistics, infoCodeObjectVO);
                }
                if (CollectionUtils.isEmpty(dataSource)) {
                    continue;
                }

                InfoCodeStatistics infoCodeStatistics = infoCodeStatisticsMap.get(infoCode);
                if (Objects.isNull(infoCodeStatistics)) {
                    infoCodeStatistics = new InfoCodeStatistics();
                    infoCodeStatistics.setInfoCode(infoCode);
                    infoCodeStatistics.setInfoCodeName(infoCodeConfigGroupVO.getInfoCodeName());
                    infoCodeStatistics.setInfoCodeObjectCount(1);
                    Object dataValue = infoCodeConfigGroupVO.getDataValue();
                    Map<String, DataSourceStatistic> dataSourceMap = new HashMap<>(16);

                    if (Objects.isNull(dataValue)) {
                        DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                        dataSourceStatistic.setCode("nullCode");
                        dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                        dataSourceStatistic.setCodeCount(1);
                        dataSourceMap.put("nullCode", dataSourceStatistic);
                    }
                    for (InfoCodeDataSourceDefineModel infoCodeDataSourceDefineModel : dataSource) {
                        DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                        dataSourceStatistic.setCode(infoCodeDataSourceDefineModel.getCode());
                        dataSourceStatistic.setCodeName(infoCodeDataSourceDefineModel.getName());
                        if (dataValue != null && dataValue.equals(infoCodeDataSourceDefineModel.getCode())) {
                            dataSourceStatistic.setCodeCount(1);
                        } else {
                            dataSourceStatistic.setCodeCount(0);
                        }
                        dataSourceMap.put(infoCodeDataSourceDefineModel.getCode(), dataSourceStatistic);
                    }
                    infoCodeStatistics.setDataSourceMap(dataSourceMap);
                } else {
                    infoCodeStatistics.setInfoCodeObjectCount(infoCodeStatistics.getInfoCodeObjectCount() + 1);
                    Map<String, DataSourceStatistic> dataSourceMap = infoCodeStatistics.getDataSourceMap();
                    Object dataValue = infoCodeConfigGroupVO.getDataValue();
                    if (Objects.nonNull(dataValue)) {
                        DataSourceStatistic dataSourceStatistic = dataSourceMap.get(dataValue);
                        if (dataSourceStatistic != null) {
                            dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                            dataSourceMap.put(dataValue.toString(), dataSourceStatistic);
                            infoCodeStatistics.setDataSourceMap(dataSourceMap);
                        }
                    } else {
                        //无数据处理
                        DataSourceStatistic dataSourceStatistic = dataSourceMap.get("nullCode");
                        if (dataSourceStatistic != null) {
                            dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                            dataSourceMap.put("nullCode", dataSourceStatistic);
                            infoCodeStatistics.setDataSourceMap(dataSourceMap);
                        } else {
                            dataSourceStatistic = new DataSourceStatistic();
                            dataSourceStatistic.setCode("nullCode");
                            dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                            dataSourceStatistic.setCodeCount(1);
                            dataSourceMap.put("nullCode", dataSourceStatistic);
                            infoCodeStatistics.setDataSourceMap(dataSourceMap);
                        }
                    }
                }
                infoCodeStatisticsMap.put(infoCode, infoCodeStatistics);
            }
        }
        if (Objects.nonNull(alarmEventDetailInfoCodeStatistics)) {
            infoCodeStatisticsMap.put("alarmeventdetail", alarmEventDetailInfoCodeStatistics);
        }
        if (Objects.nonNull(armingStatusStatistics)) {
            infoCodeStatisticsMap.put("onlineStatusArming", armingStatusStatistics);
        }
        List<InfoCodeStatistics> infoCodeStatisticsList = new ArrayList<>();
        for (InfoCodeStatistics infoCodeStatistics : infoCodeStatisticsMap.values()) {
            Map<String, DataSourceStatistic> dataSourceMap = infoCodeStatistics.getDataSourceMap();
            infoCodeStatistics.setDataSource(Lists.newArrayList(dataSourceMap.values()));
            infoCodeStatistics.setDataSourceMap(null);
            infoCodeStatisticsList.add(infoCodeStatistics);
        }
        return infoCodeStatisticsList;

    }

    /**
     * @Author 薛涛
     * @Description 处理带撤布防能力摄像头的在离线状态统计
     * @Date 14:30 2024/9/19
     * @Param [armingStatusStatistics, infoCodeObjectVO]
     * @return com.persagy.apccontrol.basic.model.vo.outside.energy.InfoCodeStatistics
     **/
    private static InfoCodeStatistics ofArmingStatus(InfoCodeStatistics armingStatusStatistics, InfoCodeObjectVO infoCodeObjectVO) {

        for (InfoCodeConfigGroupInformationVO infoCodeConfigGroupVO : infoCodeObjectVO.getInfoCodeConfigGroupVOs()) {
            String infoCode = infoCodeConfigGroupVO.getInfoCode();
            if(infoCode.equals("onlineStatus")){
                List<InfoCodeDataSourceDefineModel> dataSource = infoCodeConfigGroupVO.getDataSource();
                if (Objects.isNull(armingStatusStatistics)) {
                    armingStatusStatistics = new InfoCodeStatistics();
                    armingStatusStatistics.setInfoCode("onlineStatusArming");
                    armingStatusStatistics.setInfoCodeName(infoCodeConfigGroupVO.getInfoCodeName());
                    armingStatusStatistics.setInfoCodeObjectCount(1);
                    Object dataValue = infoCodeConfigGroupVO.getDataValue();
                    Map<String, DataSourceStatistic> dataSourceMap = new HashMap<>(16);

                    if (Objects.isNull(dataValue)) {
                        DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                        dataSourceStatistic.setCode("nullCode");
                        dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                        dataSourceStatistic.setCodeCount(1);
                        dataSourceMap.put("nullCode", dataSourceStatistic);
                    }
                    for (InfoCodeDataSourceDefineModel infoCodeDataSourceDefineModel : dataSource) {
                        DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                        dataSourceStatistic.setCode(infoCodeDataSourceDefineModel.getCode());
                        dataSourceStatistic.setCodeName(infoCodeDataSourceDefineModel.getName());
                        if (dataValue != null && dataValue.equals(infoCodeDataSourceDefineModel.getCode())) {
                            dataSourceStatistic.setCodeCount(1);
                        } else {
                            dataSourceStatistic.setCodeCount(0);
                        }
                        dataSourceMap.put(infoCodeDataSourceDefineModel.getCode(), dataSourceStatistic);
                    }
                    armingStatusStatistics.setDataSourceMap(dataSourceMap);
                } else {
                    armingStatusStatistics.setInfoCodeObjectCount(armingStatusStatistics.getInfoCodeObjectCount() + 1);
                    Map<String, DataSourceStatistic> dataSourceMap = armingStatusStatistics.getDataSourceMap();
                    Object dataValue = infoCodeConfigGroupVO.getDataValue();
                    if (Objects.nonNull(dataValue)) {
                        DataSourceStatistic dataSourceStatistic = dataSourceMap.get(dataValue);
                        if (dataSourceStatistic != null) {
                            dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                            dataSourceMap.put(dataValue.toString(), dataSourceStatistic);
                            armingStatusStatistics.setDataSourceMap(dataSourceMap);
                        }
                    } else {
                        //无数据处理
                        DataSourceStatistic dataSourceStatistic = dataSourceMap.get("nullCode");
                        if (dataSourceStatistic != null) {
                            dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                            dataSourceMap.put("nullCode", dataSourceStatistic);
                            armingStatusStatistics.setDataSourceMap(dataSourceMap);
                        } else {
                            dataSourceStatistic = new DataSourceStatistic();
                            dataSourceStatistic.setCode("nullCode");
                            dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                            dataSourceStatistic.setCodeCount(1);
                            dataSourceMap.put("nullCode", dataSourceStatistic);
                            armingStatusStatistics.setDataSourceMap(dataSourceMap);
                        }
                    }
                }
            continue;
            }
        }
        return armingStatusStatistics;


    }

    /**
     * @return com.persagy.apccontrol.basic.model.vo.outside.energy.InfoCodeStatistics
     * @Author 薛涛
     * @Description 高等级异常事件统计
     * @Date 20:52 2024/9/13
     * @Param [infoCodeStatistics, infoCodeObjectVO]
     **/
    public static InfoCodeStatistics ofAlarmEventDetail(InfoCodeStatistics infoCodeStatistics, InfoCodeConfigGroupInformationVO infoCodeConfigGroupVO) {

        List<ExceptionEventBaseInfoVO> eventInfoDTOS = (List<ExceptionEventBaseInfoVO>) infoCodeConfigGroupVO.getDataValue();
        if (CollectionUtils.isEmpty(eventInfoDTOS)) {
            return infoCodeStatistics;
        }
        Integer exceptionEventCount = 0;
        if (CollectionUtils.isNotEmpty(eventInfoDTOS)) {
            Set<Integer> importanceDegreeSet = new HashSet<>();
            importanceDegreeSet.add(ImportanceDegreeEnum.HIGH.getValue());
            for (ExceptionEventBaseInfoVO eventInfoDTO : eventInfoDTOS) {
                int value = eventInfoDTO.getImportanceDegree().getValue();
                if (importanceDegreeSet.contains(value)) {
                    exceptionEventCount++;
                }
            }
        }
        if (exceptionEventCount.equals(0)) {
            return infoCodeStatistics;
        }
        if (Objects.isNull(infoCodeStatistics)) {
            infoCodeStatistics = new InfoCodeStatistics();
            infoCodeStatistics.setInfoCode("alarmeventdetail");
            infoCodeStatistics.setInfoCodeName(LocaleMessageUtil.getMessage("key_6df1ec01bd544768ae77a0978819a361"));
            infoCodeStatistics.setInfoCodeObjectCount(null);
            DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
            dataSourceStatistic.setCode("alarmstatus");
            dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_5bf0b8410a1f4947ab5c4d9c580e3db4"));
            dataSourceStatistic.setCodeCount(exceptionEventCount);
            Map<String, DataSourceStatistic> dataSourceMap = new HashMap<>(16);
            dataSourceMap.put("alarmstatus", dataSourceStatistic);
            infoCodeStatistics.setDataSourceMap(dataSourceMap);
        } else {
            Map<String, DataSourceStatistic> dataSourceMap = infoCodeStatistics.getDataSourceMap();
            DataSourceStatistic dataSourceStatistic = infoCodeStatistics.getDataSourceMap().get("alarmstatus");
            dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
            dataSourceMap.put("alarmstatus", dataSourceStatistic);
            infoCodeStatistics.setDataSourceMap(dataSourceMap);
            infoCodeStatistics.setDataSource(Lists.newArrayList(dataSourceStatistic));
        }

        return infoCodeStatistics;

    }


    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.outside.energy.InfoCodeStatistics>
     * @Author 薛涛
     * @Description InfoCodeValueVO转换统计
     * @Date 14:23 2024/9/6
     * @Param [infoCodeObjectVOs]
     **/
    public static List<InfoCodeStatistics> ofInfoCodeValueVO(List<InfoCodeValueVO> infoCodeObjectVOs) {
        Map<String, InfoCodeStatistics> infoCodeStatisticsMap = new HashMap<>(16);
        for (InfoCodeValueVO infoCodeConfigGroupVO : infoCodeObjectVOs) {
            String infoCode = infoCodeConfigGroupVO.getInfoCode();
            InfoCodeStatistics infoCodeStatistics = infoCodeStatisticsMap.get(infoCode);
            List<DtpKnowledgeInfoCdeDataSourceVO> dataSources = infoCodeConfigGroupVO.getDataSources();
            if (CollectionUtils.isEmpty(dataSources)) {
                continue;
            }
            if (infoCodeStatistics == null) {
                infoCodeStatistics = new InfoCodeStatistics();
                infoCodeStatistics.setInfoCode(infoCode);
                infoCodeStatistics.setInfoCodeName(infoCodeConfigGroupVO.getInfoCodeName());
                infoCodeStatistics.setInfoCodeObjectCount(1);
                Object dataValue = infoCodeConfigGroupVO.getDataValue();
                Map<String, DataSourceStatistic> dataSourceMap = new HashMap<>(16);
                for (DtpKnowledgeInfoCdeDataSourceVO infoCodeDataSourceDefineModel : dataSources) {
                    DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                    dataSourceStatistic.setCode(infoCodeDataSourceDefineModel.getCode());
                    dataSourceStatistic.setCodeName(infoCodeDataSourceDefineModel.getName());
                    if (dataValue != null && dataValue.equals(infoCodeDataSourceDefineModel.getCode())) {
                        dataSourceStatistic.setCodeCount(1);
                    } else {
                        dataSourceStatistic.setCodeCount(0);
                    }
                    dataSourceMap.put(infoCodeDataSourceDefineModel.getCode(), dataSourceStatistic);
                }
                if (Objects.isNull(dataValue)) {
                    DataSourceStatistic dataSourceStatistic = new DataSourceStatistic();
                    dataSourceStatistic.setCode("nullCode");
                    dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                    dataSourceStatistic.setCodeCount(1);
                    dataSourceMap.put("nullCode", dataSourceStatistic);
                }
                infoCodeStatistics.setDataSourceMap(dataSourceMap);
            } else {
                infoCodeStatistics.setInfoCodeObjectCount(infoCodeStatistics.getInfoCodeObjectCount() + 1);
                Map<String, DataSourceStatistic> dataSourceMap = infoCodeStatistics.getDataSourceMap();
                Object dataValue = infoCodeConfigGroupVO.getDataValue();
                if (Objects.nonNull(dataValue)) {
                    DataSourceStatistic dataSourceStatistic = dataSourceMap.get(dataValue);
                    if (dataSourceStatistic != null) {
                        dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                        dataSourceMap.put(dataValue.toString(), dataSourceStatistic);
                        infoCodeStatistics.setDataSourceMap(dataSourceMap);
                    }

                } else {
                    //无数据处理
                    DataSourceStatistic dataSourceStatistic = dataSourceMap.get("nullCode");
                    if (dataSourceStatistic != null) {
                        dataSourceStatistic.setCodeCount(dataSourceStatistic.getCodeCount() + 1);
                        dataSourceMap.put("nullCode", dataSourceStatistic);
                        infoCodeStatistics.setDataSourceMap(dataSourceMap);
                    } else {
                        dataSourceStatistic = new DataSourceStatistic();
                        dataSourceStatistic.setCode("nullCode");
                        dataSourceStatistic.setCodeName(LocaleMessageUtil.getMessage("key_8c13979c9e684f43b9b6cfa4988b004f"));
                        dataSourceStatistic.setCodeCount(1);
                    }
                }
            }
            infoCodeStatisticsMap.put(infoCode, infoCodeStatistics);
        }

        List<InfoCodeStatistics> infoCodeStatisticsList = new ArrayList<>();
        for (InfoCodeStatistics infoCodeStatistics : infoCodeStatisticsMap.values()) {
            Map<String, DataSourceStatistic> dataSourceMap = infoCodeStatistics.getDataSourceMap();
            infoCodeStatistics.setDataSource(Lists.newArrayList(dataSourceMap.values()));
            infoCodeStatistics.setDataSourceMap(null);
            infoCodeStatisticsList.add(infoCodeStatistics);
        }
        return infoCodeStatisticsList;

    }
}