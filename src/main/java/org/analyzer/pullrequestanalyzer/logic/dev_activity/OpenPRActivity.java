package org.analyzer.pullrequestanalyzer.logic.dev_activity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.analyzer.pullrequestanalyzer.registry.PullRequest;

import java.util.Date;

@Data
public class OpenPRActivity extends DeveloperActivity {

    private long pullRequestId;

    public OpenPRActivity(Date timestamp, String typeOfActivity, long pullRequestId){
        super(timestamp, typeOfActivity);
        this.pullRequestId = pullRequestId;
    }

    public String toString() { return typeOfActivity; }
}