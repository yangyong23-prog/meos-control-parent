package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author yuyao
 * @description 针对表【running_log(感知运行日志表)】的数据库操作Mapper
 * @createDate 2025-04-22 16:47:52
 * @Entity generator.domain.RunningLog
 */
@Mapper
public interface RunningLogMapper extends BaseMapper<RunningLog> {
    
    /**
     * 定时创建分区和删除历史分区
     * month 需要删除几个月前的数据
     */
    public void createPartitionAndDeleteHistoryPartition(@Param("month") int month);
    
    
    /**
     * 获取项目下运行日志类型统计 当日的
     *
     * @param projectId
     * @return
     */
    @Select("SELECT `type`,COUNT(1) as count  FROM running_log x WHERE project_id =#{projectId} and  data_time >= " +
            "CURDATE() GROUP BY `type`; ")
    List<Map<String, Object>> countWithTypeByToday(String projectId);
}




