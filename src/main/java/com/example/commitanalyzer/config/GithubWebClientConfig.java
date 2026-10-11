package com.example.commitanalyzer.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class GithubWebClientConfig {

    @Bean
    public WebClient githubWebClient(GithubProperties githubProperties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, githubProperties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(githubProperties.getReadTimeoutMs()))
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(githubProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(githubProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS));
                });

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(githubProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "commit-analyzer")
                .clientConnector(new reactor.netty.http.client.HttpClientConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build());

        if (StringUtils.hasText(githubProperties.getToken())) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.getToken());
        }

        return builder.build();
    }
}
