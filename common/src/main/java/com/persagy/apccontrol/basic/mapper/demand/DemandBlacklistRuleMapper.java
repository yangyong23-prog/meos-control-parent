package com.persagy.apccontrol.basic.mapper.demand;

import com.persagy.apccontrol.basic.model.entity.demand.DemandBlacklistRule;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author yuyao
 * @description 针对表【demand_blacklist_rule(需求黑名单规则表)】的数据库操作Mapper
 * @createDate 2025-03-04 11:19:33
 * @Entity generator.domain.DemandBlacklistRule
 */
@Mapper
@Repository
public interface DemandBlacklistRuleMapper extends SuperMapper<DemandBlacklistRule> {

}




