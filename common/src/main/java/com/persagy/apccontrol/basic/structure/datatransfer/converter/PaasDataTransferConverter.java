package com.persagy.apccontrol.basic.structure.datatransfer.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.datatransfer.PaasDataTransferKafkaOffsetInfoDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.datatransfer.DataTransferKafkaOffsetInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 数据同步服务转换器
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaasDataTransferConverter extends BaseConvert {

    // ------------------------- To DTO -------------------------

    /**
     * 转换模型
     *
     * @param source 源对象
     * @return 目标对象
     */
    PaasDataTransferKafkaOffsetInfoDTO toDto(DataTransferKafkaOffsetInfoResponse source);


}
