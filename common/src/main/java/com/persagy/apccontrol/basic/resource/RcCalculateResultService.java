package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.framework.database.service.SuperService;

import java.util.List;
import java.util.Set;

/**
 * 价值计算结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
public interface RcCalculateResultService extends SuperService<RcCalculateResult> {

    /**
     * 获取价值计算结果
     *
     * @param groupCode
     * @param projectId
     * @param spaceId
     * @param workDate
     * @param calculateType 价值计算类型
     * @return
     */
    List<RcCalculateResult> queryCalculateResultList(String groupCode, String projectId, Set<String> spaceId, String workDate, BasicsConfigTypeEnum calculateType);

    /**
     * 获取价值计算的所有空间ID
     *
     * @param groupCode
     * @param projectId
     * @param workDate
     * @return
     */
    Set<String> getSpaceCalculateResultIds(String groupCode, String projectId, String workDate);

    /**
     * 获取价值计算结果
     *
     * @param groupCode
     * @param projectId
     * @param spaceId
     * @param workDate
     * @return
     */
    RcCalculateResult getSpaceCalculateResult(String groupCode, String projectId, String spaceId, String workDate);

    /**
     * 获取多天价值计算结果
     * @param groupCode 集团编码
     * @param projectId 项目id
     * @param spaceId 空间id
     * @param workDate 价值计算结果天
     * @param calculateType 配置枚举类型
     * @return 价值计算结果
     */
    List<RcCalculateResult> queryMultiDayCalculateResultList(String groupCode,
                                                             String projectId,
                                                             List<String> spaceId,
                                                             List<String> workDate,
                                                             BasicsConfigTypeEnum calculateType);

}
