package org.analyzer.pullrequestanalyzer.logic.dev_activity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.analyzer.pullrequestanalyzer.registry.Comment;

import java.util.Date;

@Data
public class CommentActivity extends DeveloperActivity {

    private long commentId;

    public CommentActivity(Date timestamp, String typeOfActivity, long commentId) {
        super(timestamp, typeOfActivity);
        this.commentId = commentId;
    }

    public String toString() {
        return typeOfActivity;
    }
}