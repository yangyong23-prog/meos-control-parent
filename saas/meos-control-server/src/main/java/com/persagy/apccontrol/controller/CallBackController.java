package com.persagy.apccontrol.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategySyncDTO;
import com.persagy.apccontrol.basic.model.entity.*;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.*;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import com.persagy.validate.utility.executor.ObjectExecutor;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author yuyao
 */
@RestController
@RequestMapping("/callBack")
@Slf4j
@Validated
@Tag(name = "同步回调接口 接口")
public class CallBackController {

    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private RcBasicsAsyncTaskService rcBasicsAsyncTaskService;
    @Autowired
    private RcBasicsConfigService rcBasicsConfigService;
    @Autowired
    private RcSysLogService rcSysLogService;
    @Autowired
    private RcSystemPicService rcSystemPicService;
    @Autowired
    private RcEqInfoCodeCfgService rcEqInfoCodeCfgService;
    @Autowired
    private StrategyCfgService strategyCfgService;
    @Autowired
    private StrategyDateCfgService strategyDateCfgService;
    @Autowired
    private StrategyPlanCfgService strategyPlanCfgService;
    @Autowired
    private RcRunningLogService rcRunningLogService;
    @Autowired
    private MidPermissionClientService midPermissionClientService;

    @ApiOperation("Redis回调")
    @PostMapping("/redis")
    public R<Boolean> syncRedis(@RequestParam String key, @RequestBody Object data) {
        redisCacheUtil.set(key, data);
        return R.success(true);
    }

    @ApiOperation("任务同步回调结果")
    @RequestMapping(value = "/syncTask", method = RequestMethod.POST)
    public R<Boolean> syncTask(@RequestBody @Validated DataSynDTO ysnObj) {
        if (CollectionUtils.isEmpty(ysnObj.getYsnObj())) {
            return R.success(true);
        }

        Date n = new Date();

        // 执行业务逻辑 批量更新
        ysnObj.getYsnObj().forEach(i -> {

            if (ysnObj.getTableName() == null) {
                return;
            }
            switch (ysnObj.getTableName()) {
                case RC_BASICS_ASYNC_TASK:
                    RcBasicsAsyncTask t = JSONObject.parseObject(i, RcBasicsAsyncTask.class);
                    LambdaUpdateWrapper<RcBasicsAsyncTask> gx = new LambdaUpdateWrapper<>();
                    gx.in(RcBasicsAsyncTask::getId, t.getId());
                    gx.set(RcBasicsAsyncTask::getUpdateTime, n);
                    gx.set(RcBasicsAsyncTask::getSynTime, n);
                    rcBasicsAsyncTaskService.update(gx);
                    break;
                case RC_BASICS_CONFIG:
                    RcBasicsConfig c = JSONObject.parseObject(i, RcBasicsConfig.class);
                    LambdaUpdateWrapper<RcBasicsConfig> gxc = new LambdaUpdateWrapper<>();
                    gxc.in(RcBasicsConfig::getId, c.getId());
                    gxc.set(RcBasicsConfig::getUpdateTime, n);
                    gxc.set(RcBasicsConfig::getSynTime, n);
                    rcBasicsConfigService.update(gxc);
                    break;
                case RC_SYSTEM_PIC_EQUIPMENT:
                case RC_SYSTEM_PIC:
                    RcSystemPic pic = JSONObject.parseObject(i, RcSystemPic.class);
                    LambdaUpdateWrapper<RcSystemPic> rsp = new LambdaUpdateWrapper<>();
                    rsp.eq(ObjectUtil.isNotEmpty(pic.getName()), RcSystemPic::getName, pic.getName())
                            .eq(ObjectUtil.isNotEmpty(pic.getProjectId()), RcSystemPic::getProjectId, pic.getProjectId())
                            .isNull(ObjectUtil.isEmpty(pic.getProjectId()), RcSystemPic::getProjectId);
                    rsp.set(RcSystemPic::getUpdateTime, n);
                    rsp.set(RcSystemPic::getSynTime, n);
                    rcSystemPicService.update(rsp);
                    break;
                case RC_EQ_INFO_CODE_CFG:
                    RcEqInfoCodeCfg cfg = JSONObject.parseObject(i, RcEqInfoCodeCfg.class);
                    LambdaUpdateWrapper<RcEqInfoCodeCfg> queryCfg = new LambdaUpdateWrapper<>();
                    queryCfg.eq(ObjectUtil.isNotEmpty(cfg.getClassCode()), RcEqInfoCodeCfg::getClassCode, cfg.getClassCode())
                            .eq(ObjectUtil.isNotEmpty(cfg.getProjectId()), RcEqInfoCodeCfg::getProjectId, cfg.getProjectId())
                            .eq(ObjectUtil.isNotEmpty(cfg.getInfoCode()), RcEqInfoCodeCfg::getInfoCode, cfg.getInfoCode())
                            .eq(ObjectUtil.isNotEmpty(cfg.getProjectId()), RcEqInfoCodeCfg::getProjectId, cfg.getProjectId());
                    queryCfg.set(RcEqInfoCodeCfg::getSysTime, n);
                    rcEqInfoCodeCfgService.update(queryCfg);
                    break;
                default:
            }
        });

        return R.success(true);
    }


