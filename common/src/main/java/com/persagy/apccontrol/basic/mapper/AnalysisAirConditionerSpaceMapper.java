package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.po.AnalysisAirConditionerSpacePO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【analysis_air_conditioner_spaces(运行分析-空调空间分析)】的数据库操作Mapper
 * @createDate 2024-12-03 10:31:19
 * @Entity generator.domain.AnalysisAirConditionerSpaces
 */
@Mapper
@Repository
public interface AnalysisAirConditionerSpaceMapper extends BaseMapper<AnalysisAirConditionerSpacePO> {

    /**
     * 项目达标率统计
     *
     * @param day
     * @param tagType
     * @return
     */
    public Double projectSatisfyPercentage(@NonNull @Param("projectId") String projectId,
                                           @NonNull @Param("day") LocalDate day,
                                           @NonNull @Param("tagType") RunningMonitorSecondTypeEnum tagType);

    public Double spaceSatisfyPercentage(@NonNull @Param("projectId") String projectId,
                                           @NonNull @Param("day") LocalDate day,
                                           @NonNull @Param("spaceIds") Set<String> spaceIds,
                                           @NonNull @Param("tagType") RunningMonitorSecondTypeEnum tagType);

}




