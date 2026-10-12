package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.param.validatedinterface.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class DemandInstanceGroupParam {

    @NotNull(message = "{key_13d8a6853d1a468c9c0ad1d609fa96eb}", groups = {UpdateGroup.class, DeleteGroup.class, CopyGroup.class})
    private Long id;
    /**
     * 编组名称
     */
    @NotBlank(message = "{key_7ad7003b45ae4f79ae7c4627aaed315f}", groups = {CreateGroup.class, CopyGroup.class, CheckGroup.class})
    @Size(max = 30, message = "{key_581ed865397343e59b025cd29f0b817c}")
    private String groupName;
    /**
     * 实例ID
     */
    @NotNull(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}", groups = {CreateGroup.class})
    private Set<String> instanceIds;
    /**
     * 实例类型
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}", groups = {CreateGroup.class})
    private InstanceTypeEnum instanceType;

    /**
     * 需求模版类型
     */
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}", groups = {CreateGroup.class, UpdateGroup.class, CheckGroup.class})
    private DemandTemplateTypeEnum templateType;

    /**
     * 编组用途 groups = {CreateGroup.class, UpdateGroup.class}
     */
    @Size(max = 50, message = "{key_f87b0c13aa854f878ee93610813f7a4e}")
    private String purpose;

    /**
     * 编组标签 groups = {CreateGroup.class, UpdateGroup.class, CheckGroup.class}
     */
    private String tag;

}
