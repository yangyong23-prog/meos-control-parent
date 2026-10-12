package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.persagy.apccontrol.basic.model.dto.system.SystemOperationRecordsDTO;
import com.persagy.apccontrol.basic.model.entity.RcSysLog;
import com.persagy.apccontrol.basic.model.vo.system.SystemOperationRecordsVO;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author yuyao
 * @description 针对表【rc_sys_log(系统日志表)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcSysLog
 */
@Mapper
@Repository
public interface RcSysLogMapper extends SuperMapper<RcSysLog> {

    /**
     * 查询运行记录
     * @param buildPage
     * @param model
     * @return
     */
    Page<SystemOperationRecordsVO> querySystemOperationRecords(@Param("page")IPage<?> buildPage, @Param("model") SystemOperationRecordsDTO model);



}




