package com.persagy.apccontrol.common.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description: 时间表下发
 * @Author: laixingqi
 * @Date: 2023/12/28 18:26
 * @param:
 * @return:
 **/
public interface ScheduleInstructService {


    /**
     * 更新当日时间表指令-通过时间表生成当日的未来时间的时间表指令
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param classCode
     * @param objId
     */
    public void updateScheduleInstruct(String groupCode, String projectId, String systemId,
                                       String classCode, String objId);

    /**
     * @Description: 时间表下发
     * @Author: laixingqi
     * @Date: 2024/1/10 19:55
     * @param: [groupCode, projectId, systemId, classCode, objId, infoCodeSyList, listMatic]
     * @return: void
     **/
    void distributionSchedule(String groupCode, String projectId, String systemId, String classCode,
                              String objId, List<Map<LocalDateTime, String>> listMatic, LocalDateTime executionTime);

    /**
     * @Description: 1根据objecId+信息点取出时间表计划
     * 2如果有，下发时间指令
     * 3 如果没有，存objectId+信息点为自动
     * @Author: laixingqi
     * @Date: 2023/12/23 10:53
     * @param: []
     * @return: java.util.Map<java.lang.String, java.util.List < java.util.Map < java.time.LocalDateTime, java.lang.String>>>
     **/
    Map<String, List<Map<LocalDateTime, String>>> getStrategys();
}
