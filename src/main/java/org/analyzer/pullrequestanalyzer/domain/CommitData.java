package org.analyzer.pullrequestanalyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Commit")
public class CommitData {

    @Id
    @GeneratedValue
    private Long commitId;
    @ManyToOne
    @JoinColumn(name = "developerId")
    private DeveloperData author;
    @ManyToOne
    @JoinColumn(name = "pullRequestId")
    private PullRequestData pullRequest;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(columnDefinition = "TEXT")
    private String message;
}
