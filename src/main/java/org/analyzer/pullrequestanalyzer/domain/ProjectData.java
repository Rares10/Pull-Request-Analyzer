package org.analyzer.pullrequestanalyzer.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Project")
public class ProjectData {

    @Id
    private Long projectId;
    private String repository;
    private String owner;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<DeveloperData> developers;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<PullRequestData> pullRequests;
}
