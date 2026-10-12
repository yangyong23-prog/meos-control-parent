package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.dto.system.SystemSassRecordsDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfigLog;
import com.persagy.apccontrol.basic.model.vo.system.SystemOperationRecordsVO;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config_log(配置日志表)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcBasicsConfigLog
 */
@Mapper
@Repository
public interface RcBasicsConfigLogMapper extends SuperMapper<RcBasicsConfigLog> {

    /**
     * 查询操作记录
     * @author tangzy
     * @param model
     * @return java.util.List<com.persagy.apccontrol.model.vo.system.SystemOperationRecordsVO>
     **/
    List<SystemOperationRecordsVO> queryOperationRecordsList(@Param("model") SystemSassRecordsDTO model);
}