    @ApiOperation("同步本地配置")
    @GetMapping(value = "/syncBasicsConfig")
    public R<List<RcBasicsConfig>> syncBasicsConfig() {
        return R.success(rcBasicsConfigService.listAll());
    }

    @ApiOperation("同步本地操作日志")
    @RequestMapping(value = "/syncRcSysLog", method = RequestMethod.POST)
    public R<Boolean> syncRcSysLog(@RequestBody @Validated List<RcSysLog> cfg) {
        if (CollectionUtils.isEmpty(cfg)) {
            return R.success(true);
        }
        this.rcSysLogService.batchSyn(cfg);
        return R.success(true);
    }

    @Operation(summary = "同步策略干预", tags = VersionConstant.V5_7_0)
    @PostMapping("/syncStrategy")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> syncStrategy(@RequestBody @Valid StrategySyncDTO param) {
        ObjectExecutor.notEmptyExecute(param.getStrategies(),
                () -> strategyCfgService.saveOrUpdateBatch(param.getStrategies()));
        ObjectExecutor.notEmptyExecute(param.getStrategyDateList(),
                () -> strategyDateCfgService.saveOrUpdateBatch(param.getStrategyDateList()));
        ObjectExecutor.notEmptyExecute(param.getStrategyPlans(),
                () -> strategyPlanCfgService.saveOrUpdateBatch(param.getStrategyPlans()));

        ObjectExecutor.notEmptyExecute(param.getDelStrategyIds(),
                () -> strategyCfgService.removeByIds(param.getDelStrategyIds()));
        ObjectExecutor.notEmptyExecute(param.getDelStrategyDateIds(),
                () -> strategyDateCfgService.removeByIds(param.getDelStrategyDateIds()));
        ObjectExecutor.notEmptyExecute(param.getDelStrategyPlanIds(),
                () -> strategyPlanCfgService.removeByIds(param.getDelStrategyPlanIds()));
        return R.success(true);
    }


    @Operation(summary = "同步运行日志", tags = VersionConstant.V5_8_0)
    @PostMapping("/syncRunningLog")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> syncRunningLog(@RequestBody @Valid List<RcRunningLog> param) {
        /*if (CollectionUtils.isEmpty(param)) {
            return R.success(true);
        }
        //由于边缘端无法查到人员信息，所以在同步数据得时候需要将userid转换为人员名称
        Set<String> userIds = param.stream().filter(x -> !CommonConst.SYSTEM_NAME.equals(x.getCreateUser()) && StringUtils.isNotEmpty(x.getCreateUser()))
                .map(RcRunningLog::getCreateUser).collect(Collectors.toSet());
        Map<String, String> userMap = midPermissionClientService.getUserNameByUserIds(ContextUtil.getGroupCode(), userIds);
        param.forEach(runningLog -> {
            if (StringUtils.isNotEmpty(userMap.get(runningLog.getCreateUser()))){
                runningLog.setCreateUser(userMap.get(runningLog.getCreateUser()));
                runningLog.setKeyWord(runningLog.getCreateUser() + runningLog.getKeyWord().substring(runningLog.getKeyWord().indexOf(" ")));
            }
        });
        this.rcRunningLogService.batchSyn(param);*/
        return R.success(true);
    }
}
