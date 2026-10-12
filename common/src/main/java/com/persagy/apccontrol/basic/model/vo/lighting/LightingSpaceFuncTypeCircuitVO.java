package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 照明空间的回路功能类型和关联的控制模块
 *
 */
@Data
@ToString
@Schema(title = "照明空间的回路功能类型和关联的控制模块")
@NoArgsConstructor
@AllArgsConstructor
public class LightingSpaceFuncTypeCircuitVO {

    @Schema(description = "照明回路功能类型")
    private String funcType;

    @Schema(description = "照明回路功能类型名称")
    private String funcTypeName;
    
    @Schema(description = "回路灯具类型")
    private List<LampFuncType> lampFuncTypeList;
    
    /**
     * 回路灯具类型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LampFuncType{
        @Schema(description = "照明回路灯具功能类型")
        private String lampFuncType;
        
        @Schema(description = "照明回路灯具功能类型名称")
        private String lampFuncTypeName;
        
        @Schema(description = "回路列表")
        private  List<BasicObj> circuitList;
     }
    
    /**
     * 基础对象类型
     */
    @Data
    public static class BasicObj{
        
        @Schema(description = "对象 ID")
        private String id;
        
        @Schema(description = "本地编码")
        private String localId;
        
        @Schema(description = "本地名称")
        private String localName;
        
        @Schema(description = "照明回路功能类型")
        private String funcType;
        
        @Schema(description = "照明回路功能类型名称")
        private String funcTypeName;
        
        @Schema(description = "照明回路灯具功能类型")
        private String lampFuncType;
        
        @Schema(description = "照明回路灯具功能类型名称")
        private String lampFuncTypeName;
        
        @Schema(description = "照明回路配置模式")
        private DemandLightingCircuitSettingModeEnum settingMode;
        
        @Schema(description = "关联控制模块")
        private List<DtpRwdObjectDTO> relController;

        @Schema(description = "关联 DDC")
        private List<DtpRwdObjectDTO> relDDC;
        
        @Schema(description = "服务的空间")
        private List<DtpRwdSpaceDTO>  serviceSpace;
        
        /**
         * 为空显示未知
         *
         * @return
         */
        public String getFuncType() {
            if (StringUtils.isEmpty(funcType)) {
                return CommonConst.NULL_STRING_VO;
            }
            return funcType;
        }
        
        /**
         * 为空显示未知
         *
         * @return
         */
        public String getLampFuncType() {
            if (StringUtils.isEmpty(lampFuncType)) {
                return CommonConst.NULL_STRING_VO;
            }
            return lampFuncType;
        }
    }

}
