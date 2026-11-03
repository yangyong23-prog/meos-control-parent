package com.persagy.apccontrol.common.outside.twin;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoBatchSendDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.utils.BeanUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.opt.OptRealtimeTwinReader;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 调用物理世界
 * @author: yancheng
 * @date: 2023/4/1
 * @version: V1.0
 */
@Slf4j
@Service
public class DtpTwinClientServiceImpl implements DtpTwinClientService, BaseFeignClientService {

    private static final String LOG_TAG = "孪生服务";

    @Resource
    private ProjectCfg projectCfg;

    @Resource
    private DtpTwinClient dtpTwinClient;

    /** opt 分支 B2（switch.opt.realtimeCacheRead）：当前值查询先读共享缓存 */
    @Resource
    private OptRealtimeTwinReader optRealtimeTwinReader;

    /**
     * @author wxf
     * @description 用于统一记录IOT数据供前端计算过期时间
     * @date 16:00 2024/7/24
     * @param null
     * @return null
     **/
    private final Map<Object, List<DtpTwinIotData>> twinDataOnMomentMap = new ConcurrentHashMap<>();

    /**
     * 验证远程调用结果
     *
     * @param result
     */
    public static void handleResult(DtpTwinR<Object> result) {
        boolean success = result.getIsSuccess();
        if (!success) {
            log.error("调用数据中台报错-{}", result);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), result.getRespMsg() == null ? LocaleMessageUtil.getMessage("key_92cd29bb9b114e049ec56c1141b79bbd") : result.getRespMsg());
        }
    }

    @Override
    public List<DtpTwinIotData> queryTwinDataOnMoment(String groupCode, String projectId, String userId, QueryTwinDataDTO queryTwinDataDTO) {
        if (optRealtimeTwinReader.handles(queryTwinDataDTO)) {
            List<DtpTwinIotData> content = optRealtimeTwinReader.query(queryTwinDataDTO,
                    dto -> queryTwinDataOnMomentFromTwin(groupCode, projectId, userId, dto));
            handleTwinDataOnMoment(content);
            return content;
        }
        try {
            // log.info("查看当前反馈孪生:"+ JSONObject.toJSONString(queryTwinDataDTO));
            DtpTwinR<DtpTwinIotData> dtpTwinIotDataDtpTwinR = dtpTwinClient.queryTwinDataOnMoment(groupCode, projectId, userId, queryTwinDataDTO);
            if (dtpTwinIotDataDtpTwinR != null && dtpTwinIotDataDtpTwinR.getIsSuccess()) {
                List<DtpTwinIotData> content = dtpTwinIotDataDtpTwinR.getContent();
                handleTwinDataOnMoment(content);
                return content;
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_0f41958a07e54e9d8e64a0a967358e71"));
        }
        return Lists.newArrayList();
    }

    /**
     * opt 分支 B2：与 queryTwinDataOnMoment 原逻辑相同、但不做前端过期提示记录的 Twin 调用，供缓存回源和双读校验使用。
     */
    private List<DtpTwinIotData> queryTwinDataOnMomentFromTwin(String groupCode, String projectId, String userId, QueryTwinDataDTO queryTwinDataDTO) {
        try {
            DtpTwinR<DtpTwinIotData> r = dtpTwinClient.queryTwinDataOnMoment(groupCode, projectId, userId, queryTwinDataDTO);
            if (r != null && r.getIsSuccess()) {
                return r.getContent() == null ? Lists.newArrayList() : r.getContent();
            }
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_0f41958a07e54e9d8e64a0a967358e71"));
        }
        return Lists.newArrayList();
    }


    @Override
    public void manualSend(DtpTwinManualSend dtpTwinManualSend) {
        DtpTwinR<Object> result = dtpTwinClient.send(projectCfg.getProjectId(), projectCfg.getGroupCode(), ContextUtil.getUserId(), dtpTwinManualSend);
        handleResult(result);
    }

    @Override
    public void autoSend(DtpTwinAutoSendDTO dtpTwinAutoSendDTO) {
        DtpTwinR<Object> result = dtpTwinClient.autoSend(projectCfg.getGroupCode(), projectCfg.getProjectId(), ContextUtil.getUserId(), dtpTwinAutoSendDTO);
        handleResult(result);
    }

    @Override
    public void autoCommandBatchSend(@NonNull List<DtpTwinAutoBatchSendDTO> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        insert(false, LocaleMessageUtil.getMessage("key_b29f91f5b39b4e6aa296018c3e1e35fa"), params, () -> dtpTwinClient.autoCommandBatchSend(params));
    }

    @Override
    public void autoSendBatch(List<DtpTwinAutoSendDTO> dtpTwinAutoSendDTOList) {

        // 转成为批量对象
        List<DtpTwinAutoBatchSendDTO> date = BeanUtil.listCopyProperties(dtpTwinAutoSendDTOList, DtpTwinAutoBatchSendDTO.class);

        DtpTwinR<Object> result = dtpTwinClient.autoSendBatch(projectCfg.getGroupCode(), projectCfg.getProjectId(), ContextUtil.getUserId(), date);
        handleResult(result);
    }

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> ventilationEquipStatistics(@NonNull Map<String, Set<String>> param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_2cd3ee16e41f4b0bb6511e663d83dd46"), param, () ->
                dtpTwinClient.ventilationEquipStatistics(commonParam, param), Collections::emptyMap);
    }

    @Override
    public void switchEquipLock(String projectId, String groupCode, String objectId, Integer lock) {
        DtpTwinR<Object> result = dtpTwinClient.switchEquipLock(projectId, groupCode, objectId, lock);
        handleResult(result);
    }

    /**
     * 指令下发
     *
     * @param objectId
     * @param infoCode
     * @param iotValue
     * @param infoCodeCfg
     * @return
     */
    @Override
    public String setOperatingParameters(String objectId, String infoCode, String iotValue, RcInfoCodeCfg infoCodeCfg) {
        if (!StrUtil.isAllNotBlank(infoCode, iotValue)) {
            log.warn("控制指令下发objectId:{}  infoCode:{} iotValue:{} 不能为空", objectId, infoCode, iotValue);
            return null;
        }
        try {
            if (infoCodeCfg != null) {
                BigDecimal value = new BigDecimal(iotValue);
                value = infoCodeCfg.getScale() == null ? value : value.divide(infoCodeCfg.getScale(), infoCodeCfg.getIotDecimal() == null ? 2 : infoCodeCfg.getIotDecimal(), BigDecimal.ROUND_HALF_UP);
                iotValue = value.toPlainString();
            }

            log.info("控制指令下发开始入参：" + "projectId:" + projectCfg.getProjectId() + "objectId:" + objectId + "infoCode:" + infoCode + "value:" + iotValue);
            this.manualSend(DtpTwinManualSend.builder().objectId(objectId).infoCode(infoCode).value(iotValue).build());
            log.info("控制指令下发结束出参无异常");
            return iotValue;
        } catch (Exception e) {
            log.error("控制指令下发失败，对象ID：{}，信息点：{}，IOT值：{}", objectId, infoCode, iotValue, e);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), e.getMessage());
        }
    }

    @Override
    public void saveList(List<JSONObject> paramList) {
        this.dtpTwinClient.saveList(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void saveClassConfs(List<JSONObject> paramList) {
        this.dtpTwinClient.saveClassConfs(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void deleteByClassConf(List<JSONObject> paramList) {
        this.dtpTwinClient.deleteByClassConf(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void saveExclusions(List<JSONObject> paramList) {
        this.dtpTwinClient.saveExclusions(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void deleteByConf(List<JSONObject> paramList) {
        this.dtpTwinClient.deleteByConf(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void saveStartIntervals(List<JSONObject> paramList) {
        this.dtpTwinClient.saveStartIntervals(projectCfg.getGroupCode(), projectCfg.getProjectId(), paramList);
    }

    @Override
    public void reviseObjectInSys(JSONObject param) {
        this.dtpTwinClient.reviseObjectInSys(projectCfg.getGroupCode(), projectCfg.getProjectId(), param);
    }


    @Override
    public void resetEquipInfoCodeInSystem(String objectId, String classCode, String infoCode) {
        JSONObject param = new JSONObject();
        param.put("objectId", objectId);
        param.put("classCode", classCode);
        param.put("infoCode", infoCode);
        DtpTwinR<Object> result = this.dtpTwinClient.resetEquipInfoCodeInSystem(projectCfg.getGroupCode(), projectCfg.getProjectId(), param);
        handleResult(result);
    }

    @Override
    public void resetEquipInfoCodeInGroup(String objectId, String classCode, String infoCode) {
        JSONObject param = new JSONObject();
        param.put("objectId", objectId);
        param.put("classCode", classCode);
        param.put("infoCode", infoCode);
        DtpTwinR<Object> result = this.dtpTwinClient.resetEquipInfoCodeInGroup(projectCfg.getGroupCode(), projectCfg.getProjectId(), param);
        handleResult(result);
    }

    /**
     * @param content
     * @author wxf
     * @description 统一返回IOT数据用于前端计算过期时间
     * @date 13:40 2024/7/24
     **/
    private void handleTwinDataOnMoment(List<DtpTwinIotData> content) {
        if (content.isEmpty()) {
            return;
        }

        /*
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtil.isNull(servletRequestAttributes))
            return;

         */

        HttpServletRequest request = LocalContextStorage.request();
        if (ObjectUtil.isNull(request)) {
            return;
        }

        List<DtpTwinIotData> dtpTwinIotData = this.twinDataOnMomentMap.get(request);
        if (dtpTwinIotData == null) {
            dtpTwinIotData = new ArrayList<>();
        }

        CollectionUtil.addAll(dtpTwinIotData, DtpTwinIotData.getVaildIotDatas(content));
        this.twinDataOnMomentMap.put(request, dtpTwinIotData);
    }

    public List<DtpTwinIotData> getTwinDataOnMoment(@lombok.NonNull HttpServletRequest request) {
        return this.twinDataOnMomentMap.get(request);
    }

    public void removeTwinDataOnMoment(@lombok.NonNull HttpServletRequest request) {
        this.twinDataOnMomentMap.remove(request);
    }


    @Override
    public String logTag() {
        return "孪生服务";
    }
}
