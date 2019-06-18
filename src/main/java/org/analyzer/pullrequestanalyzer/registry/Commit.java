package org.analyzer.pullrequestanalyzer.registry;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Commit {

    private Long commitId;
    private Developer author;
    private PullRequest pullRequest;
    private Date timestamp;
    private String message;
}
