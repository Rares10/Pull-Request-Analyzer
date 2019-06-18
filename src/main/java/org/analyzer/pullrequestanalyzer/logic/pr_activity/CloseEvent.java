package org.analyzer.pullrequestanalyzer.logic.pr_activity;

import java.util.Date;

public class CloseEvent extends PullRequestEvent {

    long closeDevId;

    public CloseEvent(Date timestamp, String typeOfActivity, long developerId) {

        super(timestamp, typeOfActivity);
        this.closeDevId = developerId;
    }

    public String toString() {

        return typeOfActivity;
    }
}
