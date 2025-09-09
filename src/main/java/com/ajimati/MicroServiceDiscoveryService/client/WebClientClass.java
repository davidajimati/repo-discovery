package com.ajimati.MicroServiceDiscoveryService.client;

import com.ajimati.MicroServiceDiscoveryService.config.PropsReader;
import com.ajimati.MicroServiceDiscoveryService.exception.CustomRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class WebClientClass {
    private final PropsReader props;
    private final ObjectMapper objectMapper;

    public <R> R makeHttpCall(String url, String encodedPat, String org, Class<R> rClass) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedPat)
                .GET()
                .build();

        HttpClient client = props.getUseProxy() ? httpClientWithProxy() : httpClientWithoutProxy();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("interrupted exception occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
        if (response.statusCode() == 401 || response.statusCode() == 403)
            throw new CustomRuntimeException("99", "Azure PersonalAccessToken expired or Access Denied for '" + org + "'",
                    "Contact administrator", HttpStatus.UNAUTHORIZED);
        if (response.body() == null || response.body().isEmpty())
            return null;
        R apiResponse = null;
        try {
            apiResponse = objectMapper.readValue(response.body(), rClass);
        } catch (JsonProcessingException e) {
            System.out.println("error processing JSON body for: " + org);
            System.out.println("API response body: " + response.body());
        }
        return apiResponse;
    }

    private HttpClient httpClientWithoutProxy() {
        return HttpClient.newBuilder().build();
    }

    private HttpClient httpClientWithProxy() {
        return HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(props.getProxyHost(), props.getProxyPort())))
                .build();
    }
}
