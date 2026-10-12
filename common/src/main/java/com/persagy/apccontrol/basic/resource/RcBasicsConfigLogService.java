package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfigLog;
import com.persagy.apccontrol.basic.model.order.ConfigLogOrder;
import com.persagy.framework.common.entity.PageParams;
import com.persagy.framework.database.service.SuperService;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config_log(配置日志表)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcBasicsConfigLogService extends SuperService<RcBasicsConfigLog> {


    /**
     * 保存配置快照
     *
     * @param cfg
     */
    public void saveConfigLog(RcBasicsConfig cfg, RcBasicsConfig before);

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    public IPage<RcBasicsConfigLog> queryPage(PageParams<ConfigLogOrder> params);

}
