package com.ajimati.MicroServiceDiscoveryService.service;

import com.ajimati.MicroServiceDiscoveryService.client.WebClientClass;
import com.ajimati.MicroServiceDiscoveryService.config.PropsReader;
import com.ajimati.MicroServiceDiscoveryService.exception.CustomRuntimeException;
import com.ajimati.MicroServiceDiscoveryService.models.CommitsResponse;
import com.ajimati.MicroServiceDiscoveryService.models.QualifiedProjects;
import com.ajimati.MicroServiceDiscoveryService.models.RepoSearchResponse;
import com.ajimati.MicroServiceDiscoveryService.models.contract.RedboxResponseContract;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ajimati.MicroServiceDiscoveryService.enums.Responses.INVALID_ORG_CONFIG;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryService {
    private final PropsReader props;
    private final WebClientClass webClient;

    private static QualifiedProjects.Match getQualifiedProjects(String userType, RepoSearchResponse.Values value) {
        QualifiedProjects.Match match = new QualifiedProjects.Match();
        match.setProjectName(value.getName());
        match.setDocumentationUrl(value.getCommitsResponse().getDocumentationUrl());
        match.setDeveloperName(value.getCommitsResponse().getValue().get(0).getAuthor().getName());
        match.setDeveloperEmail(value.getCommitsResponse().getValue().get(0).getAuthor().getEmail());
        if (userType.equalsIgnoreCase("ADMIN"))
            match.setRepoUrl(value.getWebUrl());
        return match;
    }

    @SneakyThrows
    public ResponseEntity<RedboxResponseContract> findAllRecords() {
        log.info("Starting parallel fetch for all records...");

        Set<String> azureOrganizations = Set.of(props.getAzureOrganizations());
        log.info("{} organizations found", azureOrganizations.size());

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(azureOrganizations.size(), 10));
        ConcurrentLinkedQueue<QualifiedProjects.Match> allQualifiedProjects = new ConcurrentLinkedQueue<>();

        List<CompletableFuture<Void>> futures = azureOrganizations.stream()
                .map(orgInfo -> CompletableFuture.runAsync(() -> {
                    try {
                        var creds = getOrgAndPat(orgInfo);
                        String orgName = creds.get("orgName");
                        String personalAccessToken = creds.get("orgPat");
                        String encodedPat = encodePat(personalAccessToken);

                        String repoSearchUrl = String.format("https://dev.azure.com/%s/_apis/git/repositories?api-version=7.1", orgName);
                        String encodedUrl = new URI(repoSearchUrl.replace(" ", "%20")).toASCIIString();
                        RepoSearchResponse repoSearchResponse = webClient.makeHttpCall(encodedUrl, encodedPat, orgName, RepoSearchResponse.class);

                        if (repoSearchResponse == null || repoSearchResponse.getValue() == null) {
                            log.warn("No repositories for org: {}", orgName);
                            return;
                        }

                        repoSearchResponse.getValue().stream()
                                .flatMap(repo -> {
                                    try {
                                        String commitUrl = String.format("https://dev.azure.com/%s/%s/_apis/git/repositories/%s/commits?$top=1&api-version=7.1-preview.1",
                                                orgName, repo.getProject().getName(), repo.getName());
                                        String encodedCommitUrl = new URI(commitUrl.replace(" ", "%20")).toASCIIString();

                                        CommitsResponse commit = webClient.makeHttpCall(encodedCommitUrl, encodedPat, orgName, CommitsResponse.class);
                                        if (commit != null && commit.getValue() != null && !commit.getValue().isEmpty()) {
                                            commit.setDocumentationUrl("https://docs.testcloud.com/" + repo.getName());
                                            repo.setCommitsResponse(commit);
                                            repo.setOrgName(orgName);
                                            repo.setOrgPat(personalAccessToken);
                                            return Stream.of(repo);
                                        }
                                    } catch (Exception e) {
                                        log.error("Failed to fetch commit for repo {}: {}", repo.getName(), e.getMessage());
                                    }
                                    return Stream.empty();
                                })
                                .map(repo -> getQualifiedProject(repo, "ADMIN"))
                                .forEach(allQualifiedProjects::add);

                        log.info("Fetched and streamed projects for {}", orgName);
                    } catch (Exception e) {
                        log.error("Failed to fetch data for org {}: {}", orgInfo, e.getMessage(), e);
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        if (allQualifiedProjects.isEmpty()) {
            log.warn("No repo commits found after parallel fetch.");
            throw new CustomRuntimeException("00", "Success", new ArrayList<>(), OK);
        }

        QualifiedProjects qualifiedProjects = new QualifiedProjects();
        qualifiedProjects.setMatch(new ArrayList<>(allQualifiedProjects));
        qualifiedProjects.setCount(allQualifiedProjects.size());

        log.info("Matched services count: {}", qualifiedProjects.getMatch().size());
        return new ResponseEntity<>(new RedboxResponseContract(qualifiedProjects), OK);
    }

    private String encodePat(String pat) {
        return Base64.getEncoder().encodeToString((":" + pat).getBytes(StandardCharsets.UTF_8));
    }

    private HashMap<String, String> getOrgAndPat(String orgInfo) {
        HashMap<String, String> orgCredentials = new HashMap<>();
        try {
            orgCredentials.put("orgName", orgInfo.split(":")[0]);
            orgCredentials.put("orgPat", orgInfo.split(":")[1]);
        } catch (Exception e) {
            log.info("error parsing organization credentials");
            throw new CustomRuntimeException(INVALID_ORG_CONFIG);
        }
        return orgCredentials;
    }

    @SneakyThrows
    public ResponseEntity<RedboxResponseContract> findRecordByName(String serviceName) {
        log.info("findRecordByName...");
        Set<String> azureOrganizations = Set.of(props.getAzureOrganizations());
        log.info("{} organizations found.", azureOrganizations.size());

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(azureOrganizations.size(), 1));
        ConcurrentLinkedQueue<QualifiedProjects.Match> allMatchedProjects = new ConcurrentLinkedQueue<>();

        List<CompletableFuture<Void>> futures = azureOrganizations.stream()
                .map(orgInfo -> CompletableFuture.runAsync(() -> {
                    try {
                        var creds = getOrgAndPat(orgInfo);
                        String orgName = creds.get("orgName");
                        String personalAccessToken = creds.get("orgPat");
                        String encodedPat = encodePat(personalAccessToken);

                        String repoSearchUrl = String.format("https://dev.azure.com/%s/_apis/git/repositories?api-version=7.1", orgName);
                        String encodedUrl = new URI(repoSearchUrl.replace(" ", "%20")).toASCIIString();

                        RepoSearchResponse repoSearchResponse = webClient.makeHttpCall(encodedUrl, encodedPat, orgName, RepoSearchResponse.class);
                        if (repoSearchResponse == null || repoSearchResponse.getValue() == null) {
                            log.warn("No repositories for org: {}", orgName);
                            return;
                        }

                        repoSearchResponse.setOrgName(orgName);
                        repoSearchResponse.setOrgPat(personalAccessToken);

                        executeSearchByName(repoSearchResponse, serviceName).stream()
                                .flatMap(this::getCommits) // commits is streamed here
                                .map(item -> getQualifiedProject(item, "DEV")) // process and transform one-by-one
                                .filter(Objects::nonNull)
                                .forEach(allMatchedProjects::add);

                        log.info("Projects added for org: {}", orgName);
                    } catch (Exception e) {
                        log.error("Error occurred during org processing", e);
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        log.info("Returning {} matched services\n\n", allMatchedProjects.size());

        QualifiedProjects qualifiedProjects = new QualifiedProjects();
        qualifiedProjects.setMatch(new ArrayList<>(allMatchedProjects));
        qualifiedProjects.setCount(allMatchedProjects.size());

        return new ResponseEntity<>(new RedboxResponseContract(qualifiedProjects), OK);
    }

    private QualifiedProjects.Match getQualifiedProject(RepoSearchResponse.Values value, String userType) {
        if (value == null || value.getCommitsResponse() == null || value.getCommitsResponse().getValue().isEmpty()) {
            return null;
        }
        return getQualifiedProjects(userType, value);
    }

    private Stream<RepoSearchResponse.Values> getCommits(RepoSearchResponse.Values item) {
        try {
            String personalAccessToken = ":" + item.getOrgPat();
            String encodedPat = Base64.getEncoder().encodeToString(personalAccessToken.getBytes(StandardCharsets.UTF_8));

            String commitUrl = "https://dev.azure.com/" + item.getOrgName() + "/" + item.getProject().getName() +
                    "/_apis/git/repositories/" + item.getName() + "/commits?$top=1&api-version=7.1-preview.1";
            String encodedUrl = new URI(commitUrl.replace(" ", "%20")).toASCIIString();

            CommitsResponse commitsResponse = webClient.makeHttpCall(encodedUrl, encodedPat, item.getOrgName(), CommitsResponse.class);
            if (commitsResponse != null && commitsResponse.getValue() != null && !commitsResponse.getValue().isEmpty()) {
                commitsResponse.setDocumentationUrl("https://docs.testcloud.com/" + item.getName());
                item.setCommitsResponse(commitsResponse);
            } else {
                log.info("no commits in: {}", item.getProject().getName());
            }

            return Stream.of(item);
        } catch (Exception e) {
            log.error("Error fetching commits", e);
            return Stream.empty();
        }
    }

    private List<RepoSearchResponse.Values> executeSearchByName(RepoSearchResponse repoSearchResponses, String serviceName) {
        String normalizedInput = normalize(serviceName);
        int maxDistance = 5;
        LevenshteinDistance distance = new LevenshteinDistance();

        return repoSearchResponses.getValue().stream()
                .filter(item -> {
                    String repoName = item.getName();
                    if (repoName == null)
                        return false;

                    String normalizedRepo = normalize(repoName);
                    return normalizedRepo.contains(normalizedInput) ||
                            distance.apply(normalizedRepo, normalizedInput) <= maxDistance;
                })
                .peek(item -> {
                    item.setOrgName(repoSearchResponses.getOrgName());
                    item.setOrgPat(repoSearchResponses.getOrgPat());
                })
                .collect(Collectors.toList());
    }

    private String normalize(String input) {
        return input.toLowerCase().replaceAll("[-_\\s]", "");
    }
}
