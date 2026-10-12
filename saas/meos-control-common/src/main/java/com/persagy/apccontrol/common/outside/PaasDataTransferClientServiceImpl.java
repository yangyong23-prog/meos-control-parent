package com.persagy.apccontrol.common.outside;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Suppliers;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.datatransfer.PaasDataTransferKafkaOffsetInfoDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 数据同步服务客户端服务
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Service
public class PaasDataTransferClientServiceImpl implements PaasDataTransferClientService {

    private static final Supplier<ApplicationException> NOT_SUPPORT_EXCEPTION = Suppliers.memoize(() ->
            new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_e505ebb23f8547309105a480e56ab8af")));

    @Override
    public boolean cloudNetHealthy() {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public List<PaasDataTransferKafkaOffsetInfoDTO> listKafkaOffsets() {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public <T extends BaseAssignIdDO> void syncDbDataToCloud(
            @NonNull List<@NonNull PaasDataTransferParam<@NonNull T>> params) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public void syncDbDataToCloud(
            @NonNull String tableName,
            @Nullable Set<@NonNull String> jsonColumns,
            @NonNull DataTypeEnum dataType,
            @NonNull ObjectNode param) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }

    @Override
    public <T extends BaseAssignIdDO> void sendCloudToAsync(List<PaasDataTransferParam<T>> data) {
        throw NOT_SUPPORT_EXCEPTION.get();
    }
}
