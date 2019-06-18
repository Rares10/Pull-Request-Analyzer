package org.analyzer.pullrequestanalyzer.logic.pr_activity;

import lombok.Data;

import java.util.Date;

@Data
public class FollowUpCommitEvent extends PullRequestEvent {

    private long commitId;
    private long authorId;

    public FollowUpCommitEvent(Date timestamp, String typeOfActivity, long commitId, long developerId) {

        super(timestamp, typeOfActivity);
        this.commitId = commitId;
        this.authorId = developerId;
    }

    public String toString() {

        return typeOfActivity;
    }
}
