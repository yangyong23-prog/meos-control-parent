package com.persagy.apccontrol.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.feign.client.DtpMeosServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcCommandSendDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.InfoCodeSetExtendService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.model.param.CoolWaterReturnTempCfgParam;
import com.persagy.apccontrol.common.model.param.ExtendInfoCodeValueConfigParam;
import com.persagy.apccontrol.common.model.param.OutsideCoolWaterReturnTempCfgParam;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.service.ColdCfgService;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 冷战配置相关业务
 */
@Service
public class ColdCfgServiceImpl implements ColdCfgService {

    @Resource
    InfoCodeSetExtendService infoCodeSetExtendService;
    @Resource
    DtpMeosServerClient dtpMeosServerClient;
    @Autowired
    SseHandlerContext sseHandlerContext;
    @Autowired
    private IWSMessageService iWSMessageService;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Override
    public void coolWaterReturnTempCfg(CoolWaterReturnTempCfgParam param) {

        // 保存信息点设置扩展数据
        InfoCodeSetExtend infoCodeSetExtend = new InfoCodeSetExtend();
        infoCodeSetExtend.setProjectId(ContextUtil.getProjectId());
        infoCodeSetExtend.setSystemId(param.getSystemId());
        infoCodeSetExtend.setObjId(param.getSystemId());
        infoCodeSetExtend.setInfoCode(param.getInfoCode());
        infoCodeSetExtend.setExtend(param.getExtendSetting());
        infoCodeSetExtendService.saveOrUpdateWithObjectIdAndInfoCode(infoCodeSetExtend);

        // 调用 BDTP 服务触发指令下发
        TwcInstructionSend dto = new TwcInstructionSend();
        dto.setProjectId(infoCodeSetExtend.getProjectId());
        dto.setSystemId(infoCodeSetExtend.getSystemId());
        dto.setObjectId(infoCodeSetExtend.getObjId());
        List<PointInfoDTO> pointInfos = new ArrayList<>();
        PointInfoDTO pointInfoDTO = new PointInfoDTO();
        pointInfoDTO.setInfoCode(param.getInfoCode());
        pointInfoDTO.setValue(param.getBeforeValue());
        pointInfoDTO.setContent(param.getContent());
        pointInfoDTO.setExtendSetting(param.getExtendSetting());
        pointInfos.add(pointInfoDTO);
        dto.setPointInfos(pointInfos);
        dtpMeosServerClient.setOperatingParameters(dto);

        // 推送扩展配置消息 刷新页面
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        feedbackInfoMsgMqttVO.setSystemId(dto.getSystemId());
        feedbackInfoMsgMqttVO.setObjectId(dto.getObjectId());
        feedbackInfoMsgMqttVO.setProjectId(dto.getProjectId());
        feedbackInfoMsgMqttVO.setExtendSetting(param.getExtendSetting());
        feedbackInfoMsgMqttVO.setInfoCode(param.getInfoCode());
        feedbackInfoMsgMqttVO.setDataTime(DateUtils.getNowTimeString());
        feedbackInfoMsgMqttVO.setWebType(WebSocketTypeEnum.INFO_EXTEND_SET);
        
        iWSMessageService.pushMessageWeb(feedbackInfoMsgMqttVO);
    }
    
