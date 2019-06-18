package org.analyzer.pullrequestanalyzer.logic.pr_activity;

import lombok.Data;

import java.util.Date;

@Data
public class CommentEvent extends PullRequestEvent {

    private long commentId;
    private long authorId;

    public CommentEvent(Date timestamp, String typeOfActivity, long commentId, long developerId) {

        super(timestamp, typeOfActivity);
        this.commentId = commentId;
        this.authorId = developerId;
    }

    public String toString() {

        return typeOfActivity;
    }
}
