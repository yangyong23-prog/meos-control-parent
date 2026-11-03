package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseQueryParamDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryEqParamDTO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DtpRwdClientPageWrapper;
import com.persagy.apccontrol.basic.utils.DMPResultUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

/**
 * opt 分支 B10：按类查对象时加上 id 条件，只取需要的那部分。
 * <p>
 * 条件与 BizDtpRwdService#queryObjectList(groupCode, projectId, classCode, null, null, null, null) 相同（classCode、valid=1），
 * 另加 id IN (...)，500 个一批。用于“先查全项目某类对象，再按 id 取其中几个”的场景。
 */
@Component
public class OptRwdObjectsById {

    static final int BATCH = 500;

    private final DtpRwdClientPageWrapper dtpRwdClientPageWrapper;

    public OptRwdObjectsById(DtpRwdClientPageWrapper dtpRwdClientPageWrapper) {
        this.dtpRwdClientPageWrapper = dtpRwdClientPageWrapper;
    }

    public List<JSONObject> list(String groupCode, String projectId, List<String> classCodes, Collection<String> ids) {
        return batched(ids, part -> {
            QueryEqParamDTO criteria = new QueryEqParamDTO();
            criteria.setClassCode(classCodes);
            criteria.setValid(1);
            criteria.setId(part);
            BaseQueryParamDTO paramDTO = new BaseQueryParamDTO();
            paramDTO.setPage(new DtpRwdPageQuery().setPage(1).setSize(Integer.MAX_VALUE));
            paramDTO.setCriteria(criteria);
            DmpResult<List<JSONObject>> result = dtpRwdClientPageWrapper.queryObjectList(groupCode, projectId, paramDTO);
            DMPResultUtil.handleResult(result);
            return result.getData();
        });
    }

    /** id 去重（保持顺序）后按 BATCH 分批调用，结果按批次顺序拼接；空 id 不调用 */
    static List<JSONObject> batched(Collection<String> ids, Function<List<String>, List<JSONObject>> query) {
        List<JSONObject> out = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        for (List<String> part : Lists.partition(new ArrayList<>(new LinkedHashSet<>(ids)), BATCH)) {
            List<JSONObject> rows = query.apply(new ArrayList<>(part));
            if (rows != null) {
                out.addAll(rows);
            }
        }
        return out;
    }
}
