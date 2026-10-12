package com.persagy.apccontrol.service.syn;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.model.enumtype.task.SystemEnum;
import com.persagy.apccontrol.basic.resource.RcBasicsAsyncTaskService;
import com.persagy.apccontrol.basic.service.AbstractSynData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 同步云端基础配置到 本地
 *
 * @author yuyao
 */
@Service
public class TaskSynServiceImplSynData extends AbstractSynData {

    @Resource
    RcBasicsAsyncTaskService rcBasicsAsyncTaskService;



    @Override
    public void getSynUrl(SynDataCtx ctx) {
        //MQ 消息同步
        ctx.setPortSyn(false);
    }

    @Override
    public void getSynDate(SynDataCtx ctx) {

        //获取所有 待同步的数据 任务同步情况是 同步时间为空 其它为 同步时间为空或者同步数据不等于更新时间情况
        List<RcBasicsAsyncTask> list = rcBasicsAsyncTaskService.lambdaQuery()
                .select(RcBasicsAsyncTask::getId)
                .eq(RcBasicsAsyncTask::getSys, SystemEnum.LOCALITY)
                .isNull(RcBasicsAsyncTask::getSynTime).list();

        if (!CollectionUtils.isEmpty(list)) {
            //存在需要同步的数据 全量数据同步
            list = rcBasicsAsyncTaskService.lambdaQuery()
                    .eq(RcBasicsAsyncTask::getSys, SystemEnum.LOCALITY).list();

            DataSynDTO d = new DataSynDTO();
            d.setAll(true);
            d.setProjectId(ctx.getProjectId());
            d.setTableName(DataSynRunEnum.RC_BASICS_ASYNC_TASK);
            d.setYsnObj(ListUtil.toList());
            list.forEach(i -> d.getYsnObj().add(JSONUtil.toJsonStr(i)));
            ctx.setYsnObj(d);
        }

    }

    @Override
    public void syn(SynDataCtx ctx) {
        if (ctx.getYsnObj() == null) {
            return;
        }
        //发送MQ 进行数据同步
       // rabbitMessageSender.send(MqMessageConstant.APC_CONTROL_SYN_FANOUT, ctx.getProjectId(), JSONUtil.toJsonStr(ctx.getYsnObj()));
    }

    @Override
    public void updateResult(SynDataCtx ctx) {
        //只有接口更新才进行 结果更新 MQ同步走接口回调更新
    }
}
