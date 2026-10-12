package com.persagy.apccontrol.engine;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.mapper.RcBasicsAsyncTaskMapper;
import com.persagy.apccontrol.basic.model.base.TaskTypeAnnotation;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.task.SystemEnum;
import com.persagy.apccontrol.basic.service.engine.TecTaskEngine;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yuyao
 * @date 2022/4/13 15:54
 * 系统基础任务
 */
@Service("rcTaskHandler")
@Slf4j
public class RcTaskHandlerImpl extends AbstractTecXxJobHandler {

    public final String handlerName = LocaleMessageUtil.getMessage("key_67e4dd0742614689b3cf0b4e6b0c3535");

    @Resource
    private List<TecTaskEngine> tecTaskEngine;

    @Resource
    private RcBasicsAsyncTaskMapper rcBasicsAsyncTaskMapper;
    public static ExecutorService executor = ExecutorBuilder.create().setCorePoolSize(1).setMaxPoolSize(10)
            .setHandler(new ThreadPoolExecutor.CallerRunsPolicy()).build();

    @Override
    public String getHandlerName() {
        return handlerName;
    }

    @Override
    public void handleProject(ProjectDTO projectDTO) {

        //获取项目纬度任务
        List<RcBasicsAsyncTask> list = getTecTasks(projectDTO);

        //逐个执行
        for (RcBasicsAsyncTask t : list) {
            execute(t, projectDTO);
        }
    }

    /**
     * 获取批量执行任务
     *
     * @param projectDTO projectDTO
     */
    private List<RcBasicsAsyncTask> getTecTasks(ProjectDTO projectDTO) {

        List<RcBasicsAsyncTask> list = rcBasicsAsyncTaskMapper.queryProjectTaskList(projectDTO.getProjectId()
                , SystemEnum.SAAS.getCode());

        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<OperateTypeEnum> typeList = new ArrayList<>();

        list.forEach(i -> {
            //如果是 统配 判断是否 存在项目纬度的配置 如果存在就以项目任务为准
            typeList.add(i.getType());
        });

        //如果是 统配 判断是否 存在项目纬度的配置 如果存在就以项目任务为准
        LambdaQueryWrapper<RcBasicsAsyncTask> qw = new LambdaQueryWrapper<>();
        qw.eq(RcBasicsAsyncTask::getProjectId, projectDTO.getProjectId());
        qw.in(RcBasicsAsyncTask::getType, typeList);
        List<RcBasicsAsyncTask> pList = rcBasicsAsyncTaskMapper.selectList(qw);
        if (CollectionUtil.isNotEmpty(pList)) {
            list.forEach(t -> {
                for (RcBasicsAsyncTask i : pList) {
                    if (StringUtils.isEmpty(t.getProjectId()) &&
                            t.getType().equals(i.getType())
                    ) {
                        //对象替换
                        t = i;
                    }
                }
            });
        }
        return list;
    }

    /**
     * 任务执行
     *
     * @param tecBasicsAsyncTask tecTask
     * @param projectDTO         projectDTO
     * @author yuyao
     * @date 2022/7/25 18:24
     * @Return void
     */
    public void execute(RcBasicsAsyncTask tecBasicsAsyncTask, ProjectDTO projectDTO) {

        //任务时间更新到当前
        RcBasicsAsyncTask t = new RcBasicsAsyncTask();
        t.setId(tecBasicsAsyncTask.getId());
        t.setUpdateTime(new Date());
        rcBasicsAsyncTaskMapper.updateById(t);

        OperateTypeEnum type = tecBasicsAsyncTask.getType();
        if (type == null) {
            log.warn("任务类型还未配置适配器不执行！【{}】", tecBasicsAsyncTask);
            return;
        }
        for (TecTaskEngine f : tecTaskEngine) {
            executor.execute(() -> executeTaskEngine(tecBasicsAsyncTask, projectDTO, t, type, f, ContextUtil.getGroupCode()));
        }
    }

    /**
     * 执行单个任务引擎 按照计算任务的线程进行处理
     *
     * @param tecBasicsAsyncTask
     * @param projectDTO
     * @param t
     * @param type
     * @param f
     */
    public void executeTaskEngine(RcBasicsAsyncTask tecBasicsAsyncTask, ProjectDTO projectDTO,
                                  RcBasicsAsyncTask t, OperateTypeEnum type, TecTaskEngine f, String groupCode) {
        //异步线程 重新设置上下文
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);

        if (AopUtils.getTargetClass(f).getAnnotation(TaskTypeAnnotation.class).value().equals(type)) {
            try {
                tecBasicsAsyncTask.setProjectId(projectDTO.getProjectId());
                f.execute(tecBasicsAsyncTask, projectDTO);
            } catch (BusinessException e) {
                //记录已知异常到 任务memo 里面
                t.setMemo(e.getMessage());
                rcBasicsAsyncTaskMapper.updateById(t);
            } catch (Exception e) {
                //任务执行失败
                log.error("系统基础任务执行失败：{}-{}-{}", type.getDesc(), t.getProjectId(), t.getType(), e);
            }
        }
    }
}
