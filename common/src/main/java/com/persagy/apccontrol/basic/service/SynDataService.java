package com.persagy.apccontrol.basic.service;

import com.persagy.apccontrol.basic.model.context.SynDataCtx;

/**
 * 同步数据服务
 *
 * @author yuyao
 */
public interface SynDataService {

    /**
     * @param ctx
     */
    public void execute(SynDataCtx ctx);

}
