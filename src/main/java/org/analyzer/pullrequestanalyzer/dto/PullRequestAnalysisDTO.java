package org.analyzer.pullrequestanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.logic.pr_activity.PullRequestEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PullRequestAnalysisDTO {

    private String owner;
    private String projectName;
    private int openPRs;
    private int closedPRsWithoutMerge;
    private int mergedPRs;
    private int longTimeToMergePRs;
    private int openForALongTimePRs;
    private int prsWithoutActivity;
    private int complexPRs;
    private double meanOpenedPRsPerDay;
    private long medianValueOfOpenedPRsPerDay;
    private double meanOpenedPRsPerWeek;
    private long medianValueOfOpenedPRsPerWeek;
    private double meanMergedPRsPerDay;
    private long medianValueOfMergedPRsPerDay;
    private double meanMergedPRsPerWeek;
    private long medianValueOfMergedPRsPerWeek;
    private double meanNoOfCommentsPerPR;
    private int medianValueOfCommentsPerPR;
    private double meanNoOfCommentsOnCodePerPR;
    private long medianValueOfCommentsOnCodePerPR;
    private double meanNoOfSimpleCommentsPerPR;
    private long medianValueOfSimpleCommentsPerPR;
    private double meanNoOfCommitsPerPR;
    private int medianValueOfNoOfCommitsPerPR;
    private double meanNoOfFollowUpCommitsPerPR;
    private long medianValueOfFollowUpCommitsPerPR;
    private double meanNoOfChangedFilesPerPR;
    private int medianValueOfChangedFilesPerPR;
    private int branchBranchPRs;
    private double meanReactionTimesOnPRs;
    private double medianValueOfReactionTimesOnPRs;
    private double meanResponseTimesOfPROwner;
    private double medianValueOfResponseTimesOfPROwner;
    private double meanMergeTimesOfPRs;
    private double medianValueOfMergeTimesOfPRs;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    TreeMap<Date, Long> crowdedDays;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    TreeMap<Date, Long> crowdedTwoDays;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    TreeMap<Date, Long> crowdedThreeDays;
    Map<Long, List<PullRequestEvent>> PRTimeline;
}
