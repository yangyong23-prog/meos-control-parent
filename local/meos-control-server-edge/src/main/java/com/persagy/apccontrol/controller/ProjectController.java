package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.controller.vo.ProjectInfoVO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调试控制器
 *
 * @since 1.0.0
 */
@Tag(name = "项目信息")
@RestController
@RequestMapping("/project")
public class ProjectController {


    @Autowired
    private ProjectCfg projectCfg;

    @Value("${meoi.url.pre:}")
    private String meoiUrl;
    @Value("${system.appName:}")
    private String systemAppName;

    @Operation(summary = "查询项目信息")
    @PostMapping("/query")
    public R<ProjectInfoVO> queryProject() {
        return R.success(new ProjectInfoVO(projectCfg.getGroupCode(), projectCfg.getProjectId(), projectCfg.getProjectName(), meoiUrl, systemAppName));
    }
}
