package de.muenchen.zammad.ldap.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "zammad")
public class ZammadProperties {

    private String token;
    private ZammadUrlProperties url;
    private Assignment assignment;

    public String getToken() {
        return "Token token=" + this.token;
    }

}
