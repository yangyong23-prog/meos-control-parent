package com.persagy.apccontrol.basic.service.airterminal;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.entity.po.AnalysisAirConditionerSpacePO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【analysis_air_conditioner_spaces(运行分析-空调空间分析)】的数据库操作Service
 * @createDate 2024-12-03 10:31:19
 */
public interface AnalysisAirConditionerSpaceService extends IService<AnalysisAirConditionerSpacePO> {

    /**
     * 批量保存数据并删除 60 天前的数据
     *
     * @param list 保存的数据
     */
    void saveBatchAndDel(List<AnalysisAirConditionerSpace> list);

    boolean deleteByDay(@NotNull LocalDate day);


    /**
     * 按照日期+楼层 id+标签类型批量查询数据
     * @param spaceId 筛选空间 ID 非必须
     * @param floorIds 楼层 ID 非必须
     * @param day 查询日期 必须
     * @param abnormal 是否异常
     * @param tagType 标签
     * @return 列表数据
     */
    List<AnalysisAirConditionerSpace> listByDayAndAbnormalAndTagType(List<String> spaceId, List<String> floorIds,List<String> buildingIds,
                                                                     @NotNull LocalDate day, Boolean abnormal, RunningMonitorSecondTypeEnum tagType);

    /**
     * 查询运行分析结果表 某天的 空间规则
     *
     * @param day 具体日期
     * @return 空间规则
     */
    public Map<String, Set<String>> getSpaceByInfoCodeRule(@NotNull LocalDate day);

    /**
     * 查询项目达标率
     *
     * @param day
     * @param tagType
     * @return
     */
    Double projectSatisfyPercentage(@NotNull LocalDate day, @NotNull RunningMonitorSecondTypeEnum tagType);

    /**
     * @author wxf
     * @description 获取空间达标率
     * @date 17:32 2025/10/14
     * @param day
     * @param tagType
     * @param spaceIds
     * @return java.lang.Double
    **/
    Double spaceSatisfyPercentage(@NotNull LocalDate day, @NotNull RunningMonitorSecondTypeEnum tagType, @NotNull Set<String> spaceIds);

    /**
     * @author wxf
     * @description 返回空间标签数量
     * @date 11:28 2025/10/16
     * @param day
     * @param tagType
     * @param spaceIds
     * @return java.util.Map<java.lang.String,java.lang.Integer>
    **/
    Map<String,Integer> getSpaceTagCount(@NotNull LocalDate day, @NotNull RunningMonitorSecondTypeEnum tagType, List<String> spaceIds);

}
