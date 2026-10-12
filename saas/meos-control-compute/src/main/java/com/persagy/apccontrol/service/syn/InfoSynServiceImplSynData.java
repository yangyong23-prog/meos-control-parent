package com.persagy.apccontrol.service.syn;

import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.service.AbstractSynData;
import org.springframework.stereotype.Service;


/**
 * 同步云端基础配置到 本地
 *
 * @author yuyao
 */
@Service
public class InfoSynServiceImplSynData extends AbstractSynData {



    @Override
    public void getSynUrl(SynDataCtx ctx) {
        //MQ 消息同步
        ctx.setPortSyn(false);
    }

    @Override
    public void getSynDate(SynDataCtx ctx) {

    }

    @Override
    public void syn(SynDataCtx ctx) {
        if (ctx.getYsnObj() == null) {
            return;
        }
        //发送MQ 进行数据同步
    //    rabbitMessageSender.send(MqMessageConstant.APC_CONTROL_SYN_FANOUT, ctx.getProjectId(), JSONUtil.toJsonStr(ctx.getYsnObj()));
    }

    @Override
    public void updateResult(SynDataCtx ctx) {
        //只有接口更新才进行 结果更新 MQ同步走接口回调更新
    }
}
