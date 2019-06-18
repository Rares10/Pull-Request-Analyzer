package org.analyzer.pullrequestanalyzer.logic;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.analysis.DeveloperAnalysis;
import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.analyzer.pullrequestanalyzer.dto.*;
import org.analyzer.pullrequestanalyzer.logic.analyzer.DataAnalyzer;
import org.analyzer.pullrequestanalyzer.logic.analyzer.Statistics;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.DeveloperActivity;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.MergePRActivity;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.OpenPRActivity;
import org.analyzer.pullrequestanalyzer.registry.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class DataParser {

    private DeveloperRegistry developerRegistry;
    private PullRequestRegistry pullRequestRegistry;
    private DataAnalyzer dataAnalyzer;
    private DTORegistry dtoRegistry;
    private Statistics statistics;

    @Autowired
    public DataParser(DeveloperRegistry developerRegistry,
                      PullRequestRegistry pullRequestRegistry,
                      DataAnalyzer dataAnalyzer,
                      DTORegistry dtoRegistry,
                      Statistics statistics) {

        this.developerRegistry = developerRegistry;
        this.pullRequestRegistry = pullRequestRegistry;
        this.dataAnalyzer = dataAnalyzer;
        this.dtoRegistry = dtoRegistry;
        this.statistics = statistics;
    }

    public void parseData() {

        System.out.println("Started parsing data at " + new Date());
        parseDevelopers();
        parsePullRequests();
        parseComments();
        parseCommits();
        parseFiles();
        parseDevAnalysis();
        parsePRAnalysis();
        System.out.println("Finished parsing data at " + new Date());
    }

    private void parseDevelopers() {

        List<Developer> developers = new ArrayList<>(developerRegistry.getDevelopers().values());
        List<DeveloperDTO> developerDTOS = new ArrayList<>();
        for (Developer developer : developers) {

            DeveloperDTO currentDeveloperDTO = new DeveloperDTO();
            currentDeveloperDTO.setDeveloperId(developer.getDeveloperId());
            currentDeveloperDTO.setUsername(developer.getUsername());
            currentDeveloperDTO.setName(developer.getName());
            currentDeveloperDTO.setEmail(developer.getEmail());
            currentDeveloperDTO.setCreatedPullRequests(developer.getCreatedPullRequests().stream().map(x -> x.getPullRequestId()).collect(Collectors.toList()));
            currentDeveloperDTO.setMergedPullRequests(developer.getMergedPullRequests().stream().map(x -> x.getPullRequestId()).collect(Collectors.toList()));
            currentDeveloperDTO.setComments(developer.getComments().stream().map(x -> x.getCommentId()).collect(Collectors.toList()));
            currentDeveloperDTO.setCommits(developer.getCommits().stream().map(x -> x.getCommitId()).collect(Collectors.toList()));
            developerDTOS.add(currentDeveloperDTO);
        }
        dtoRegistry.setDeveloperDTOS(developerDTOS);
    }

    private void parsePullRequests() {

        List<PullRequest> pullRequests = pullRequestRegistry.getPullRequests();
        List<PullRequestDTO> pullRequestsDTOS = new ArrayList<>();

        for (PullRequest pullRequest : pullRequests) {

            PullRequestDTO pullRequestDTO = new PullRequestDTO();
            pullRequestDTO.setPullRequestId(pullRequest.getPullRequestId());
            pullRequestDTO.setTitle(pullRequest.getTitle());
            pullRequestDTO.setStatus(pullRequest.getStatus());
            pullRequestDTO.setCreationDate(pullRequest.getCreationDate());
            if (pullRequest.getMergeDate() != null)
                pullRequestDTO.setMergeDate(pullRequest.getMergeDate());
            if (pullRequest.getCloseDate() != null)
                pullRequestDTO.setCloseDate(pullRequest.getCloseDate());
            pullRequestDTO.setInitBranch(pullRequest.getInitBranch());
            pullRequestDTO.setFinalBranch(pullRequest.getFinalBranch());
            pullRequestDTO.setComments(pullRequest.getComments().stream().map(x -> x.getCommentId()).collect(Collectors.toList()));
            pullRequestDTO.setCommits(pullRequest.getCommits().stream().map(x -> x.getCommitId()).collect(Collectors.toList()));
            pullRequestDTO.setFilesModified(pullRequest.getFilesModified().stream().map(x -> x.getFileId()).collect(Collectors.toList()));
            pullRequestDTO.setCreatorDev(pullRequest.getCreatorDev().getDeveloperId());
            if (pullRequest.getMergeDev() != null)
                pullRequestDTO.setMergeDev(pullRequest.getMergeDev().getDeveloperId());
            pullRequestsDTOS.add(pullRequestDTO);
        }
        dtoRegistry.setPullRequestDTOS(pullRequestsDTOS);
    }

    private void parseComments() {

        List<Comment> comments = pullRequestRegistry.getPullRequests().stream()
                .flatMap(x -> x.getComments().stream())
                .collect(Collectors.toList());
        List<CommentDTO> commentDTOS = new ArrayList<>();

        for (Comment comment : comments) {

            if (comment.getAuthor() == null)
                continue;
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setCommentId(comment.getCommentId());
            commentDTO.setAuthor(comment.getAuthor().getDeveloperId());
            commentDTO.setContent(comment.getContent());
            commentDTO.setPullRequest(comment.getPullRequest().getPullRequestId());
            commentDTO.setFile(comment.getFile());
            commentDTO.setLineOfCode(comment.getLineOfCode());
            commentDTO.setParentId(comment.getParentId());
            commentDTO.setTimestamp(comment.getTimestamp());
            commentDTOS.add(commentDTO);
        }
        dtoRegistry.setCommentDTOS(commentDTOS);
    }

    private void parseCommits() {

        List<Commit> commits = pullRequestRegistry.getPullRequests().stream()
                .flatMap(x -> x.getCommits().stream())
                .collect(Collectors.toList());
        List<CommitDTO> commitDTOS = new ArrayList<>();

        for (Commit commit : commits) {
            if (commit.getAuthor() == null)
                continue;
            CommitDTO commitDTO = new CommitDTO();
            commitDTO.setAuthor(commit.getAuthor().getDeveloperId());
            commitDTO.setCommitId(commit.getCommitId());
            commitDTO.setMessage(commit.getMessage());
            commitDTO.setPullRequest(commit.getPullRequest().getPullRequestId());
            commitDTO.setTimestamp(commit.getTimestamp());
            commitDTOS.add(commitDTO);
        }
        dtoRegistry.setCommitDTOS(commitDTOS);
    }

    private void parseFiles() {

        List<File> files = pullRequestRegistry.getPullRequests().stream()
                .flatMap(x -> x.getFilesModified().stream())
                .collect(Collectors.toList());
        List<FileDTO> fileDTOS = new ArrayList<>();

        for (File file : files) {

            FileDTO fileDTO = new FileDTO();
            fileDTO.setFileId(file.getFileId());
            fileDTO.setPullRequest(file.getPullRequest().getPullRequestId());
            fileDTO.setFileName(file.getFileName());
            fileDTO.setAdditions(file.getAdditions());
            fileDTO.setChanges(file.getChanges());
            fileDTO.setDeletions(file.getDeletions());
            fileDTOS.add(fileDTO);
        }
        dtoRegistry.setFileDTOS(fileDTOS);
    }

    private void parseDevAnalysis() {

        List<DeveloperAnalysis> developerAnalyses = dataAnalyzer.getDeveloperAnalyses();
        List<DeveloperAnalysisDTO> developerAnalysisDTOS = new ArrayList<>();

        for (DeveloperAnalysis developerAnalysis : developerAnalyses) {

            DeveloperAnalysisDTO developerAnalysisDTO = new DeveloperAnalysisDTO();
            developerAnalysisDTO.setClosedWithoutMergePR(developerAnalysis.getClosedWithoutMergePR());
            developerAnalysisDTO.setCommentsOnCreatedPR(developerAnalysis.getCommentsOnCreatedPR());
            developerAnalysisDTO.setCommentsOnOtherPR(developerAnalysis.getCommentsOnOtherPR());
            developerAnalysisDTO.setDeveloperId(developerAnalysis.getDeveloper().getDeveloperId());
            developerAnalysisDTO.setInteractions(developerAnalysis.getInteractions());
            developerAnalysisDTO.setLongTimeToMergePR(developerAnalysis.getLongTimeToMergePR());
            developerAnalysisDTO.setMergedPRByCreator(developerAnalysis.getMergedPRByCreator());
            developerAnalysisDTO.setMergedPRByOthers(developerAnalysis.getMergedPRByOthers());
            developerAnalysisDTO.setMergeTimesOfCreatedPR(developerAnalysis.getMergeTimesOfCreatedPR().stream().map(x -> (double)x/(1000*60*60*24)).collect(Collectors.toList()));
            developerAnalysisDTO.setMeanMergeTimesOfCreatedPR(statistics.calculateLongsAverage(developerAnalysis.getMergeTimesOfCreatedPR())/(double)(1000*60*60*24));
            developerAnalysisDTO.setMedianValueOfMergeTimesOfCreatedPR(statistics.calculateLongMedianValue(developerAnalysis.getMergeTimesOfCreatedPR())/(double)(1000*60*60*24));
            developerAnalysisDTO.setMergedPROfOthers(developerAnalysis.getMergedPROfOthers());
            developerAnalysisDTO.setNoOfChangedFiles(developerAnalysis.getNoOfChangedFiles());
            developerAnalysisDTO.setMeanNoOfChangedFiles(statistics.calculateIntegersAverage(developerAnalysis.getNoOfChangedFiles()));
            developerAnalysisDTO.setMedianValueOfChangedFiles(statistics.calculateIntMedianValue(developerAnalysis.getNoOfChangedFiles()));
            developerAnalysisDTO.setNoOfCommentsOfCreatedPR(developerAnalysis.getNoOfCommentsOfCreatedPR());
            developerAnalysisDTO.setMeanNoOfCommentsOfCreatedPR(statistics.calculateIntegersAverage(developerAnalysis.getNoOfCommentsOfCreatedPR()));
            developerAnalysisDTO.setMedianValueOfCommentsOfCreatedPR(statistics.calculateIntMedianValue(developerAnalysis.getNoOfCommentsOfCreatedPR()));
            developerAnalysisDTO.setNoOfCommits(developerAnalysis.getNoOfCommits());
            developerAnalysisDTO.setMeanNoOfCommits(statistics.calculateIntegersAverage(developerAnalysis.getNoOfCommits()));
            developerAnalysisDTO.setMedianValueOfCommits(statistics.calculateIntMedianValue(developerAnalysis.getNoOfCommits()));
            developerAnalysisDTO.setNoOfComplexPR(developerAnalysis.getNoOfComplexPR());
            developerAnalysisDTO.setOpenedPR(developerAnalysis.getOpenedPR());
            developerAnalysisDTO.setOpenForALongTimePR(developerAnalysis.getOpenForALongTimePR());
            developerAnalysisDTO.setReactionTimesOnPR(developerAnalysis.getReactionTimesOnPR().stream().map(x -> x / (double)(1000*60*60)).collect(Collectors.toList()));
            developerAnalysisDTO.setMeanReactionTimeOnPR(statistics.calculateLongsAverage(developerAnalysis.getReactionTimesOnPR())/(double) (1000*60*60));
            developerAnalysisDTO.setMedianValueOfReactionTimesOnPR(statistics.calculateLongMedianValue(developerAnalysis.getReactionTimesOnPR())/(double)(1000*60*60));
            developerAnalysisDTO.setResponseTimesOnComments(developerAnalysis.getResponseTimesOnComments().stream().map(x -> x / (double)(1000*60*60)).collect(Collectors.toList()));
            developerAnalysisDTO.setMeanResponseTimesOnComments(statistics.calculateDoublesAverage(developerAnalysis.getResponseTimesOnComments())/(1000*60*60));
            developerAnalysisDTO.setMedianValueOfResponseTimesOnComments(statistics.calculateDoubleMedianValue(developerAnalysis.getResponseTimesOnComments())/(double)(1000*60*60));
            developerAnalysisDTO.setTimeline(developerAnalysis.getTimeline());
            developerAnalysisDTOS.add(developerAnalysisDTO);
        }
        dtoRegistry.setDeveloperAnalysisDTOS(developerAnalysisDTOS);
    }

    private void parsePRAnalysis() {

        PullRequestAnalysis pullRequestAnalysis = dataAnalyzer.getPullRequestAnalysis();
        PullRequestAnalysisDTO pullRequestAnalysisDTO = new PullRequestAnalysisDTO();

        pullRequestAnalysisDTO.setOwner(pullRequestAnalysis.getOwner());
        pullRequestAnalysisDTO.setProjectName(pullRequestAnalysis.getProjectName());
        pullRequestAnalysisDTO.setOpenPRs(pullRequestAnalysis.getOpenPRs().size());
        pullRequestAnalysisDTO.setClosedPRsWithoutMerge(pullRequestAnalysis.getClosedPRsWithoutMerge().size());
        pullRequestAnalysisDTO.setMergedPRs(pullRequestAnalysis.getMergedPRs().size());
        pullRequestAnalysisDTO.setLongTimeToMergePRs(pullRequestAnalysis.getLongTimeToMergePRs().size());
        pullRequestAnalysisDTO.setOpenForALongTimePRs(pullRequestAnalysis.getOpenForALongTimePRs().size());
        pullRequestAnalysisDTO.setPrsWithoutActivity(pullRequestAnalysis.getPrsWithoutActivity().size());
        pullRequestAnalysisDTO.setComplexPRs(pullRequestAnalysis.getComplexPRs().size());
        pullRequestAnalysisDTO.setMeanOpenedPRsPerDay(statistics.calculateDailyAverage(pullRequestAnalysis.getDailyOpenedPRs()));
        pullRequestAnalysisDTO.setMedianValueOfOpenedPRsPerDay(statistics.calculateDailyMedianValue(pullRequestAnalysis.getDailyOpenedPRs()));
        pullRequestAnalysisDTO.setMeanOpenedPRsPerWeek(statistics.calculateWeeklyAverage(pullRequestAnalysis.getDailyOpenedPRs()));
        pullRequestAnalysisDTO.setMedianValueOfOpenedPRsPerWeek(statistics.calculateWeeklyMedianValue(pullRequestAnalysis.getDailyOpenedPRs()));
        pullRequestAnalysisDTO.setMeanMergedPRsPerDay(statistics.calculateDailyAverage(pullRequestAnalysis.getDailyMergedPRs()));
        pullRequestAnalysisDTO.setMedianValueOfMergedPRsPerDay(statistics.calculateDailyMedianValue(pullRequestAnalysis.getDailyMergedPRs()));
        pullRequestAnalysisDTO.setMeanMergedPRsPerWeek(statistics.calculateWeeklyAverage(pullRequestAnalysis.getDailyMergedPRs()));
        pullRequestAnalysisDTO.setMedianValueOfMergedPRsPerWeek(statistics.calculateWeeklyMedianValue(pullRequestAnalysis.getDailyMergedPRs()));
        pullRequestAnalysisDTO.setMeanNoOfCommentsPerPR(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfCommentsPerPR()));
        pullRequestAnalysisDTO.setMedianValueOfCommentsPerPR(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfCommentsPerPR()));
        pullRequestAnalysisDTO.setMeanNoOfCommentsOnCodePerPR(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfCommentsOnCodePerPR()));
        pullRequestAnalysisDTO.setMedianValueOfCommentsOnCodePerPR(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfCommentsOnCodePerPR()));
        pullRequestAnalysisDTO.setMeanNoOfSimpleCommentsPerPR(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfSimpleCommentsPerPR()));
        pullRequestAnalysisDTO.setMedianValueOfSimpleCommentsPerPR(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfSimpleCommentsPerPR()));
        pullRequestAnalysisDTO.setMeanNoOfCommitsPerPR(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfCommitsPerPR()));
        pullRequestAnalysisDTO.setMedianValueOfNoOfCommitsPerPR(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfCommitsPerPR()));
        pullRequestAnalysisDTO.setMeanNoOfFollowUpCommitsPerPR(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfFollowUpCommitsPerPR()));
        pullRequestAnalysisDTO.setMedianValueOfFollowUpCommitsPerPR(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfFollowUpCommitsPerPR()));
        pullRequestAnalysisDTO.setMeanNoOfChangedFilesPerPR(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfChangedFilesPerPR()));
        pullRequestAnalysisDTO.setMedianValueOfChangedFilesPerPR(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfChangedFilesPerPR()));
        pullRequestAnalysisDTO.setBranchBranchPRs(pullRequestAnalysis.getBranchBranchPRs().size());
        pullRequestAnalysisDTO.setMeanReactionTimesOnPRs(statistics.calculateDoublesAverage(pullRequestAnalysis.getReactionTimesOnPRs()) / (double)(1000*60*60));
        pullRequestAnalysisDTO.setMedianValueOfReactionTimesOnPRs(statistics.calculateDoubleMedianValue(pullRequestAnalysis.getReactionTimesOnPRs()) / (double) (1000*60*60));
        pullRequestAnalysisDTO.setMeanResponseTimesOfPROwner(statistics.calculateDoublesAverage(pullRequestAnalysis.getResponseTimesOfPROwner()) / (double) (1000*60*60));
        pullRequestAnalysisDTO.setMedianValueOfResponseTimesOfPROwner(statistics.calculateDoubleMedianValue(pullRequestAnalysis.getResponseTimesOfPROwner())/ (double) (1000*60*60));
        pullRequestAnalysisDTO.setMeanMergeTimesOfPRs(statistics.calculateLongsAverage(pullRequestAnalysis.getMergeTimesOfPRs()));
        pullRequestAnalysisDTO.setMedianValueOfMergeTimesOfPRs(statistics.calculateLongMedianValue(pullRequestAnalysis.getMergeTimesOfPRs()));
        pullRequestAnalysisDTO.setCrowdedDays(pullRequestAnalysis.getCrowdedDays());
        pullRequestAnalysisDTO.setCrowdedTwoDays(pullRequestAnalysis.getCrowdedTwoDays());
        pullRequestAnalysisDTO.setCrowdedThreeDays(pullRequestAnalysis.getCrowdedThreeDays());
        pullRequestAnalysisDTO.setPRTimeline(pullRequestAnalysis.getPRTimeline());

        dtoRegistry.setPullRequestAnalysisDTO(pullRequestAnalysisDTO);
    }
}
