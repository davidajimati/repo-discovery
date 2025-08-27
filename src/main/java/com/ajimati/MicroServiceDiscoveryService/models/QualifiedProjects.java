package com.ajimati.MicroServiceDiscoveryService.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class QualifiedProjects {
    private Integer count;
    private ArrayList<Match> match;

    @Data
    public static class Match {
        private String projectName;
        private String developerName;
        private String developerEmail;
        private String developerTeam;
        private String repoUrl;
        private String documentationUrl;
    }
}
