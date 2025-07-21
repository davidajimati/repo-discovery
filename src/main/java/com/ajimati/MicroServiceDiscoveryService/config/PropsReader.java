package com.ajimati.MicroServiceDiscoveryService.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@RequiredArgsConstructor
public class PropsReader {
    @Value("${use.proxy}")
    private Boolean useProxy;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private Integer proxyPort;

    @Value("${azure.organization.projects}")
    private String[] azureOrganizations;
}
