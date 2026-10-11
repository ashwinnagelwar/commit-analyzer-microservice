package com.example.commitanalyzer;

import com.example.commitanalyzer.config.GithubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GithubProperties.class)
public class CommitAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommitAnalyzerApplication.class, args);
    }
}
