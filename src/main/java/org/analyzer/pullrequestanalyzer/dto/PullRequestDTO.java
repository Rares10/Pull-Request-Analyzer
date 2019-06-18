package org.analyzer.pullrequestanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.domain.DeveloperData;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullRequestDTO {

    private Long pullRequestId;
    private String title;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date creationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date mergeDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date closeDate;
    private Long creatorDev;
    private Long mergeDev;
    private List<Long> comments;
    private List<Long> commits;
    private List<Long> filesModified;
    private String initBranch;
    private String finalBranch;
}