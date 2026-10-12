package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.persagy.apccontrol.basic.mapper.RcBasicsConfigLogMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfigLog;
import com.persagy.apccontrol.basic.model.order.ConfigLogOrder;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigLogService;
import com.persagy.apccontrol.basic.utils.SnowflakeIdWorker;
import com.persagy.apccontrol.basic.utils.UserUtils;
import com.persagy.framework.common.entity.PageParams;
import com.persagy.framework.database.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config_log(配置日志表)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
@Slf4j
public class RcBasicsConfigLogServiceImpl extends SuperServiceImpl<RcBasicsConfigLogMapper, RcBasicsConfigLog>
        implements RcBasicsConfigLogService {

    @Override
    public void saveConfigLog(RcBasicsConfig cfg, RcBasicsConfig before) {
        //记录配置快照
        try {
            // 查询历史最新
            RcBasicsConfigLog configLog = queryConfig(before);
            String parentId = Objects.isNull(configLog) ? null : configLog.getId();
            RcBasicsConfigLog log = BeanUtil.toBean(cfg, RcBasicsConfigLog.class);
            log.setId(SnowflakeIdWorker.getWorkerId());
            log.setCreateTime(new Date());
            log.setCreateUser(UserUtils.getUserName());
            log.setParentId(parentId);
            this.save(log);
        } catch (Exception e) {
            log.warn("配置日志记录失败！-{}", cfg, e);
        }
    }

    /**
     * 通过配置 查询存在的配置
     *
     * @param cfg
     * @return
     */
    public RcBasicsConfigLog queryConfig(RcBasicsConfig cfg) {
        return lambdaQuery()
                .eq(RcBasicsConfigLog::getType, cfg.getType())
                .eq(StringUtils.isNotBlank(cfg.getProjectId()), RcBasicsConfigLog::getProjectId, cfg.getProjectId())
                .eq(StringUtils.isNotBlank(cfg.getChildType()), RcBasicsConfigLog::getChildType, cfg.getChildType())
                .eq(StringUtils.isNotBlank(cfg.getSystemId()), RcBasicsConfigLog::getSystemId, cfg.getSystemId())
                .eq(StringUtils.isNotBlank(cfg.getFloorCode()), RcBasicsConfigLog::getFloorCode, cfg.getFloorCode())
                .eq(StringUtils.isNotBlank(cfg.getArchitectureCode()), RcBasicsConfigLog::getArchitectureCode, cfg.getArchitectureCode())
                .eq(StringUtils.isNotBlank(cfg.getFacilityCode()), RcBasicsConfigLog::getFacilityCode, cfg.getFacilityCode())
                .eq(StringUtils.isNotBlank(cfg.getRoomCode()), RcBasicsConfigLog::getRoomCode, cfg.getRoomCode())
                .eq(cfg.getStartTime() != null, RcBasicsConfigLog::getStartTime, cfg.getStartTime())
                .eq(cfg.getEndTime() != null, RcBasicsConfigLog::getEndTime, cfg.getEndTime())
                .eq(StringUtils.isNotBlank(cfg.getMd()), RcBasicsConfigLog::getMd, cfg.getMd())
                .orderByDesc(RcBasicsConfigLog::getCreateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
    }


    @Override
    public IPage<RcBasicsConfigLog> queryPage(PageParams<ConfigLogOrder> params) {
        ConfigLogOrder order = params.getModel();

        if (CollectionUtils.isEmpty(params.getOrders())) {
            //默认执行时间排序
            OrderItem oi = new OrderItem("createTime", false);
            List<OrderItem> orders = ListUtil.toList();
            orders.add(oi);
            params.setOrders(orders);
        }
        IPage<RcBasicsConfigLog> page = params.buildPage(RcBasicsConfigLog.class);

        LambdaQueryWrapper<RcBasicsConfigLog> qw = new LambdaQueryWrapper<>();
        qw.eq(RcBasicsConfigLog::getProjectId, order.getProjectId());
        qw.eq(RcBasicsConfigLog::getType, order.getType());
        qw.orderByAsc(RcBasicsConfigLog::getCreateTime);

        return this.page(page, qw);
    }

}




