package com.persagy.apccontrol.basic.outside.feign.wrapper;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseQueryParamDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdQueryPointListDTO;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.outside.dto.*;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRwdClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCriteriaQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.*;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 物理世界服务包装器
 *
 * @author JiYinchuan
 * @since 5.240901.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpRwdClientPageWrapper implements DtpRwdClient {

    private static final ApplicationException DEFAULT_EXCEPTION = new ApplicationException(
            RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_ca45e189960e4a499252636fe1acf3ee"));

    private final DtpRwdConvert dtpRwdConvert;
    private final DtpRwdClient dtpRwdClient;

    @Override
    public CustomerResponse<List<DtpRwdObjectResponse>> listObject(
            DtpRwdCommonParam param, DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> body) {
        return FeignPageExecutor.execute(body.getPage(), pageQuery -> {
            DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> copy = dtpRwdConvert.copy(body);
            copy.setPage(new DtpRwdPageQuery().fillByPageQuery(pageQuery));
            CustomerResponse<List<DtpRwdObjectResponse>> object = dtpRwdClient.listObject(param, copy);
            return object;
        });
    }

    @Override
    public CustomerResponse<List<DtpRwdObjectResponse>> listSpacesByRoomFuncType(
            DtpRwdCommonParam param, DtpRwdSpaceByRoomFuncTypeQueryParam body) {
        return FeignPageExecutor.execute(body.getPage(), pageQuery -> {
            DtpRwdSpaceByRoomFuncTypeQueryParam copy = dtpRwdConvert.copy(body);
            copy.setPage(new DtpRwdPageQuery().fillByPageQuery(pageQuery));
            return dtpRwdClient.listSpacesByRoomFuncType(param, copy);
        });
    }

    @Override
    public CustomerResponse<List<DtpRwdRelationResponse>> listRelationObject(
            DtpRwdCommonParam param, DtpRwdRelationQueryParam body) {
        return FeignPageExecutor.execute(body.getPage(), pageQuery -> {
            DtpRwdRelationQueryParam copy = dtpRwdConvert.copy(body);
            copy.setPage(new DtpRwdPageQuery().fillByPageQuery(pageQuery));
            return dtpRwdClient.listRelationObject(param, copy);
        });
    }

    @Override
    public CustomerResponse<ObjectInstantiateResponse> getInstantiate(
            ObjectInstantiateQueryParam param) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<ObjectInstantiateResponse>> listInstantiate(
            DtpRwdCommonParam param, ObjectInstantiateBatchQueryParam body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<DtpRwdTwinObjectRuleByIdResponse>> listTwinObjectRuleById(
            DtpRwdCommonParam param, DtpRwdTwinObjectRuleByIdQueryParam body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<DtpRwdTwinObjectRuleResponse>> listTwinObjectRule(
            DtpRwdCommonParam param, Collection<DtpRwdTwinObjectRuleQueryParam> body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<DtpRwdTwinControlObjectResponse>> listTwinControlObjects(
            DtpRwdCommonParam param, Collection<String> classCodes) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<List<JSONObject>> queryObjectList(
            String groupCode, String projectId, BaseQueryParamDTO body) {
        return FeignPageExecutor.executeToDmpResult(body.getPage(), pageQuery -> {
            BaseQueryParamDTO copy = dtpRwdConvert.copy(body);
            copy.setPage(new DtpRwdPageQuery().fillByPageQuery(pageQuery));
            return dtpRwdClient.queryObjectList(groupCode, projectId, copy);
        });
    }

    @Override
    public DmpResult<ArrayNode> query(
            String groupCode, String projectId,
            String appId, String userId, BaseQueryParamDTO body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<List<JSONObject>> queryRelation(
            String groupCode, String projectId,
            String appId, String userId, DtpRwdRelationQueryParam body) {
        return FeignPageExecutor.executeToDmpResult(body.getPage(), pageQuery -> {
            DtpRwdRelationQueryParam copy = dtpRwdConvert.copy(body);
            copy.setPage(new DtpRwdPageQuery().fillByPageQuery(pageQuery));
            return dtpRwdClient.queryRelation(groupCode, projectId, appId, userId, copy);
        });
    }

    @Override
    public DmpResult<List<JSONObject>> queryObjectListByGraphCodeAndRelCode(
            String groupCode, String projectId,
            String appId, String userId, Object criteria) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<List<ObjectsExistRule>> queryObjectsOfExistRule(
            String groupCode, String projectId,
            String userId, Object criteria) {
        throw DEFAULT_EXCEPTION;
    }

    /**
     * 查找持久化有值的对象
     *
     * @param param 参数
     * @param body  查询参数
     * @return {@code CustomerResponse<List<ObjectsExistRule>> }
     * @since 7.250801.0
     */
    @Override
    public CustomerResponse<List<ObjectsExistRule>> listObjectsOfExistRule(DtpRwdCommonParam param, Collection<DtpRwdObjectsOfExistRuleQueryParam> body) {
        throw  DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<List<ObjectsExistRule>> queryObjectsExistRule(
            String groupCode, String projectId,
            String userId, List<ClassCode> criteria) {
        throw DEFAULT_EXCEPTION;
    }

    /**
     * 查询孪生对象接口
     *
     * @param param 参数
     * @param body  查询条件
     * @return {@code CustomerResponse<List<ObjectsExistRule>> }
     * @since 7.250801.0
     */
    @Override
    public CustomerResponse<List<ObjectsExistRule>> listTwinObjectsOfExistRule(DtpRwdCommonParam param, Collection<DtpRwdObjectsOfExistRuleQueryParam> body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<AcccccGroupDTO> queryAcccccGroupBySystemId(
            String groupCode, String projectId,
            String systemId, String appId, String userId) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<JSONObject> querySysGroupBySystemId(String groupCode, String projectId, String systemId, String appId, String userId) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<List<ControlObjectDto>> queryControlObjects(
            String groupCode, String projectId, String[] classCodeList) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<List<DtpRwdObjectResponse>>> queryClassCodeFromIds(
            RwdRouteFromIdsParam rwdRouteParam) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<KitchenExhaustResponse>> queryKitchenExhaust(DtpRwdCommonParam param) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<DtpRwdFractionalPrecisionRuleDTO>> queryInfoPointPeroid(DtpRwdCommonParam param, Collection<String> objects) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<List<DtpRwdQueryPointListDTO>> queryPointList(DtpRwdCommonParam param, JSONObject criteria) {
        PageQuery initPageQuery = new PageQuery(criteria.getInteger("page"), criteria.getInteger("size"));
        return FeignPageExecutor.execute(initPageQuery, pageQuery -> {
            if (Objects.nonNull(pageQuery)) {
                criteria.fluentPut("page", pageQuery.getPageNo())
                        .fluentPut("size", pageQuery.getPageSize());
            }
            return dtpRwdClient.queryPointList(param, criteria);
        });
    }

    @Override
    public CustomerResponse<Map<String, Map<String, List<String>>>> querySystemGroup(
            String groupCode, String projectId, String systemId, String appId, String userId) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public CustomerResponse<Map<String, Map<String, List<String>>>> queryPumpGroups(DtpRwdCommonParam param, Set<String> body) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<Map<ChillerTypeSub, Map<String, List<String>>>> querySysGroup(
            String groupCode, String projectId, String systemId, String appId, String userId) {
        throw DEFAULT_EXCEPTION;
    }

    @Override
    public DmpResult<Map<String, List<String>>> queryPumpGroup(
            String groupCode, String projectId, String pumpGroupId, String appId, String userId) {
        throw DEFAULT_EXCEPTION;
    }
}
