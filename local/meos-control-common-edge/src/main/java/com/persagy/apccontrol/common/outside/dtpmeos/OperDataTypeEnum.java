package com.persagy.apccontrol.common.outside.dtpmeos;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据操作类型
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/4/03 10:30
 */
@Getter
@AllArgsConstructor
public enum OperDataTypeEnum implements BaseEnum<String>, IEnum<String> {
    
    ADD_OPER("ADD_OPER", "key_6cc5c1e16b074d669b45cdbd4f5a5d91"),
    
    EDIT_OPER("EDIT_OPER", "key_ddc82084c21541ea820840e9338664b9"),
    
    DEL_OPER("DEL_OPER", "key_c6652b85ecbe4dd6ba39de79ecdee66a"),
    
    BATCH_DEL_OPER("BATCH_DEL_OPER", "key_ae63585530b843ee84c384e540ef058d"),
    
    ;
    
    private final String value;
    
    private final String reasonPhrase;
    
    
    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    
}
