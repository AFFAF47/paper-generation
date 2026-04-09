package com.example.exam.papergenerator.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;


@Configuration
public class OllamaConfig {

    @Bean
    public RestClientCustomizer proxyRestClientCustomizer() {
        return (restClientBuilder) -> {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(proxy);
            restClientBuilder.requestFactory(requestFactory);
        };
    }
}
