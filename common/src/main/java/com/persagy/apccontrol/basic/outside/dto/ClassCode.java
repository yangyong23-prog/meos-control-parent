package com.persagy.apccontrol.basic.outside.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yuyao
 */
@Data
public class ClassCode {

   @Schema(description = "对象类型")
   @NotNull(message = "{key_86c480967d6542b0820033cce366d29a}")
   private String classCode;

   @Schema(description = "过滤信息点")
   private Set<String> infoCodes;

   public ClassCode() {

   }

   public ClassCode(String classCode) {
      this.classCode = classCode;
   }

   public ClassCode(String classCode, String infoCode) {
      this.classCode = classCode;
      this.infoCodes = new HashSet<>();
      infoCodes.add(infoCode);
   }

   public ClassCode(String classCode, Set<String> infoCodes) {
      this.classCode = classCode;
      this.infoCodes = infoCodes;
   }

}
