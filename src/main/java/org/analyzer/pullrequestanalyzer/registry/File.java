package org.analyzer.pullrequestanalyzer.registry;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class File {
    private Long fileId;
    private String fileName;
    private PullRequest pullRequest;
    private int additions;
    private int changes;
    private int deletions;
}
