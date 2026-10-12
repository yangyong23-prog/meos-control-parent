package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogNodeMapper;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class LogNodeRepositoryImpl extends ServiceImpl<LogNodeMapper, LogNodePo> implements LogNodeRepository {

    @Override
    public List<LogNodePo> findNodeByBusinessIds(Collection<String> businessIds, String businessType) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<LogNodePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(LogNodePo::getBusinessId, businessIds)
                .eq(!StringUtils.isEmpty(businessType), LogNodePo::getApplication, businessType);
        return getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<LogNodePo> getByTime(LocalDateTime beginTime, LocalDateTime endTime, String nodeCode, String application) {


        LambdaQueryWrapper<LogNodePo> chainWrapper = new LambdaQueryWrapper<>();
        chainWrapper.eq(LogNodePo::getCode, nodeCode)
                .between(LogNodePo::getBusinessTime, beginTime, endTime)
                .eq(!StringUtils.isEmpty(application), LogNodePo::getApplication, application)
                .orderByDesc(LogNodePo::getBusinessTime);
        return getBaseMapper().selectList(chainWrapper);

    }


    //    getByPageByTime(){
//        Page<MetaOptionTypeValuePo> page = new Page<>();
//        page.setCurrent(pageDto.getCurrent());
//        page.setSize(pageDto.getSize());
//        LambdaQueryChainWrapper<MetaOptionTypeValuePo> chainWrapper = this.lambdaQuery();
//        chainWrapper.and(org.springframework.util.StringUtils.hasLength(pageDto.getKeyword()), query -> {
//            query.like(MetaOptionTypeValuePo::getCode, pageDto.getKeyword())
//                    .or().like(MetaOptionTypeValuePo::getName, pageDto.getKeyword());
//        });
//        if (CollectionUtils.isNotEmpty(pageDto.getOrders())) {
//            page.addOrder(pageDto.getOrders());
//        } else {
//            chainWrapper.orderByDesc(MetaOptionTypeValuePo::getCreateTime);
//        }
//        return chainWrapper.page(page);
//    }
//    }
}