    @Override
    public void outsideCoolWaterReturnTempCfg(OutsideCoolWaterReturnTempCfgParam param) {
        
        //查询当前系统是什么类型
        List<DtpRwdObjectDTO> objects =
                dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(param.getSystemId()).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode));
        ParameterCheck.isTrue(CollectionUtils.isEmpty(objects), "系统不存在");
        DtpRwdObjectDTO object = objects.get(0);
        
        //先查询出 配置 然后只修改 出水温度
        String infoCode = InfoCodeEnum.COOL_WATER_RETURN_TEMP_SET.getCode();
        if (StringUtils.isNotEmpty(param.getInfoCode())) {
            infoCode = param.getInfoCode();
        }
        
        InfoCodeSetExtend cfg = infoCodeSetExtendService.getByObjectIdAndInfoCode(param.getSystemId(), infoCode);
        // 首次使用该功能时数据库可能没有扩展配置，需先补齐默认配置再保存温差。
        if (cfg == null) {
            cfg = new InfoCodeSetExtend();
        }
        cfg.setSystemId(param.getSystemId());
        cfg.setInfoCode(infoCode);
        
        ExtendSetting extend = cfg.getExtend();
        if (extend == null) {
            extend = new ExtendSetting();
        }
        // 历史数据或首次创建时可能没有手自动字段，默认按自动模式处理。
        if (extend.getManualAutoSet() == null) {
            extend.setManualAutoSet(ManualAutoSetEnum.AUTO);
        }
        extend.setHeatDissipationTemperatureDifference(param.getHeatDissipationTemperatureDifference());
        cfg.setExtend(extend);
            
        if (DtpRwdClassCodeEnum.COLD_SYSTEM.getValue().equals(object.getClassCode())) {
            CoolWaterReturnTempCfgParam cfgParam = new CoolWaterReturnTempCfgParam();
            cfgParam.setSystemId(cfg.getSystemId());
            cfgParam.setInfoCode(cfg.getInfoCode());
            cfgParam.setContent(param.getContent());
            cfgParam.setExtendSetting(extend);
                
            //调用设置接口设置
            coolWaterReturnTempCfg(cfgParam);
        } else {
            ExtendInfoCodeValueConfigParam cfgParam = new ExtendInfoCodeValueConfigParam();
            cfgParam.setSystemId(cfg.getSystemId());
            cfgParam.setClassCode(object.getClassCode());
            cfgParam.setInfoCode(cfg.getInfoCode());
            cfgParam.setContent(param.getContent());
            cfgParam.setExtendSetting(extend);
                
            extendInfoCodeValueConfig(cfgParam);
        }
    }
    
    @Override
    public void extendInfoCodeValueConfig(ExtendInfoCodeValueConfigParam param) {
        
        // 保存信息点设置扩展数据
        InfoCodeSetExtend infoCodeSetExtend = new InfoCodeSetExtend();
        infoCodeSetExtend.setProjectId(ContextUtil.getProjectId());
        infoCodeSetExtend.setSystemId(param.getSystemId());
        infoCodeSetExtend.setObjId(param.getSystemId());
        infoCodeSetExtend.setInfoCode(param.getInfoCode());
        infoCodeSetExtend.setExtend(param.getExtendSetting());
        infoCodeSetExtendService.saveOrUpdateWithObjectIdAndInfoCode(infoCodeSetExtend);
        
        // 调用 BDTP 服务触发指令下发
        TwcCommandSendDTO dto = new TwcCommandSendDTO();
        dto.setProjectId(ContextUtil.getProjectId());
        dto.setSystemId(param.getSystemId());
        dto.setObjectId(param.getSystemId());
        dto.setInfoCode(param.getInfoCode());
        dto.setInfoValue(param.getInfoValue());
        dto.setBeforeValue(param.getBeforeValue());
        dto.setContent(param.getContent());
        dto.setExtendSetting(param.getExtendSetting());
        dtpMeosServerClient.commandSend(dto);
        
        // 推送扩展配置消息 刷新页面
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        feedbackInfoMsgMqttVO.setSystemId(param.getSystemId());
        feedbackInfoMsgMqttVO.setObjectId(param.getSystemId());
        feedbackInfoMsgMqttVO.setClassCode(param.getClassCode());
        feedbackInfoMsgMqttVO.setProjectId(dto.getProjectId());
        feedbackInfoMsgMqttVO.setExtendSetting(param.getExtendSetting());
        feedbackInfoMsgMqttVO.setInfoCode(param.getInfoCode());
        feedbackInfoMsgMqttVO.setDataTime(DateUtils.getNowTimeString());
        //sse推送
        sseHandlerContext.getStrategy(SseEmitterEventEnum.INFO_EXTEND_SET)
                         .doOperation(ContextUtil.getProjectId(), feedbackInfoMsgMqttVO);
    }
}
