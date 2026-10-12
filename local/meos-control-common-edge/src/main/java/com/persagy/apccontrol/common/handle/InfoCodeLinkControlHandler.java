package com.persagy.apccontrol.common.handle;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.TriggerFormulaInfo;
import com.persagy.apccontrol.basic.model.enumtype.OnOffStateEnum;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.resource.param.RcBasicsConfigQueryDaoParam;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 当信息点之间有联动操作的时候，会在这里实现
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/13 18:19
 */
@Slf4j
@Component
public class InfoCodeLinkControlHandler {

    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private RcBasicsConfigService rcBasicsConfigService;

    /**
     * 当切换为单点控制时，需要调用
     * 重置当前对象、信息点对应的扩展信息中需要计算的IOT数据值，这里直接走删除逻辑
     *
     * @param objectId
     * @param classCode
     * @param infoCode
     * @return
     */
    public boolean resetObjectTriggerInfoCodeValue(String systemId, String objectId, String classCode, String infoCode) {
        Object o = this.redisCacheUtil.hGet(RedisKey.buildInfoCodeConfigTriggerHashKey(classCode), infoCode);
        TriggerFormulaInfo triggerFormulaInfo = o == null ? null : JsonUtils.fromJson(JsonUtils.toJson(o), new TypeReference<TriggerFormulaInfo>() {
        });
        if (triggerFormulaInfo == null || StrUtil.isBlank(triggerFormulaInfo.getTriggerFormula()) || StrUtil.isBlank(triggerFormulaInfo.getTriggerInfoCode())) {
            return true;    // 无配置，或者触发表达式、上报信息点不存在的，直接返回
        }
        return redisCacheUtil.del(RedisKey.buildIotRealKey(objectId, triggerFormulaInfo.getTriggerInfoCode()));
    }

    /**
     * 当切换为自动控制时，需要调用--暂时无用
     * 重置当前对象、信息点对应的强控模式的缓存值，这里直接走删除逻辑
     *
     * @param autoObjectIdMap key：objectId，value：classCode
     * @return
     */
    public void resetObjectInfoCodeForcedValue(Map<String, String> autoObjectIdMap) {
        if (CollectionUtil.isEmpty(autoObjectIdMap)) {
            return;
        }
        for (Map.Entry<String, String> stringStringEntry : autoObjectIdMap.entrySet()) {
            String objectId = stringStringEntry.getKey();
            String classCode = stringStringEntry.getValue();
            this.resetObjectInfoCodeForcedValue(objectId, classCode);
        }
    }

    /**
     * 当切换为自动控制时，需要调用
     * 重置当前对象、信息点对应的强控模式的缓存值，这里直接走删除逻辑
     *
     * @param objectId
     * @param classCode
     * @return
     */
    public boolean resetObjectInfoCodeForcedValue(String objectId, String classCode) {
        if ("ACCCCC".equals(classCode)) {
            // 当为离心式冷水机组
            return this.redisCacheUtil.del(RedisKey.buildObjectInfoCodeForcedKey(objectId, "enableCtrl"));
        }
        if ("ACCCSC".equals(classCode)) {
            // 当为离心式冷水机组
            return this.redisCacheUtil.del(RedisKey.buildObjectInfoCodeForcedKey(objectId, "enableCtrl"));
        }
        if ("ACOTU".equals(classCode)) {
            // 当为冷却塔控制单元的时候
            return this.redisCacheUtil.del(RedisKey.buildObjectInfoCodeForcedKey(objectId, "enableCtrl"));
        }
        if ("ACGEHE".equals(classCode)) {
            // 当为空调换热器的时候
            return this.redisCacheUtil.del(RedisKey.buildObjectInfoCodeForcedKey(objectId, "equipSwitchSet"));
        }
        if ("ACAHC".equals(classCode)) {
            // 当空气源热泵设备组为的时候
            return this.redisCacheUtil.del(RedisKey.buildObjectInfoCodeForcedKey(objectId, "enableCtrl"));
        }
        return false;
    }

