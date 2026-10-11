package com.example.commitanalyzer.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponse {
    private String owner;
    private String repo;
    private String from;
    private String to;
    private int totalCommits;
    private int totalFilesChanged;
    private List<CommitInfo> commits;
    private List<FileChangeSummary> fileChanges;
}
