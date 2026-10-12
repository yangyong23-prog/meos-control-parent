package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.UpdateTemplateNameDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphTemplateLegendSelectDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphTemplateStatus;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author persagy
 * @description 针对表【system_graph_template_status(系统图模版状态表)】的数据库操作Service
 * @createDate 2024-06-25 11:55:20
 */
public interface SystemGraphTemplateStatusService extends IService<SystemGraphTemplateStatus> {

    boolean getTemplateFlag(String templateId, String objectId);

    List<SystemGraphTemplateStatus> listTemplateStatusByTemplateId(@NonNull String templateId, @Nullable String projectId);

    SystemGraphTemplateStatus saveTemplateStatus(String templateId, String objectId, String name, String graphCodeType, Integer sequenceNumberCurrent);

    void updateTemplateStatus(String templateId, String objectId, boolean updatedStatus, String graphCodeType, Integer sequenceNumberCurrent);

    void updateTemplateStatusByObjectIdAndGraphCodeType(String graphCodeType, boolean updatedStatus, String objectId);

    Boolean updateTemplateName(UpdateTemplateNameDTO updateTemplateNameDTO);

    String getTemplateName(String templateId, String objectId);

    boolean init(List<GraphTemplateLegendSelectDto> list);
}
