package org.analyzer.pullrequestanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDTO {

    private Long developerId;
    private String username;
    private String name;
    private String email;
    private List<Long> createdPullRequests;
    private List<Long> mergedPullRequests;
    private List<Long> comments;
    private List<Long> commits;
}