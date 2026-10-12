package com.persagy.apccontrol.basic.structure.bizcheck.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务检查结果Mapper接口
 *
 * @author wxf
 * @date 2026-02-27
 */
@Mapper
public interface BizCheckResultMapper extends BaseMapper<BizCheckResult> {

}