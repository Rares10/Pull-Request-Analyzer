package org.analyzer.pullrequestanalyzer.logic.analyzer;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.analysis.DeveloperAnalysis;
import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.analyzer.pullrequestanalyzer.registry.DeveloperRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Data
@Service
public class DataAnalyzer {

    private DeveloperAnalyzer developerAnalyzer;
    private PullRequestAnalyzer pullRequestAnalyzer;
    private ProjectData projectToAnalyze;
    private List<DeveloperAnalysis> developerAnalyses;
    private PullRequestAnalysis pullRequestAnalysis;

    @Autowired
    public DataAnalyzer(DeveloperAnalyzer developerAnalyzer,
                        PullRequestAnalyzer pullRequestAnalyzer) {

        this.developerAnalyzer = developerAnalyzer;
        this.pullRequestAnalyzer = pullRequestAnalyzer;
    }

    public void analyzeData() throws IOException {

        System.out.println("Started analysis at " + new Date());
        developerAnalyses = developerAnalyzer.run(projectToAnalyze);
        pullRequestAnalysis = pullRequestAnalyzer.run(projectToAnalyze);
        System.out.println("Finished analysis at " + new Date());
    }
}

