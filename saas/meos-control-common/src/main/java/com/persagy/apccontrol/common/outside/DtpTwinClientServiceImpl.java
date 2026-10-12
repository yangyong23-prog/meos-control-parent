package com.persagy.apccontrol.common.outside;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Suppliers;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoBatchSendDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 孪生服务接口
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class DtpTwinClientServiceImpl implements DtpTwinClientService {

    private static final Supplier<ApplicationException> NOT_SUPPORT_EXCEPTION = Suppliers.memoize(() ->
            new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_e505ebb23f8547309105a480e56ab8af")));

    @Override
    public List<DtpTwinIotData> queryTwinDataOnMoment(String groupCode, String projectId, String userId, QueryTwinDataDTO queryTwinDataDTO) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void manualSend(DtpTwinManualSend dtpTwinManualSend) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void autoSend(DtpTwinAutoSendDTO dtpTwinAutoSendDTO) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void autoCommandBatchSend(@NonNull List<DtpTwinAutoBatchSendDTO> params) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void autoSendBatch(List<DtpTwinAutoSendDTO> dtpTwinAutoSendDTOList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> ventilationEquipStatistics(@NonNull Map<String, Set<String>> param) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void switchEquipLock(String projectId, String groupCode, String objectId, Integer lock) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public String setOperatingParameters(String objectId, String infoCode, String iotValue, RcInfoCodeCfg infoCodeCfg) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void saveList(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void saveClassConfs(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void deleteByClassConf(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void saveExclusions(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }



    @Override
    public void deleteByConf(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void saveStartIntervals(List<JSONObject> paramList) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void reviseObjectInSys(JSONObject param) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void resetEquipInfoCodeInSystem(String objectId, String classCode, String infoCode) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void resetEquipInfoCodeInGroup(String objectId, String classCode, String infoCode) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public List<DtpTwinIotData> getTwinDataOnMoment(HttpServletRequest request) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void removeTwinDataOnMoment(HttpServletRequest request) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }
}
