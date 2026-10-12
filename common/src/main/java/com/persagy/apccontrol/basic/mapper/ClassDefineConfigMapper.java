package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.ClassDefineConfig;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 设备类业务定义
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Mapper
@Repository
public interface ClassDefineConfigMapper extends SuperMapper<ClassDefineConfig> {

}




