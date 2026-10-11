package com.example.commitanalyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class GithubProperties {

    private String baseUrl = "https://api.github.com";
    private String token = "";
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 15000;
}
