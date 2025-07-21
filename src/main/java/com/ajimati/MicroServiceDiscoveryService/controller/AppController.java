package com.ajimati.MicroServiceDiscoveryService.controller;

import com.ajimati.MicroServiceDiscoveryService.models.ServiceName;
import com.ajimati.MicroServiceDiscoveryService.models.contract.RedboxResponseContract;
import com.ajimati.MicroServiceDiscoveryService.service.DiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AppController {
    private final DiscoveryService discoveryService;

    @GetMapping("/all-services")
    public ResponseEntity<RedboxResponseContract> fetchAllRepos() {
        return discoveryService.findAllRecords();
    }

    @PostMapping("/find-by-name")
    public ResponseEntity<RedboxResponseContract> findServiceByName(@RequestBody ServiceName serviceName) {
        return discoveryService.findRecordByName(serviceName.getName());
    }
}
