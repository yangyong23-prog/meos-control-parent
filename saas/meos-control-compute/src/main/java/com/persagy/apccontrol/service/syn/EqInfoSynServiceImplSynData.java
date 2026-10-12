package com.persagy.apccontrol.service.syn;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.entity.RcEqInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.resource.RcEqInfoCodeCfgService;
import com.persagy.apccontrol.basic.service.AbstractSynData;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;


/**
 * 同步云端基础配置到 本地
 *
 * @author yuyao
 */
@Service
public class EqInfoSynServiceImplSynData extends AbstractSynData {



    @Resource
    RcEqInfoCodeCfgService rcEqInfoCodeCfgService;

    @Override
    public void getSynUrl(SynDataCtx ctx) {
        //MQ 消息同步
        ctx.setPortSyn(false);
    }

    @Override
    public void getSynDate(SynDataCtx ctx) {
        // 同步 设备信息点 数据
        List<RcEqInfoCodeCfg> fcgList = rcEqInfoCodeCfgService.lambdaQuery()
                .select(RcEqInfoCodeCfg::getId)
                .eq(RcEqInfoCodeCfg::getProjectId, ctx.getProjectId())
                .isNull(RcEqInfoCodeCfg::getSysTime)
                .list();

        if (!CollectionUtils.isEmpty(fcgList)) {

            fcgList = rcEqInfoCodeCfgService.lambdaQuery()
                    .eq(RcEqInfoCodeCfg::getProjectId, ctx.getProjectId())
                    .list();

            DataSynDTO f = new DataSynDTO();
            f.setProjectId(ctx.getProjectId());
            f.setTableName(DataSynRunEnum.RC_EQ_INFO_CODE_CFG);
            f.setYsnObj(ListUtil.toList());
            fcgList.forEach(i -> f.getYsnObj().add(JSONUtil.toJsonStr(i)));
            ctx.setYsnObj(f);
        }

    }

    @Override
    public void syn(SynDataCtx ctx) {
        if (ctx.getYsnObj() == null) {
            return;
        }
        //发送MQ 进行数据同步
      //  rabbitMessageSender.send(MqMessageConstant.APC_CONTROL_SYN_FANOUT, ctx.getProjectId(), JSONUtil.toJsonStr(ctx.getYsnObj()));
    }

    @Override
    public void updateResult(SynDataCtx ctx) {
        //只有接口更新才进行 结果更新 MQ同步走接口回调更新
    }
}
