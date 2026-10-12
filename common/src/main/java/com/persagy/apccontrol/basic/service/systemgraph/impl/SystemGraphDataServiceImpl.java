package com.persagy.apccontrol.basic.service.systemgraph.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.mapper.systemgraph.SystemGraphDataMapper;
import com.persagy.apccontrol.basic.model.dto.systemgraph.UpdateGraphDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphData;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumUpdateGraphType;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import com.persagy.apccontrol.basic.service.systemgraph.PipelineFlowRulesMerger;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphDataService;
import com.persagy.apccontrol.basic.utils.CompressUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author persagy
 * @description 针对表【system_graph_data(系统图数据表)】的数据库操作Service实现
 * @createDate 2024-06-25 11:50:39
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphDataServiceImpl extends ServiceImpl<SystemGraphDataMapper, SystemGraphData>
        implements SystemGraphDataService, LogComponent {

    private final RedissonClient redissonClient;

    /**
     * 编辑系统图
     *
     * @param systemGraphDataId 系统图数据id
     * @param list              更新列表
     * @return {@link Boolean }
     */
    @Override
    public Boolean editSystemGraph(Long systemGraphDataId, List<UpdateGraphDTO> list) {
        RLock updateSystemGraphDataLock = redissonClient.getLock("updateSystemGraphDataLock");
        try {
            if (updateSystemGraphDataLock.tryLock(10, TimeUnit.SECONDS)) {
                SystemGraphData data = this.getById(systemGraphDataId);
                if (data == null) {
                    throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_7dcfcf0eb6274a16822d316ea2af3240"), LocaleMessageUtil.getMessage("key_7dcfcf0eb6274a16822d316ea2af3240"));
                }
                String graphData = CompressUtil.decompress(data.getData());
                JSONObject graphDataObject = JSON.parseObject(graphData);
                for (UpdateGraphDTO dto : list) {
                    EnumUpdateGraphType type = dto.getType();
                    logInstance().debug("更新关系图数据类型", Pair.of("类型", type.getDesc()));
                    switch (type) {
                        case EQ:
                            JSONArray equipmentNodeVos = graphDataObject.getJSONArray("equipmentNodeVos");
                            graphDataObject.put("equipmentNodeVos", updateGraphDataContent(equipmentNodeVos, dto));
                            break;
                        case PASSBY:
                            JSONArray passByVos = graphDataObject.getJSONArray("passByVos");
                            graphDataObject.put("passByVos", updateGraphDataContent(passByVos, dto));
                            break;
                        case GROUP:
                            JSONArray groupVos = graphDataObject.getJSONArray("groupVos");
                            graphDataObject.put("groupVos", updateGraphDataContent(groupVos, dto));
                            break;
                        case CUSTOMIZE:
                            // customizeContent 按 code 平级合并，不同自定义数据互不覆盖
                            JSONObject customize = graphDataObject.getJSONObject("customizeContent");
                            if (customize == null) {
                                customize = new JSONObject();
                            }
                            if (PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY.equals(dto.getCode())) {
                                JSONObject mergedRules = PipelineFlowRulesMerger.merge(
                                        customize.getJSONObject(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY), dto.getData());
                                if (mergedRules != null) {
                                    customize.put(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY, mergedRules);
                                } else {
                                    customize.remove(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY);
                                }
                            } else {
                                customize.put(dto.getCode(), dto.getData());
                            }
                            graphDataObject.put("customizeContent", customize);
                            break;
                        default:
                            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_5c99a0782dd74a3c89047aa09126b857"), LocaleMessageUtil.getMessage("key_ebf7c774868c4fdbaa0d003002a42fc7"));
                    }
                }
                graphDataObject.put("isUpdateDetail", true);
                data.setData(CompressUtil.compress(graphDataObject.toJSONString()));
                return updateById(data);
            }
        } catch (Exception e) {
            logInstance().error("更新关系图数据异常", e);
            return false;
        } finally {
            updateSystemGraphDataLock.unlock();
        }
        return false;
    }

    private JSONArray updateGraphDataContent(JSONArray nodes, UpdateGraphDTO dto) {
        List<JSONObject> list = nodes.toJavaList(JSONObject.class);
        JSONArray result = new JSONArray();
        String objectId = dto.getObjectId();
        Object data = dto.getData();
        String code = dto.getCode();
        for (JSONObject node : list) {
            result.add(node);
            if ("packNode".equals(node.getString("compType"))) {
                continue;
            }
            String dataObjectId = node.getString("dataObjectId");
            if (!objectId.equals(dataObjectId)) {
                continue;
            }
            logInstance().debug("更新关系图数据", Arrays.asList(Pair.of("对象id", objectId), Pair.of("编码", code), Pair.of("数据", data)));
            node.put(code, data);
            logInstance().debug("更新关系图数据结果", Pair.of("结果", node));
        }
        return result;
    }

    @Override
    public SystemGraphData saveSystemGraphData(String requestParam, SystemGraphInstanceVo vo, EnumSystemGraphStatus status) {
        SystemGraphData data = new SystemGraphData();
        data.setData(CompressUtil.compress(JSON.toJSONString(vo)));
        data.setProjectId(ContextUtil.getProjectId());
        data.setStatus(status);
        data.setRequestParam(requestParam);
        this.save(data);
        return data;
    }

    @Override
    public Boolean updateSystemGraphDataStatusById(Long systemGraphDataId, EnumSystemGraphStatus status) {
        SystemGraphData systemGraphData = new SystemGraphData();
        systemGraphData.setId(systemGraphDataId);
        systemGraphData.setStatus(status);
        return updateById(systemGraphData);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图数据service";
    }
}




