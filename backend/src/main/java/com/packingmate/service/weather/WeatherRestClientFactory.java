package com.packingmate.service.weather;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class WeatherRestClientFactory {
    private WeatherRestClientFactory() {
    }

    static RestClient create(String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(4));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
