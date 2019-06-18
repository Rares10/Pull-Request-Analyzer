package org.analyzer.pullrequestanalyzer.controller;

import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.analyzer.pullrequestanalyzer.dto.DeveloperAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dev_analysis")
public class DeveloperAnalysisController {

    private DTORegistry dtoRegistry;

    @Autowired
    public DeveloperAnalysisController(DTORegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
    }

    @GetMapping("")
    public List<DeveloperAnalysisDTO> getDevAnalysis() {

        return dtoRegistry.getDeveloperAnalysisDTOS();
    }
}
