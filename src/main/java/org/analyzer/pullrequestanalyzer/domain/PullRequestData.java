package org.analyzer.pullrequestanalyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "PullRequest")
public class PullRequestData {

    @Id
    private Long pullRequestId;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private ProjectData project;

    private String title;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date mergeDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date closeDate;
    @ManyToOne
    @JoinColumn(name = "creatorId")
    private DeveloperData creatorDev;
    @ManyToOne
    @JoinColumn(name = "mergerId")
    private DeveloperData mergeDev;
    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<CommentData> comments;
    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<CommitData> commits;
    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    private List<FileData> filesModified;
    private String initBranch;
    private String finalBranch;

}
