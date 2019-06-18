package org.analyzer.pullrequestanalyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Developer")
public class DeveloperData {

    @Id
    private Long developerId;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private ProjectData project;

    private String username;
    private String name;
    private String email;

    @OneToMany(mappedBy = "creatorDev", cascade = CascadeType.ALL)
    private List<PullRequestData> createdPRs;
    @OneToMany(mappedBy = "mergeDev", cascade = CascadeType.ALL)
    private List<PullRequestData> mergedPRs;
    @OneToMany(mappedBy = "author" , cascade = CascadeType.ALL)
    private List<CommentData> comments;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<CommitData> commits;
}