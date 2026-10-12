package com.persagy.apccontrol.service.syn;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.entity.RcSystemPic;
import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.PictureTypeEnum;
import com.persagy.apccontrol.basic.resource.RcSystemPicService;
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
public class PicSynIconServiceImplSynData extends AbstractSynData {



    @Resource
    private RcSystemPicService rcSystemPicService;

    @Override
    public void getSynUrl(SynDataCtx ctx) {
        //MQ 消息同步
        ctx.setPortSyn(false);
    }

    @Override
    public void getSynDate(SynDataCtx ctx) {
        //系统图 和 设备图同步单独处理 系统图会平凡一些
        List<RcSystemPic> fcgList2 = rcSystemPicService.lambdaQuery()
                .select(RcSystemPic::getId)
                .eq(RcSystemPic::getType, PictureTypeEnum.EQUIPMENT)
                .last("update_time != sys_time").list();

        if (!CollectionUtils.isEmpty(fcgList2)) {
            fcgList2 = rcSystemPicService.lambdaQuery()
                    .eq(RcSystemPic::getType, PictureTypeEnum.EQUIPMENT)
                    .list();

            DataSynDTO f = new DataSynDTO();
            f.setProjectId(ctx.getProjectId());
            f.setTableName(DataSynRunEnum.RC_SYSTEM_PIC_EQUIPMENT);
            f.setYsnObj(ListUtil.toList());
            fcgList2.forEach(i -> f.getYsnObj().add(JSONUtil.toJsonStr(i)));
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
