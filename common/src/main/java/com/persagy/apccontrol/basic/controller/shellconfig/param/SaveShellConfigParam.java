package com.persagy.apccontrol.basic.controller.shellconfig.param;

import com.persagy.apccontrol.basic.model.entity.ShellConfigBasic;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/8 6:02 PM
 */
@Data
public class SaveShellConfigParam {

    @NotEmpty(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @NotEmpty(message = "{key_f50f6fb295a841508be6af5ad04431b6}")
    private String scene;

    @NotEmpty(message = "{key_22e4a3a24ab846eea53085254bdd7a2a}")
    private List<ShellConfigGroup> groups;

    @Data
    public static class ShellConfigGroup {
        @NotBlank(message = "{key_515ca07e23bd4e0b9625749e904ef603}")
        private String group;

        @Nullable
        private String tab;

        @NotEmpty(message = "{key_57c4ef1df516476c811614d568b50f19}")
        private List<ShellConfigBasic> items;
    }
}
