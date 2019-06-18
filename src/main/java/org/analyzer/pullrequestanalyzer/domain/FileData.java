package org.analyzer.pullrequestanalyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "File")
public class FileData {

    @Id
    @GeneratedValue
    private Long fileId;
    private String fileName;
    @ManyToOne
    @JoinColumn(name = "pullRequestId")
    private PullRequestData pullRequest;
    private int additions;
    private int changes;
    private int deletions;
}
