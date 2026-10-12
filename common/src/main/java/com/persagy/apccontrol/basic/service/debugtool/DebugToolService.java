package com.persagy.apccontrol.basic.service.debugtool;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectByClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectInfoCodesQueryParam;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolTemplateTypeAndClassCodeVO;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolInfoCodeConfigStatusVO;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 调试工具服务
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
public interface DebugToolService {

    /**
     * 查询模版类型和设备类编码
     *
     * @return 模版类型和设备类编码集合
     * @since 7.250102.0
     */
    Collection<DebugToolTemplateTypeAndClassCodeVO> listTemplateTypeAndClassCodes();

    /**
     * 根据设备类编码查询对象
     *
     * @param param 查询参数
     * @return 对象
     * @since 7.250102.0
     */
    Collection<DtpRwdObjectDTO> listObjectsByClassCode(@NonNull @Valid DebugToolObjectByClassCodeQueryParam param);

    /**
     * 根据对象id和模板id集合查询对象信息点配置以及bdtp相关信息的状态
     *
     * @param param 查询参数
     * @return 对象信息点配置以及bdtp相关信息的状态
     */
    Collection<DebugToolInfoCodeConfigStatusVO> queryObjectInfoCodes(DebugToolObjectInfoCodesQueryParam param);

}