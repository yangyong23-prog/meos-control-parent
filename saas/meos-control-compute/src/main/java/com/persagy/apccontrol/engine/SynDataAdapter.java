package com.persagy.apccontrol.engine;

import com.persagy.apccontrol.basic.model.base.TaskTypeAnnotation;
import com.persagy.apccontrol.basic.model.context.SynDataCtx;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.service.SynDataService;
import com.persagy.apccontrol.basic.service.engine.TecTaskEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : yuyao
 * @Date : 2022/4/13 16:01
 * @Description ：数据同步任务 补偿配置的同步时间是 空的数据 进行定时同步
 */
@Service
@TaskTypeAnnotation(value = OperateTypeEnum.DS)
@Slf4j
public class SynDataAdapter implements TecTaskEngine {

    @Resource
    private List<SynDataService> synDataService;

    @Override
    public void execute(RcBasicsAsyncTask rcBasicsAsyncTask, ProjectDTO projectDTO) {
        //判断状态 任务状态不匹配直接跳过
        if (!OperateTypeEnum.DS.equals(rcBasicsAsyncTask.getType())) {
            return;
        }

        try {
            SynDataCtx ctx = new SynDataCtx();
            ctx.setProjectId(projectDTO.getProjectId());
            synDataService.forEach(i -> i.execute(ctx));
        } catch (Exception e) {
            log.warn("数据同步失败:{}", projectDTO.getProjectId(), e);
        }

    }

}
