package com.ajimati.MicroServiceDiscoveryService.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CommitsResponse {
    private ArrayList<Values> value;
    private Integer count;
    private String documentationUrl;

    @Data
    public static class Values {
        private Author author;
    }

    @Data
    public static class Author {
        private String name;
        private String email;
    }
}
