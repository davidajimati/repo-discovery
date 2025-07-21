package com.ajimati.MicroServiceDiscoveryService.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class RepoSearchResponse {
    private ArrayList<Values> value;
    private Integer count;
    private String orgName;
    private String orgPat;

    @Data
    public static class Values {
        private String id;
        private String orgName;
        private String orgPat;
        private String name;
        private String webUrl;
        private Project project;
        private CommitsResponse commitsResponse;
    }

    @Data
    public static class Project {
        private String id;
        private String name;
    }
}
