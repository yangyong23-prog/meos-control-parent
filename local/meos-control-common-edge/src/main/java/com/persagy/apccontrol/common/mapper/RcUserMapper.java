package com.persagy.apccontrol.common.mapper;

import com.persagy.apccontrol.common.model.entity.RcUser;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author yuyao
 * @description 针对表【rc_user(本地用户信息表)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcUser
 */
@Mapper
@Repository
public interface RcUserMapper extends SuperMapper<RcUser> {

}




