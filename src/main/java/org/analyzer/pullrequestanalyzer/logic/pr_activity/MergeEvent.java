package org.analyzer.pullrequestanalyzer.logic.pr_activity;

import java.util.Date;

public class MergeEvent extends PullRequestEvent {

    private long mergeDevId;

    public MergeEvent(Date timestamp, String typeOfActivity, long developerId) {

        super(timestamp, typeOfActivity);
        this.mergeDevId = developerId;
    }

    public String toString() {

        return typeOfActivity;
    }
}
