package com.persagy.apccontrol.basic.service;

import com.persagy.apccontrol.basic.model.context.SynDataCtx;

/**
 * 数据同步策略类
 *
 * @author yuyao
 */
public abstract class AbstractSynData implements SynDataService {

    @Override
    public void execute(SynDataCtx ctx) {
        getSynUrl(ctx);
        getSynDate(ctx);
        syn(ctx);
        updateResult(ctx);
    }


    /**
     * 获取连接地址 通过项目获取
     */
    public abstract void getSynUrl(SynDataCtx ctx);


    /**
     * 获取待同步的数据 通过项目编号
     */
    public abstract void getSynDate(SynDataCtx ctx);


    /**
     * 调用同步接口/发送MQ消息 进行数据同步
     */
    public abstract void syn(SynDataCtx ctx);

    /**
     * 更新同据结果
     *
     * @param ctx
     */
    public abstract void updateResult(SynDataCtx ctx);

}
