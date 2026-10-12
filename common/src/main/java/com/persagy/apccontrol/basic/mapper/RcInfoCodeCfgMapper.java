package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author yuyao
 * @description 针对表【rc_info_code_cfg(信息点配置 )】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcInfoCodeCfg
 */
@Mapper
@Repository
public interface RcInfoCodeCfgMapper extends SuperMapper<RcInfoCodeCfg> {

}




