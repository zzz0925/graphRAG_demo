// GraphitiService.java
package com.chat.service.impl;

import com.chat.dto.GraphitiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class GraphitiServiceImpl {

    @Value("${graphiti.api.url:https://your-graphiti-api.com}")
    private String graphitiApiUrl;

    @Value("${graphiti.api.key:your-api-key}")
    private String apiKey;

    private final WebClient webClient;

    public GraphitiServiceImpl() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public Mono<Boolean> saveToGraphiti(GraphitiRequest request) {
        return webClient.post()
                .uri(graphitiApiUrl + "/api/conversations")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    log.info("Graphiti保存成功: {}", response);
                    return true;
                })
                .onErrorResume(error -> {
                    log.error("Graphiti保存失败: ", error);
                    return Mono.just(false);
                });
    }
}