package org.analyzer.pullrequestanalyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Comment")
public class CommentData {
    @Id
    private Long commentId;
    @ManyToOne
    @JoinColumn(name = "developerId")
    private DeveloperData author;
    @ManyToOne
    @JoinColumn(name = "pullRequestId")
    private PullRequestData pullRequest;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    private Long parentId;
    private String file;
    private int lineOfCode;
    @Column(columnDefinition = "TEXT")
    private String content;
}
