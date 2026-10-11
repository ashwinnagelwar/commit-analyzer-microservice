package com.example.commitanalyzer.controller;

import com.example.commitanalyzer.model.AnalysisResponse;
import com.example.commitanalyzer.service.CommitAnalysisService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CommitAnalysisController {

    private final CommitAnalysisService commitAnalysisService;

    public CommitAnalysisController(CommitAnalysisService commitAnalysisService) {
        this.commitAnalysisService = commitAnalysisService;
    }

    @GetMapping("/commits/analyze")
    public AnalysisResponse analyzeCommits(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return commitAnalysisService.analyzeCommits(owner, repo, from, to);
    }
}
