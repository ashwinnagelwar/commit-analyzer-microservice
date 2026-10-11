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
public class FileChangeSummary {
    private String path;
    private String status;
    private int additions;
    private int deletions;
    private int totalChangedLines;
    private List<String> patchFragments;
    private List<String> commitShas;
    private List<String> commitMessages;
}
