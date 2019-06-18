package org.analyzer.pullrequestanalyzer.dto;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class DTORegistry {

    private List<DeveloperDTO> developerDTOS;
    private List<PullRequestDTO> pullRequestDTOS;
    private List<CommentDTO> commentDTOS;
    private List<CommitDTO> commitDTOS;
    private List<FileDTO> fileDTOS;
    private List<DeveloperAnalysisDTO> developerAnalysisDTOS;
    private PullRequestAnalysisDTO pullRequestAnalysisDTO;

    public PullRequestDTO getPullRequestById(long id) {
        return pullRequestDTOS.stream()
                .filter(x -> x.getPullRequestId().equals(id))
                .findAny().orElse(null);
    }

    public DeveloperDTO getDeveloperById(long id) {

        return developerDTOS.stream()
                .filter(x -> x.getDeveloperId().equals(id))
                .findAny().orElse(null);
    }

    public DeveloperDTO getDeveloperByUsername(String username) {

        return developerDTOS.stream()
                .filter(x -> x.getUsername().equals(username))
                .findAny().orElse(null);
    }
}
