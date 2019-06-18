package org.analyzer.pullrequestanalyzer.logic.dev_activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperActivity {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    protected Date timestamp;
    protected String typeOfActivity;
}












