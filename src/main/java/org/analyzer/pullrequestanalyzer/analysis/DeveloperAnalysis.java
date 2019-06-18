package org.analyzer.pullrequestanalyzer.analysis;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.DeveloperActivity;
import org.analyzer.pullrequestanalyzer.registry.Developer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Data
@Service
public class DeveloperAnalysis {

    private Developer developer;
    private int openedPR;
    private long mergedPRByCreator;
    private long mergedPRByOthers;
    private long mergedPROfOthers;
    private long closedWithoutMergePR;
    private long longTimeToMergePR;
    private long openForALongTimePR;
    private long noOfComplexPR;
    private List<Integer> noOfCommentsOfCreatedPR;
    private long commentsOnCreatedPR;
    private long commentsOnOtherPR;
    private List<Integer> noOfCommits;
    private List<Integer> noOfChangedFiles;
    private List<Long> reactionTimesOnPR;
    private List<Long> mergeTimesOfCreatedPR;
    private List<Double> responseTimesOnComments;
    private List<DeveloperActivity> timeline;
    private Map<String, Integer> interactions;
}