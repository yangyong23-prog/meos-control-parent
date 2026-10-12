package com.persagy.apccontrol.common.service.command.batch.dto;


import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


/**
 * 需求指令分组执行
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandExecuteGroupDTO {

    /**
     * 下发指令列表 单设备多信息点模式
     */
    private List<DtpTwinAutoSendDTO> dtpTwinAutoSendDTOList;

    /**
     * 原始指令列表 单设备单信息点模式
     */
    private List<CommandBatchExecuteDTO> commandExecuteDTOList;

    // ----------------------- 模版数据 ---------------------------

    /**
     * 指令类型-时间表-手动干预-自动需求
     */
    private SourceFromEnum type;


    /**
     * 分组的系统ID
     */
    private String systemId;

    public CommandExecuteGroupDTO(List<DtpTwinAutoSendDTO> dtpTwinAutoSendDTOList, List<CommandBatchExecuteDTO> commandExecuteDTOList) {
        this.dtpTwinAutoSendDTOList = dtpTwinAutoSendDTOList;
        this.commandExecuteDTOList = commandExecuteDTOList;
    }


    /**
     * 校验数据是否符合预期
     *
     * @return 是否校验失败
     */
    public boolean isCheckFail() {
        init();
        boolean a = CollectionUtils.isEmpty(commandExecuteDTOList);
        boolean b = CollectionUtils.isEmpty(dtpTwinAutoSendDTOList);

        return a || b;
    }

    public void init() {
        if (CollectionUtils.isEmpty(commandExecuteDTOList)) {
            return;
        }
        this.type = commandExecuteDTOList.get(0).getType();
        this.systemId = commandExecuteDTOList.get(0).getSystemId();

    }


}
