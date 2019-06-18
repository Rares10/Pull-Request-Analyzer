package org.analyzer.pullrequestanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.DeveloperActivity;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeveloperAnalysisDTO {

    //Stefania
    private long developerId;
    private int openedPR;
    private long mergedPRByCreator;
    private long mergedPRByOthers;
    private long mergedPROfOthers;
    private long closedWithoutMergePR;
    private long longTimeToMergePR;
    private long openForALongTimePR;
    private long noOfComplexPR;
    private List<Integer> noOfCommentsOfCreatedPR;
    private double meanNoOfCommentsOfCreatedPR;
    private int medianValueOfCommentsOfCreatedPR;
    private long commentsOnCreatedPR;
    private long commentsOnOtherPR;
    private List<Integer> noOfCommits;
    private double meanNoOfCommits;
    private int medianValueOfCommits;
    private List<Integer> noOfChangedFiles;
    private double meanNoOfChangedFiles;
    private int medianValueOfChangedFiles;
    private List<Double> reactionTimesOnPR;
    private double meanReactionTimeOnPR;
    private double medianValueOfReactionTimesOnPR;
    private List<Double> mergeTimesOfCreatedPR;
    private double meanMergeTimesOfCreatedPR;
    private double medianValueOfMergeTimesOfCreatedPR;
    private List<Double> responseTimesOnComments;
    private double meanResponseTimesOnComments;
    private double medianValueOfResponseTimesOnComments;
    private List<DeveloperActivity> timeline;
    private Map<String, Integer> interactions;
}
