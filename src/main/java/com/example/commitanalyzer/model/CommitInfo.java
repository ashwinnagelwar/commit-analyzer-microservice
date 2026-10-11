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
public class CommitInfo {
    private String sha;
    private String message;
    private String committedAt;
    private List<String> files;
}
