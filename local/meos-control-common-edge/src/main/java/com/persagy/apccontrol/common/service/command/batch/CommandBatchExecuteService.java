package com.persagy.apccontrol.common.service.command.batch;


import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.common.service.command.batch.dto.CommandBatchExecuteDTO;

import java.util.List;

/**
 * 指令下发服务
 * 1.下发自动需求指令
 * 2.下发手动指令
 *
 * @author yuyao
 */
public interface CommandBatchExecuteService {



    public void commandExecute(List<CommandBatchExecuteDTO> commandExecuteDTOList);

    /**
     * 需求类型 指令执行
     * 冷站系统和末级泵组
     * er 通过 指令记录表下发需求指令
     */
    public void commandExecuteByEquipRecord(List<EquipRecord> equipRecordList);

    /**
     * 通知前端做页面数据刷新
     */
    public void pushMessageToWeb();

}
