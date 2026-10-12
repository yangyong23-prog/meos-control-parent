package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * websocket推送优化
 */
@Mapper
@Repository
public interface WebsocketConfigMapper extends SuperMapper<WebsocketConfig> {

}




