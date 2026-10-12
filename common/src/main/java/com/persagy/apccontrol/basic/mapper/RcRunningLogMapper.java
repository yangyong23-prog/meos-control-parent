package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogEquipmentDto;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogEquipmentListQueryParam;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 针对表【rc_running_log(运行日志表)】的数据库操作Mapper
 */
@Mapper
@Repository
public interface RcRunningLogMapper extends SuperMapper<RcRunningLog> {

    List<RunningLogEquipmentDto> equipmentList(@Param("param") RunningLogEquipmentListQueryParam param);

    @Select("SELECT `type`,COUNT(1) as count  FROM rc_running_log x WHERE project_id =#{projectId} and  create_time >= CURDATE() GROUP BY `type`; ")
    List<Map<String, Object>> countWithType(String projectId);
}




