package com.persagy.apccontrol.basic.outside.feign.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.datatransfer.PaasDataTransferKafkaOffsetInfoDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * 数据同步服务客户端服务
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
public interface PaasDataTransferClientService {

    /**
     * 查询云端在离线状态
     *
     * @return 云端在离线状态
     * @since 5.240801.0
     */
    boolean cloudNetHealthy();

    /**
     * 查询Kafka消费情况
     *
     * @return Kafka消费情况
     * @since 5.240801.0
     */
    List<PaasDataTransferKafkaOffsetInfoDTO> listKafkaOffsets();

    /**
     * 同步数据库数据至云端
     *
     * @param params 同步参数
     * @since 5.241102.0
     */
    <T extends BaseAssignIdDO> void syncDbDataToCloud(@NonNull List<@NonNull PaasDataTransferParam<@NonNull T>> params);

    /**
     * 同步数据库数据至云端
     *
     * @param tableName 表名
     * @param jsonColumns JSON列
     * @param dataType  操作类型
     * @param param     同步参数
     * @since 7.250403.0
     */
    void syncDbDataToCloud(
            @NonNull String tableName,
            @Nullable Set<@NonNull String> jsonColumns,
            @NonNull DataTypeEnum dataType,
            @NonNull ObjectNode param);

    /**
     * 同步数据到云端（异步执行）
     *
     * @param data 数据
     * @since 7.250601.0
     */
    <T extends BaseAssignIdDO> void sendCloudToAsync(List<PaasDataTransferParam<T>> data);
}