    /**
     * 判断当前设备、信息点是否存在扩展信息
     *
     * @param objectId
     * @param infoCodeCfg
     * @return
     */
    public TriggerFormulaInfo checkTriggerFormulaInfo(String objectId, RcInfoCodeCfg infoCodeCfg) {
        // 1.判断当前的IOT数据是否存在扩展信息点的计算配置
        if (infoCodeCfg == null || infoCodeCfg.getExtendInfo() == null || infoCodeCfg.getExtendInfo().getTriggerFormulaInfo() == null) {
            ControlLogUtil.debug(
                    "[rcRunStatus-配置检查] 设备:{} | 信息点:{} | 结果:跳过 | 原因:无triggerFormulaInfo配置",
                    objectId,
                    infoCodeCfg == null ? "null" : infoCodeCfg.getInfoCode());
            return null;    // 无配置，或者触发表达式、上报信息点不存在的，不需要处理计算
        }

        TriggerFormulaInfo triggerFormulaInfo = infoCodeCfg.getExtendInfo().getTriggerFormulaInfo();

        // 离心式冷水机组、冷却塔控制单元、的需要单独处理下
        if ("ACCCCC".equals(infoCodeCfg.getClassCode())) {
            // 当为离心式冷水机组，需判断联动-单点控制模式
            String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
            if (("1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("enableCtrl"))
                    || (!"1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("equipSwitchSet"))) {
                // 当为单点控制的时候，enableCtrl 信息点不在参与计算，仅计算 equipSwitchSet 信息点的值
                // 当为联动控制的时候，equipSwitchSet 信息点不在参与计算，仅计算 enableCtrl 信息点的值
                ControlLogUtil.debug(
                        "[rcRunStatus-配置检查] 设备:{} | 类型:离心式冷水机组 | enableCtrl={} | 结果:跳过 | 原因:单点联动模式不匹配",
                        objectId, enableCtrl);
                return null;
            }
        } else if ("ACCCSC".equals(infoCodeCfg.getClassCode())) {
            // 当为冷却塔控制单元，需判断联动-单点控制模式
            String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
            if (("1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("enableCtrl"))
                    || (!"1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("equipSwitchSet"))) {
                // 当为单点控制的时候，enableCtrl 信息点不在参与计算，仅计算 equipSwitchSet 信息点的值
                // 当为联动控制的时候，equipSwitchSet 信息点不在参与计算，仅计算 enableCtrl 信息点的值
                ControlLogUtil.debug(
                        "[rcRunStatus-配置检查] 设备:{} | 类型:冷却塔控制单元 | enableCtrl={} | 结果:跳过 | 原因:单点联动模式不匹配",
                        objectId, enableCtrl);
                return null;
            }
        } else if ("ACOTU".equals(infoCodeCfg.getClassCode())) {
            // 当为冷却塔控制单元的时候，单点控制，不在进行计算
            String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
            if ("1".equals(enableCtrl)) {
                ControlLogUtil.debug(
                        "[rcRunStatus-配置检查] 设备:{} | 类型:冷却塔 | enableCtrl={} | 结果:跳过 | 原因:单点控制模式",
                        objectId, enableCtrl);
                return null;
            }
        } else if ("ACGEHE".equals(infoCodeCfg.getClassCode())) {
            // 当为空调换热器的时候，单点控制，不在进行计算
            String enableCtrl = getEnableCtrl(objectId, "equipSwitchSet");
            if ("1".equals(enableCtrl)) {
                ControlLogUtil.debug(
                        "[rcRunStatus-配置检查] 设备:{} | 类型:空调换热器 | equipSwitchSet={} | 结果:跳过 | 原因:单点控制模式",
                        objectId, enableCtrl);
                return null;
            }
        }

        // 通过检查
        ControlLogUtil.info(
                "[rcRunStatus-配置检查] 设备:{} | 信息点:{} | 结果:通过 | 触发信息点:{} | sourceIotType:{}",
                objectId,
                infoCodeCfg.getInfoCode(),
                triggerFormulaInfo.getTriggerInfoCode(),
                triggerFormulaInfo.getSourceIotType());

        return triggerFormulaInfo;
    }

    private String getEnableCtrl(String objectId, String infoCode) {
        String enableCtrl = this.redisCacheUtil.get(RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCode));
        // 如果没有查询到 尝试查询空调冷源的配置
        if (StringUtils.isEmpty(enableCtrl)) {
            RcBasicsConfigQueryDaoParam rcBasicsConfigQueryDaoParam = new RcBasicsConfigQueryDaoParam();
            rcBasicsConfigQueryDaoParam.setProjectId(ContextUtil.getProjectId());
            rcBasicsConfigQueryDaoParam.setFacilityCode(objectId);
            rcBasicsConfigQueryDaoParam.setType(BasicsConfigTypeEnum.FORCE_CONTROL_STATUS);
            RcBasicsConfig cfg = rcBasicsConfigService.getValid(rcBasicsConfigQueryDaoParam);
            if (cfg != null && OnOffStateEnum.OPEN.getValue().equals(cfg.getResult())) {
                enableCtrl = "1";
            }
        }
        return enableCtrl;
    }

}
