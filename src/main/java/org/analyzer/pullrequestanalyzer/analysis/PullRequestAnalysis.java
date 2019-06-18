package org.analyzer.pullrequestanalyzer.analysis;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.logic.pr_activity.PullRequestEvent;
import org.analyzer.pullrequestanalyzer.registry.PullRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
@Service
public class PullRequestAnalysis {

    private String owner;
    private String projectName;
    private List<PullRequest> openPRs;
    private List<PullRequest> closedPRsWithoutMerge;
    private List<PullRequest> mergedPRs;
    private List<PullRequest> longTimeToMergePRs;
    private List<PullRequest> openForALongTimePRs;
    private List<PullRequest> prsWithoutActivity;
    private List<PullRequest> complexPRs;
    private List<Date> dailyOpenedPRs;
    private List<Date> dailyMergedPRs;
    private List<Integer> noOfCommentsPerPR;
    private List<Long> noOfCommentsOnCodePerPR;
    private List<Long> noOfSimpleCommentsPerPR;
    private List<Integer> noOfCommitsPerPR;
    private List<Long> noOfFollowUpCommitsPerPR;
    private List<Integer> noOfChangedFilesPerPR;
    private List<PullRequest> branchBranchPRs;
    private List<Double> reactionTimesOnPRs;
    private List<Double> responseTimesOfPROwner;
    private List<Long> mergeTimesOfPRs;
    TreeMap<Date, Long> crowdedDays;
    TreeMap<Date, Long> crowdedTwoDays;
    TreeMap<Date, Long> crowdedThreeDays;
    Map<Long, List<PullRequestEvent>> PRTimeline;
}