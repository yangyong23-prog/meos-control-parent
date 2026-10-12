package com.persagy.apccontrol.service.syn;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.service.AbstractSynData;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


/**
 * 同步云端基础配置到 本地
 *
 * @author yuyao
 */
@Service
public class CfgSynServiceImplSynData extends AbstractSynData {

    @Resource
    RcBasicsConfigService rcBasicsConfigService;



    @Override
    public void getSynUrl(SynDataCtx ctx) {
        //MQ 消息同步
        ctx.setPortSyn(false);
    }

    @Override
    public void getSynDate(SynDataCtx ctx) {
        //配置数据 只同步 专家规则 和 天气配置数据
        List<BasicsConfigTypeEnum> type = Arrays.asList(BasicsConfigTypeEnum.EXPERT_RULES, BasicsConfigTypeEnum.INO_ENVIRONMENT);
        List<RcBasicsConfig> fcgList = rcBasicsConfigService.querySyn(ctx.getProjectId(), type);

        if (!CollectionUtils.isEmpty(fcgList)) {
            //存在需要同步的数据 全量数据同步
            fcgList = rcBasicsConfigService.lambdaQuery()
                    .in(RcBasicsConfig::getType,
                            Arrays.asList(BasicsConfigTypeEnum.EXPERT_RULES, BasicsConfigTypeEnum.INO_ENVIRONMENT))
                    .list();
            DataSynDTO f = new DataSynDTO();
            f.setAll(true);
            f.setProjectId(ctx.getProjectId());
            f.setTableName(DataSynRunEnum.RC_BASICS_CONFIG);
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
