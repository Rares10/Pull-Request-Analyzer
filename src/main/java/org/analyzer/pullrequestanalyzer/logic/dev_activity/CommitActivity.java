package org.analyzer.pullrequestanalyzer.logic.dev_activity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.analyzer.pullrequestanalyzer.registry.Commit;

import java.util.Date;

@Data
public class CommitActivity extends DeveloperActivity {

    private long commitId;

    public CommitActivity(Date timestamp, String typeOfActivity, long commitId) {
        super(timestamp, typeOfActivity);
        this.commitId = commitId;
    }

    public String toString() {
        return typeOfActivity;
    }
}
