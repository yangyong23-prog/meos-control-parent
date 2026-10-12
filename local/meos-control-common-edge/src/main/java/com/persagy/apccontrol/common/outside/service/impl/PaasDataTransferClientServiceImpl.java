package com.persagy.apccontrol.common.outside.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.dto.datatransfer.PaasDataTransferKafkaOffsetInfoDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.structure.datatransfer.converter.PaasDataTransferConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.model.vo.PaasDataCacheVo;
import com.persagy.apccontrol.common.outside.feign.PaasDatatransferClient;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 数据同步服务客户端服务
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaasDataTransferClientServiceImpl implements PaasDataTransferClientService, BaseFeignClientService {

    private final ObjectMapper objectMapper;
    private final PaasDatatransferClient paasDatatransferClient;
    private final PaasDataTransferConverter paasDataTransferConverter;

    @Value("${persagy.db_name.meos_control:meos_control}")
    private String schemaName;

    @Override
    public boolean cloudNetHealthy() {
        return BooleanUtils.isTrue(query(true, LocaleMessageUtil.getMessage("key_bbb3909a3af449959b6461756a739bde"), "",
                paasDatatransferClient::cloudNetHealthy, null));
    }

    @Override
    public List<PaasDataTransferKafkaOffsetInfoDTO> listKafkaOffsets() {
        String businessRemark = "meos";
        return query(false, LocaleMessageUtil.getMessage("key_bbb3909a3af449959b6461756a739bde"), businessRemark,
                () -> paasDatatransferClient.listKafkaOffsets(businessRemark), ArrayList::new).stream()
                .map(paasDataTransferConverter::toDto).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public <T extends BaseAssignIdDO> void syncDbDataToCloud(
            @NonNull List<@NonNull PaasDataTransferParam<@NonNull T>> params) {
        String tenant = ContextUtil.getTenant();
        String schema = StringUtils.isNotBlank(tenant) ? (tenant + "_" + schemaName) : schemaName;
        Map<DataTypeEnum, List<PaasDataTransferParam<T>>> dataTypeAndParams = params.stream().collect(
                Collectors.groupingBy(PaasDataTransferParam::getDataType));
        List<PaasDataCacheVo> body = new ArrayList<>();
        for (Map.Entry<DataTypeEnum, List<PaasDataTransferParam<T>>> entry : dataTypeAndParams.entrySet()) {
            DataTypeEnum dataType = entry.getKey();
            for (PaasDataTransferParam<T> param : entry.getValue()) {
                for (T data : param.getData()) {
                    TableInfo tableInfo = TableInfoHelper.getTableInfo(data.getClass());
                    String jsonColumns = tableInfo.getFieldList().stream()
                            .filter(item -> Objects.nonNull(item.getTypeHandler())
                                    && ClassUtils.isAssignable(item.getTypeHandler(), AbstractJsonTypeHandler.class))
                            .map(TableFieldInfo::getColumn)
                            .collect(Collectors.joining(","));
                    Map<String, String> propertyAndColumn = analyzePropertyAndColumn(tableInfo);
                    JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(data));
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    jsonNode.fields().forEachRemaining(item -> objectNode.set(
                            propertyAndColumn.get(item.getKey()), item.getValue()));
                    String tableName = tableInfo.getTableName();
                    PaasDataCacheVo.PaasDataCacheKey key = new PaasDataCacheVo.PaasDataCacheKey();
                    key.setPaas_table(tableName);
                    key.setPaas_schema(schema);
                    if (StringUtils.isNotBlank(jsonColumns)) {
                        key.setJson_columns(jsonColumns);
                    }
                    key.setId(String.valueOf(data.getId()));
                    String value = dataType != DataTypeEnum.DELETE ? objectNode.toString() : null;
                    body.add(new PaasDataCacheVo(key.toJsonString(), value));
                }
            }
        }
        List<List<PaasDataCacheVo>> partitions = Lists.partition(body, PaasDataCacheVo.MAX_LIMIT);
        for (List<PaasDataCacheVo> partition : partitions) {
            paasDatatransferClient.sendDataToKafka(partition);
        }
    }

    @Override
    public void syncDbDataToCloud(
            @NonNull String tableName,
            @Nullable Set<@NonNull String> jsonColumns,
            @NonNull DataTypeEnum dataType,
            @NonNull ObjectNode param) {
        convertCamelToUnderscore(param);
        String tenant = ContextUtil.getTenant();
        String schema = StringUtils.isNotBlank(tenant) ? (tenant + "_" + schemaName) : schemaName;
        PaasDataCacheVo.PaasDataCacheKey key = new PaasDataCacheVo.PaasDataCacheKey();
        key.setId(param.get(BaseDO.FIELD_ID).asText());
        key.setPaas_table(tableName);
        key.setPaas_schema(schema);
        if (CollectionUtils.isNotEmpty(jsonColumns)) {
            key.setJson_columns(String.join(",", jsonColumns));
        }
        String value = dataType != DataTypeEnum.DELETE ? param.toString() : null;
        List<PaasDataCacheVo> body = new ArrayList<>();
        body.add(new PaasDataCacheVo(key.toJsonString(), value));
        List<List<PaasDataCacheVo>> partitions = Lists.partition(body, PaasDataCacheVo.MAX_LIMIT);
        for (List<PaasDataCacheVo> partition : partitions) {
            paasDatatransferClient.sendDataToKafka(partition);
        }
    }

    /**
     * 将 ObjectNode 的字段名从驼峰式转换为下划线格式
     *
     * @param node 需要转换的 ObjectNode
     */
    private void convertCamelToUnderscore(ObjectNode node) {
        List<String> fieldNames = new ArrayList<>();
        node.fields().forEachRemaining(entry -> fieldNames.add(entry.getKey()));

        for (String fieldName : fieldNames) {
            String underscoreName = toUnderscoreCase(fieldName);
            if (!fieldName.equals(underscoreName)) {
                JsonNode value = node.get(fieldName);
                node.remove(fieldName);
                node.set(underscoreName, value);
            }
        }
    }

    /**
     * 驼峰命名转下划线命名（小写）
     *
     * @param name 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    private String toUnderscoreCase(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            result.append(Character.toLowerCase(name.charAt(0)));
            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (Character.isUpperCase(c)) {
                    result.append('_').append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /**
     * 同步到云端（异步执行）
     *
     * @param data 数据
     * @since 7.250601.0
     */
    @Override
    public <T extends BaseAssignIdDO> void sendCloudToAsync(List<PaasDataTransferParam<T>> data) {
        CompletableFuture.runAsync(() -> syncDbDataToCloud(data), BusinessThreadPoolExecutor.ttlExecutor());
    }

    /**
     * 解析属性名称和字段名称
     *
     * @param tableInfo 表信息
     * @return 属性名称 -> 字段名称
     * @since 5.241201.0
     */
    private Map<String, String> analyzePropertyAndColumn(@NonNull TableInfo tableInfo) {
        Map<String, String> result = tableInfo.getFieldList().stream().collect(
                Collectors.toMap(TableFieldInfo::getProperty, TableFieldInfo::getColumn, (o1, o2) -> o1));
        result.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        return result;
    }

    @Override
    public String logTag() {
        return "数据同步服务";
    }
}
