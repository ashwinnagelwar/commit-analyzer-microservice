package com.example.commitanalyzer.service;

import com.example.commitanalyzer.model.AnalysisResponse;
import com.example.commitanalyzer.model.CommitInfo;
import com.example.commitanalyzer.model.FileChangeSummary;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommitAnalysisService {

    private final WebClient githubWebClient;

    public CommitAnalysisService(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    public AnalysisResponse analyzeCommits(String owner, String repo, LocalDate fromDate, LocalDate toDate) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner is required");
        }
        if (repo == null || repo.isBlank()) {
            throw new IllegalArgumentException("repo is required");
        }
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("from date must be before or equal to to date");
        }

        String since = DateTimeFormatter.ISO_INSTANT.format(fromDate.atStartOfDay(ZoneOffset.UTC).toInstant());
        String until = DateTimeFormatter.ISO_INSTANT.format(toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        List<JsonNode> commitNodes = fetchCommitList(owner, repo, since, until);
        List<CommitInfo> commits = new ArrayList<>();
        Map<String, FileChangeSummary> fileSummaries = new LinkedHashMap<>();

        for (JsonNode commitNode : commitNodes) {
            String sha = commitNode.path("sha").asText();
            if (sha.isBlank()) {
                continue;
            }

            JsonNode commitDetail = fetchCommitDetail(owner, repo, sha);
            String message = commitDetail.path("commit").path("message").asText();
            String committedAt = commitDetail.path("commit").path("author").path("date").asText();

            List<String> filePaths = new ArrayList<>();
            JsonNode filesNode = commitDetail.path("files");
            if (filesNode != null && filesNode.isArray()) {
                for (JsonNode fileNode : filesNode) {
                    String filePath = fileNode.path("filename").asText();
                    if (filePath.isBlank()) {
                        continue;
                    }
                    filePaths.add(filePath);

                    FileChangeSummary summary = fileSummaries.computeIfAbsent(filePath, key -> FileChangeSummary.builder()
                            .path(filePath)
                            .status(fileNode.path("status").asText())
                            .additions(0)
                            .deletions(0)
                            .totalChangedLines(0)
                            .patchFragments(new ArrayList<>())
                            .commitShas(new ArrayList<>())
                            .commitMessages(new ArrayList<>())
                            .build());

                    int additions = fileNode.path("additions").asInt();
                    int deletions = fileNode.path("deletions").asInt();
                    summary.setAdditions(summary.getAdditions() + additions);
                    summary.setDeletions(summary.getDeletions() + deletions);
                    summary.setTotalChangedLines(summary.getTotalChangedLines() + additions + deletions);
                    summary.setStatus(fileNode.path("status").asText());

                    String patch = fileNode.path("patch").asText();
                    if (!patch.isBlank()) {
                        summary.getPatchFragments().add(patch);
                    }
                    summary.getCommitShas().add(sha);
                    summary.getCommitMessages().add(message);
                }
            }

            commits.add(CommitInfo.builder()
                    .sha(sha)
                    .message(message)
                    .committedAt(committedAt)
                    .files(filePaths)
                    .build());
        }

        List<FileChangeSummary> sortedFileChanges = new ArrayList<>(fileSummaries.values());
        sortedFileChanges.sort(Comparator.comparingInt(FileChangeSummary::getTotalChangedLines).reversed());

        return AnalysisResponse.builder()
                .owner(owner)
                .repo(repo)
                .from(fromDate.toString())
                .to(toDate.toString())
                .totalCommits(commits.size())
                .totalFilesChanged(sortedFileChanges.size())
                .commits(commits)
                .fileChanges(sortedFileChanges)
                .build();
    }

    private List<JsonNode> fetchCommitList(String owner, String repo, String since, String until) {
        List<JsonNode> result = new ArrayList<>();
        int page = 1;

        while (true) {
            JsonNode pageResponse = githubWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/commits")
                            .queryParam("since", since)
                            .queryParam("until", until)
                            .queryParam("per_page", 100)
                            .queryParam("page", page)
                            .build(owner, repo))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new ResponseStatusException(response.statusCode(), "GitHub API request failed: " + body)))
                    .bodyToMono(JsonNode.class)
                    .block();

            if (pageResponse == null || !pageResponse.isArray() || pageResponse.isEmpty()) {
                break;
            }

            for (JsonNode item : pageResponse) {
                result.add(item);
            }

            if (pageResponse.size() < 100) {
                break;
            }
            page++;
        }

        return result;
    }

    private JsonNode fetchCommitDetail(String owner, String repo, String sha) {
        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/commits/{sha}", owner, repo, sha)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new ResponseStatusException(response.statusCode(), "GitHub commit detail request failed: " + body)))
                .bodyToMono(JsonNode.class)
                .block();
    }
}
