package com.persagy.apccontrol.controller.client;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SystemDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.common.handle.ClientServerHandler;
import com.persagy.apccontrol.common.model.param.client.EquipmentInfoCodeConfigByObjectQueryClientParam;
import com.persagy.apccontrol.common.model.param.desktop.DesktopEquipmentDetailsQueryParam;
import com.persagy.apccontrol.common.model.vo.SystemRelationVO;
import com.persagy.apccontrol.common.model.vo.client.EquipmentInfoCodeObjectGroupByTemplateVO;
import com.persagy.apccontrol.common.model.vo.client.RelationObjectsAndSpaceVO;
import com.persagy.apccontrol.common.model.vo.desktop.DesktopEquipmentDetailsVO;
import com.persagy.apccontrol.service.DesktopService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * CS端
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/7 14:40
 */
@Slf4j
@RestController
@Tag(name = "桌面客户端")
@RequestMapping(value = "/client-server")
public class ClientServerController {

    @Autowired
    private ClientServerHandler clientServerHandler;
    @Autowired
    private DesktopService desktopService;
    @Autowired
    private Executor executorService;

    @Operation(summary = "查询设备详情列表", tags = VersionConstant.V5_18_0)
    @PostMapping("/listEquipmentDetails")
    public R<List<DesktopEquipmentDetailsVO>> listEquipmentDetails(
            @RequestBody @Valid DesktopEquipmentDetailsQueryParam param) {
        return R.success(desktopService.listEquipmentDetails(param));
    }

    @Operation(summary = "获取系统下的设备、管网、竖井、空间", tags = VersionConstant.V5_18_0)
    @PostMapping("/querySystemRelationObjects")
    public R<List<SystemRelationVO>> querySystemRelationObjects(@Validated @RequestBody SystemDTO param) {
        List<DtpRwdClassCodeEnum> systemClassCodes = param.getSystemClassCodes();
        List<String> funcCodes = param.getFuncCodes();
        String projectId = param.getProjectId();

        List<CompletableFuture<List<JSONObject>>> futures1 = new ArrayList<>();
        List<CompletableFuture<List<SystemRelationVO>>> futures = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(systemClassCodes)) {
            systemClassCodes.forEach(e -> {
                if (DtpRwdClassCodeEnum.COLD_SYSTEM == e) {
                    CompletableFuture<List<JSONObject>> future = CompletableFuture.supplyAsync(() -> this.clientServerHandler.querySystemRelationObjectsByAccc(ContextUtil.getGroupCode(),
                            param.getProjectId(), e.value()), executorService);
                    futures1.add(future);
                } else {
                    CompletableFuture<List<SystemRelationVO>> future = CompletableFuture.supplyAsync(() ->
                            this.clientServerHandler.querySystemRelationObjects(null, e, param.getProjectId()), executorService);
                    futures.add(future);
                }

            });

        }
        if (CollectionUtil.isNotEmpty(funcCodes)) {
            CompletableFuture<List<SystemRelationVO>> future = CompletableFuture.supplyAsync(() -> this.clientServerHandler.querySystemRelationByFunctionCode(projectId, funcCodes), executorService);
            futures.add(future);
        }

        List<SystemRelationVO> systemList = new ArrayList<>();
        futures.forEach(f -> {
            try {
                systemList.addAll(f.get());
            } catch (InterruptedException e) {
                throw new BusinessException(e);
            } catch (ExecutionException e) {
                throw new BusinessException(e);
            }
        });
        futures1.forEach(f -> {
            List<JSONObject> join = null;
            try {
                join = f.get();
                if (CollectionUtil.isNotEmpty(join)) {
                    systemList.addAll(BeanUtil.copyToList(join, SystemRelationVO.class));
                }
            } catch (InterruptedException e) {
                throw new BusinessException(e);
            } catch (ExecutionException e) {
                throw new BusinessException(e);
            }


        });
        return R.success(systemList);
    }

    @Operation(summary = "查询设备列表根据项目/建筑/楼层/空间查询", tags = VersionConstant.V5_240602_0)
    @PostMapping("/listInfoCodeConfigsByObject")
    public R<List<EquipmentInfoCodeObjectGroupByTemplateVO>> listInfoCodeConfigsByObject(@Validated @RequestBody EquipmentInfoCodeConfigByObjectQueryClientParam param) {

     return  R.success(clientServerHandler.listInfoCodeConfigsByObject(param));
    }
    @Operation(summary = "查询-项目下设备以及空调机房空间", tags = VersionConstant.V5_240602_0)
    @PostMapping("/queryRelationObjectsAndSpace")
    public R<List<RelationObjectsAndSpaceVO>> queryRelationObjectsAndSpace(@Validated @RequestBody EquipmentInfoCodeConfigByObjectQueryClientParam param) {
        return  R.success(Lists.newArrayList(clientServerHandler.queryRelationObjectsAndSpace(param)));
    }

}
