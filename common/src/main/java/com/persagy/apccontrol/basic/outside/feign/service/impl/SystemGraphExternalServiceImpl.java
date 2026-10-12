package com.persagy.apccontrol.basic.outside.feign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.MenuConfigVo;
import com.persagy.apccontrol.basic.outside.feign.service.SystemGraphExternalService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.structure.outside.systemgraph.ToProjectClassCodeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphExternalServiceImpl implements SystemGraphExternalService {

    private final SystemGraphService systemGraphService;
    private final CommonConfigService commonConfigService;

    @Override
    public List<SystemGraphVO> querySystemGraphList(Collection<String> classCodes) {
        if (CollectionUtils.containsAny(classCodes, CONVERT_PROJECT_CLASSCODE_LIST)) {
            classCodes.add("project");
        }
        LambdaQueryWrapper<SystemGraph> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraph::getProjectId, ContextUtil.getProjectId());
        queryWrapper.in(SystemGraph::getClassCode, classCodes);
        queryWrapper.isNotNull(SystemGraph::getSystemGraphDuplicateId);
        List<SystemGraph> list = systemGraphService.list(queryWrapper);
        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        List<MenuConfigVo> menuConfigs = commonConfigService.getMenuConfig();
        MenuConfigVo newMenu = new MenuConfigVo();
        newMenu.setImageOrProductName("低压变配电关系图");
        newMenu.setThreeLevelMenu("[\"SELD\"]");
        newMenu.setSecondaryMenu("低压配电系统");
        newMenu.setMenuCode("SE");
        menuConfigs.add(newMenu);
        Map<String, MenuConfigVo> menuConfigMap = menuConfigs.stream().collect(Collectors.toMap(MenuConfigVo::getImageOrProductName, Function.identity(), (v1, v2) -> v1));
        Map<String, GraphConfigVo> graphConfigMap = graphConfig.stream().collect(Collectors.toMap(GraphConfigVo::getMapTitle, Function.identity(), (v1, v2) -> v1));
        List<SystemGraphVO> result = new ArrayList<>();
        for (SystemGraph systemGraph : list) {
            SystemGraphVO vo = new SystemGraphVO();
            vo.setClassCode(systemGraph.getClassCode());
            vo.setGraphTypeCode(systemGraph.getGraphTypeCode());
            vo.setSystemObjectId(systemGraph.getSystemObjectId());
            vo.setSysName(systemGraph.getGraphName());
            String graphName = systemGraphService.getGraphName(systemGraph.getClassCode(), systemGraph.getGraphTypeCode(), graphConfig, null);
            MenuConfigVo menuConfigVo = menuConfigMap.get(graphName);
            GraphConfigVo graphConfigVo = graphConfigMap.get(graphName);
            if (menuConfigVo == null) {
                continue;
            }
            vo.setSystemCode(menuConfigVo.getMenuCode());
            boolean isProject = graphConfigVo != null && "PJ".equals(graphConfigVo.getObjectType());
            vo.setIsProject(isProject);
            if (isProject) {
                ToProjectClassCodeEnum[] values = ToProjectClassCodeEnum.values();
                for (ToProjectClassCodeEnum value : values) {
                    if (value.getReasonPhrase().equals(systemGraph.getGraphTypeCode())) {
                        vo.setSourceClassCode(value.getValue());
                        break;
                    }
                }
            } else {
                vo.setSourceClassCode(systemGraph.getClassCode());
            }
            if (StringUtils.isEmpty(vo.getSourceClassCode())) {
                continue;
            }
            result.add(vo);
        }

        return result;
    }
}
