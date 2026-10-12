package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisInfoCodeVO {
    /**
     * 信息点编码
     */
    @NonNull
    private String code;

    /**
     * 信息点名称
     */
    @NonNull
    private String name;

    /**
     * 信息点别名
     */
    private String aliasName;

    /**
     * 类型编码
     */
    @NonNull
    private String classCode;

    /**
     * 单位
     */
    @Nullable
    private String unit;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 数据源
     */
    private List<InfoCodeDataSourceDefineModel> dataSources;


    /**
     * 填充信息点信息
     *
     * @param infoCodeAndDefineModel 信息编码和定义模型
     */
    public void fillByInfoCodeAndDefineModel(InfoCodeDefineModel infoCodeAndDefineModel) {
        if (infoCodeAndDefineModel == null) {
            return;
        }
        this.code = infoCodeAndDefineModel.getCode();
        this.name = infoCodeAndDefineModel.getName();
        this.aliasName = infoCodeAndDefineModel.getAliasName();
        this.classCode = infoCodeAndDefineModel.getClassCode();
        this.unit = infoCodeAndDefineModel.getUnit();
        this.dataSources = infoCodeAndDefineModel.getDataSources();
    }

    /**
     * 填充组信息
     *
     * @param groupModel 群体模型
     */

    public void fillByGroupModel(InfoCodeGroupModel groupModel) {
        if (groupModel == null) {
            return;
        }
        this.groupCode = groupModel.getGroupCode();
        this.groupName = groupModel.getGroupName();
    }

}
