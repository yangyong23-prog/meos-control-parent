package com.persagy.apccontrol.engine;

import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.task.TecXxJobHandler;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author shaohongbo
 * @date 2022/8/15 11:25
 * 基础任务执行器
 */
@Slf4j
public abstract class AbstractTecXxJobHandler implements TecXxJobHandler {

    /**
     * redis 锁
     */
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 执行方法
     * Async 跑测试用例的时候需要注释
     *
     * @param groupCode  groupCode
     * @param projectDTO projectDTO
     * @author yuyao
     * @date 2022/8/15 11:24
     * @return java.util.concurrent.Future<java.lang.String>
     */
    @Override
    @Async
    public Future<String> processAsync(String groupCode, ProjectDTO projectDTO) {
        //异步线程 重新设置上下文
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);

        long startTime = System.currentTimeMillis();
        //任务加锁 防止重复执行
        String lockKey = RedisKey.LOCK_KEY_TASK + groupCode + this.getHandlerName() + projectDTO.getProjectId();

        AsyncResult<String> r = new AsyncResult<>(this.getHandlerName() + "执行完毕:groupCode:" + groupCode + ";projectId" + projectDTO.getProjectId());
        RLock lock1 = redissonClient.getLock(lockKey);
        try {
            //防止重复执行数据堆积 对计算任务进行加锁 保证上一次任务执行完后才执行下一次任务
            boolean lock = lock1.tryLock(0, 30, TimeUnit.SECONDS);
            if (!lock) {
                log.info("上次任务还未执行完，跳过本次执行【{}】-【{}】-【{}】", groupCode, this.getHandlerName(), projectDTO.getProjectId());
                return r;
            }
            log.info("【{}】开始执行【{}】-【{}】", this.getHandlerName(), groupCode, projectDTO.getProjectId());

            handleProject(projectDTO);
        } catch (Exception e) {
            log.error("任务执行失败,{}-{}", getHandlerName(), projectDTO.getProjectId(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("【任务执行结束】【{}】-【{}】，【{}】，总耗时【{}】", groupCode, this.getHandlerName(), projectDTO.getProjectId(), (endTime - startTime));
            lock1.unlock();
            ContextUtil.unloadContext();
        }
        return r;
    }

    @Override
    public void process(String groupCode, ProjectDTO projectDTO) {
        try {
            handleProject(projectDTO);
        } catch (Exception e) {
            log.error("任务执行失败,{}-{}", getHandlerName(), projectDTO.getProjectId(), e);
        }
    }

    /**
     * 按项目执行
     *
     * @param projectDTO projectDTO
     * @author yuyao
     * @date 2022/8/15 11:23
     * @return void
     */

    public abstract void handleProject(ProjectDTO projectDTO);
}


