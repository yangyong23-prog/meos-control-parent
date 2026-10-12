package com.persagy.apccontrol.basic.controller.shellconfig;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.controller.shellconfig.param.*;
import com.persagy.apccontrol.basic.model.entity.ShellConfigBasic;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.WeatherStationVO;
import com.persagy.apccontrol.basic.service.cacherwd.CacheRwdService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/9/26 8:50 PM
 */
@Tag(name = "壳范围配置")
@RestController
@RequestMapping("/shellConfig")
@Validated
public class ShellConfigController {

    @Autowired
    private ShellConfigService shellConfigService;

    /**
     * 缓存服务，用于原子保存末端全量模式并清理新版白名单 Key。
     *
     * @since v7.260701.0
     */
    @Autowired
    private @NonNull CacheRwdService cacheRwdService;

    @Operation(summary = "AI算法开关设置", tags = VersionConstant.V5_7_0)
    @PostMapping("/changeAlgorithmConfig")
    public R<Boolean> changeAlgorithmConfig(@RequestBody JSONObject param) {
        String projectId = param.getString("projectId");
        Boolean status = param.getBoolean("status");
        if (StrUtil.isBlank(projectId)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_79ae698d8ca34f22a0784247c6543796"));
        }
        return R.success(shellConfigService.changeAlgorithmConfig(ContextUtil.getGroupCode(), projectId, status));
    }

    @Operation(summary = "时序大模型策略开关设置", tags = VersionConstant.V7_250403_0)
    @PostMapping("/changeLtsmConfig")
    public R<Boolean> changeLtsmConfig(@RequestBody @Valid LtsmConfigParam param) {
        return R.success(shellConfigService.changeLtsmConfig(param));
    }

    @Operation(summary = "查询AI算法开关设置", tags = VersionConstant.V5_7_0)
    @PostMapping("/getAlgorithmConfig")
    public R<Boolean> getAlgorithmConfig(@RequestBody @Valid ProjectVO projectVO) {
        return R.success(shellConfigService.getAlgorithmConfig(ContextUtil.getGroupCode(), projectVO.getProjectId()));
    }

    @Operation(summary = "查询时序大模型策略开关", tags = VersionConstant.V7_250403_0)
    @GetMapping("/listLtsmConfig")
    public R<List<LtsmConfigParam>> listLtsmConfig() {
        return R.success(shellConfigService.listLtsmConfig());
    }

    @Operation(summary = "查询壳范围配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/query")
    public R<List<ShellConfigVO>> query(@RequestBody @Valid QueryShellConfigParam param) {
        return R.success(shellConfigService.query(ContextUtil.getGroupCode(), param));
    }

    @Operation(summary = "保存壳范围配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody @Valid SaveShellConfigParam param) {
        if (this.isAirTerminalFullSpaceRange(param)) {
            // 原参数完整持久化和目标项目 Key 清理在缓存写服务的同一个短事务中完成。
            return R.success(this.cacheRwdService
                    .saveShellConfigAndRemoveAirTerminalWhitelist(param));
        }
        return R.success(this.shellConfigService.save(ContextUtil.getGroupCode(), param));
    }

    @Operation(summary = "根据信息点编码查询壳配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/queryByInfoCode")
    public R<ShellConfigItem> queryByInfoCode(@RequestBody @Valid QueryShellConfigByInfoCodeParam param) {
        return R.success(shellConfigService.queryByInfoCode(ContextUtil.getGroupCode(), param));
    }

    @Operation(summary = "批量查询数据", tags = VersionConstant.V5_7_0)
    @PostMapping("/batchQueryConfigByObjectId")
    public R<List<ShellConfigItem>> batchQueryConfigByObjectId(@RequestBody @Valid BatchQueryShellConfigParam param) {
        return R.success(shellConfigService.batchQueryConfigByObjectId(ContextUtil.getGroupCode(), param));
    }
    
    @Operation(summary = "壳配置mpc 智能体配置", tags = VersionConstant.V7_251001_0)
    @PostMapping("/mpcAgentCfg")
    public R<Boolean> mpcAgentCfg(@RequestBody @Valid List<MpcAgentCfgParam> param) {
        shellConfigService.mpcAgentCfg(param);
        return R.success(true);
    }
    
    @Operation(summary = "查询壳配置mpc 智能体配置", tags = VersionConstant.V7_251001_0)
    @PostMapping("/queryMpcAgentCfg")
    public R<List<MpcAgentCfgParam>> queryMpcAgentCfg() {
        return R.success(shellConfigService.queryMpcAgentCfg());
    }

    /**
     * 判断通用壳配置保存是否把末端空间范围切换为全量。
     *
     * <p>白名单场景当前只有该配置项；缺少、空白或未知值时业务读取均按 {@code FULL} 处理，
     * 因此保存入口也必须进入同一清理流程。</p>
     *
     * @param param 壳配置保存参数
     * @return {@code true} 表示需要同步切换全量并清理白名单 Key
     * @since v7.260701.0
     */
    private boolean isAirTerminalFullSpaceRange(
            @NonNull SaveShellConfigParam param) {
        if (!StringUtils.equals(
                ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getScene(),
                param.getScene())) {
            return false;
        }
        for (SaveShellConfigParam.ShellConfigGroup group : param.getGroups()) {
            for (ShellConfigBasic item : group.getItems()) {
                if (StringUtils.equals(
                        ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getCode(),
                        item.getCode())) {
                    return !StringUtils.equals(
                            WhitelistTypeEnum.CUSTOM.getShellConfigValue(),
                            item.getValue());
                }
            }
        }
        return true;
    }
}
