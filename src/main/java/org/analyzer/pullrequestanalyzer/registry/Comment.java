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
public class Comment {

    private Long commentId;
    private Developer author;
    private PullRequest pullRequest;
    private Date timestamp;
    private Long parentId;
    private String file;
    private int lineOfCode;
    private String content;

    long getTimeSincePRCreation() {

        if (timestamp.getTime() > this.getPullRequest().getCreationDate().getTime())
            return timestamp.getTime() - this.getPullRequest().getCreationDate().getTime();
        else
            return this.getPullRequest().getCreationDate().getTime() - timestamp.getTime();
    }
}
