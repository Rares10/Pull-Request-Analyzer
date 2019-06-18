package org.analyzer.pullrequestanalyzer.logic.pr_activity;

import lombok.Data;

import java.util.Date;

@Data
public class OpenEvent extends PullRequestEvent {

    private long creatorDevId;

    public OpenEvent(Date timestamp, String typeOfActivity, long developerId) {

        super(timestamp, typeOfActivity);
        creatorDevId = developerId;
    }

    public String toString() {
        return typeOfActivity;
    }
}
