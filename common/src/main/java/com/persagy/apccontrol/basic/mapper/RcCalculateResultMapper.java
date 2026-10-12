package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 价值计算结果
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Mapper
@Repository
public interface RcCalculateResultMapper extends SuperMapper<RcCalculateResult> {

}




