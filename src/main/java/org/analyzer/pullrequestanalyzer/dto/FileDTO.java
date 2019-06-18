package org.analyzer.pullrequestanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {

    private Long fileId;
    private String fileName;
    private Long pullRequest;
    private int additions;
    private int changes;
    private int deletions;
}
