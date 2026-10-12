package com.persagy.apccontrol.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "persagy.web.url", ignoreInvalidFields = true)
public class PersagyWebConfig {

    @JsonAlias(value = "dtp-rwd-server")
    private String dtpRwdServer;


}
