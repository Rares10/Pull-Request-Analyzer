package org.analyzer.pullrequestanalyzer.controller;

import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.analyzer.pullrequestanalyzer.dto.PullRequestAnalysisDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pr_analysis")
public class PullRequestAnalysisController {

    private DTORegistry dtoRegistry;

    public PullRequestAnalysisController(DTORegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
    }

    @GetMapping("")
    public PullRequestAnalysisDTO getPRAnalysis() {
        return dtoRegistry.getPullRequestAnalysisDTO();
    }
}
