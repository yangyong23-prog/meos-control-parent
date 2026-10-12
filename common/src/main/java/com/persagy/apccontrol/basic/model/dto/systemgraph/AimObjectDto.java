package com.persagy.apccontrol.basic.model.dto.systemgraph;

import cn.hutool.core.annotation.PropIgnore;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import com.persagy.apccontrol.basic.model.dto.systemgraph.InfoProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

@ApiModel(
        value = "AimObjectDto",
        description = "对象返回值"
)
public class AimObjectDto {
    private String id;
    private String name;
    private String localId;
    private String localName;
    private String classCode;
    private String className;
    @ApiModelProperty("对象类型")
    private DmpObjTypeEnum objType;
    @ApiModelProperty("信息点")
    private String info;
    @PropIgnore
    @JsonIgnore
    @JSONField(
            serialize = false
    )
    private transient volatile JSONObject infoObject;

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @Nonnull
    public JSONObject getInfoObject() {
        if (this.infoObject == null) {
            synchronized (this) {
                if (this.infoObject == null) {
                    if (StringUtils.isNotBlank(this.info)) {
                        this.infoObject = JSON.parseObject(this.info);
                    } else {
                        this.infoObject = new JSONObject();
                    }
                }
            }
        }

        return this.infoObject;
    }

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @ApiModelProperty("id")
    public String getId() {
        return this.getInfoObject().getString(InfoProperty.ID.getCode());
    }

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @ApiModelProperty("名称")
    public String getName() {
        return this.getInfoObject().getString(InfoProperty.NAME.getCode());
    }

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @ApiModelProperty("本地编码")
    public String getLocalId() {
        return this.getInfoObject().getString(InfoProperty.LOCAL_ID.getCode());
    }

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @ApiModelProperty("本地名称")
    public String getLocalName() {
        return this.getInfoObject().getString(InfoProperty.LOCAL_NAME.getCode());
    }

    @JsonIgnore
    @JSONField(
            serialize = false,
            deserialize = false
    )
    @ApiModelProperty("类编码")
    public String getClassCode() {
        return this.getInfoObject().getString(InfoProperty.CLASS_CODE.getCode());
    }


    public AimObjectDto() {
    }

    public DmpObjTypeEnum getObjType() {
        return this.objType;
    }

    public String getInfo() {
        return this.info;
    }

    public AimObjectDto setId(final String id) {
        this.id = id;
        return this;
    }

    public AimObjectDto setName(final String name) {
        this.name = name;
        return this;
    }

    public AimObjectDto setLocalId(final String localId) {
        this.localId = localId;
        return this;
    }

    public AimObjectDto setLocalName(final String localName) {
        this.localName = localName;
        return this;
    }

    public AimObjectDto setClassCode(final String classCode) {
        this.classCode = classCode;
        return this;
    }

    public AimObjectDto setClassName(final String className) {
        this.className = className;
        return this;
    }

    public AimObjectDto setObjType(final DmpObjTypeEnum objType) {
        this.objType = objType;
        return this;
    }

    public AimObjectDto setInfo(final String info) {
        this.info = info;
        return this;
    }



}
