package com.persagy.apccontrol.basic.controller.external;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.param.external.InfoCodeConfigQueryParam;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 信息点配置
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/31 11:28
 */
@Slf4j
@RestController
@Tag(name = "信息点配置")
@RequestMapping(value = "/external/infoCodeConfig")
public class RcInfoCodeConfigController {

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Operation(summary = "查询某设备类下所有的信息点定义", tags = {VersionConstant.V5_6_0})
    @GetMapping("/listInfoCodeConfigByClassCode")
    public R<Collection<RcInfoCodeCfg>> listInfoCodeConfigByClassCode(@RequestParam String projectId, @RequestParam String classCode) throws Exception {
        List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
        return R.successMulti(infoCodeConfigs);
    }

    @Operation(summary = "根据条件查询信息点定义", tags = {VersionConstant.V5_6_0})
    @PostMapping("/listInfoCodeConfig")
    public R<Map<String, List<RcInfoCodeCfg>>> listInfoCodeConfig(@RequestBody InfoCodeConfigQueryParam queryParam) throws Exception {
        Map<String, List<RcInfoCodeCfg>> resultMap = new HashMap<>();
        List<ClassCode> classCodes = queryParam.getClassCode();
        for (ClassCode classCode : classCodes) {
            List<RcInfoCodeCfg> rcInfoCodeCfgs = CollectionUtil.isEmpty(classCode.getInfoCodes()) ? this.localRedisCacheHandler.getInfoCodeConfigs(classCode.getClassCode())
                    : this.localRedisCacheHandler.getInfoCodeConfigs(classCode.getClassCode(), classCode.getInfoCodes());

            // 数据过滤
            String firstGroupInfoId = RcInfoCodeCfg.getFirstGroupIdByName(classCode.getClassCode(), queryParam.getFirstGroup());
            if (StrUtil.isNotBlank(firstGroupInfoId) || StrUtil.isNotBlank(queryParam.getFirstTag()) || StrUtil.isNotBlank(queryParam.getImportanceLevel())) {
                List<RcInfoCodeCfg> resultConfigs = new ArrayList<>();
                for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeCfgs) {
                    if (StrUtil.isNotBlank(firstGroupInfoId) && !firstGroupInfoId.equals(rcInfoCodeCfg.getTopGroupId())) {
                        continue;
                    }
                    if (StrUtil.isNotBlank(queryParam.getFirstTag()) && !queryParam.getFirstTag().equals(rcInfoCodeCfg.getFirstTag())) {
                        continue;
                    }
                    if (StrUtil.isNotBlank(queryParam.getImportanceLevel()) && !queryParam.getImportanceLevel().equals(rcInfoCodeCfg.getImportanceLevel())) {
                        continue;
                    }
                    resultConfigs.add(rcInfoCodeCfg);
                }
                resultMap.put(classCode.getClassCode(), resultConfigs);
            } else {
                resultMap.put(classCode.getClassCode(), rcInfoCodeCfgs);
            }
        }

        return R.success(resultMap);
    }

}
