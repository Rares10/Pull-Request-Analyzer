package org.analyzer.pullrequestanalyzer.logic;

import com.google.common.collect.Lists;
import lombok.Data;
import org.analyzer.pullrequestanalyzer.domain.*;
import org.analyzer.pullrequestanalyzer.registry.*;
import org.analyzer.pullrequestanalyzer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class DataRegister {

    protected DeveloperRepository developerRepository;
    protected PullRequestRepository pullRequestRepository;
    protected CommentRepository commentRepository;
    protected CommitRepository commitRepository;
    protected FileRepository fileRepository;
    protected ProjectRepository projectRepository;

    private DeveloperRegistry developerRegistry;
    private PullRequestRegistry pullRequestRegistry;

    private ProjectData projectToAnalyze;

    private List<Commit> allCommits = new ArrayList<>();
    private List<Comment> allComments = new ArrayList<>();
    private List<File> allFiles = new ArrayList<>();

    @Autowired
    public DataRegister(DeveloperRegistry developerRegistry,
                        PullRequestRegistry pullRequestRegistry,
                        DeveloperRepository developerRepository,
                        PullRequestRepository pullRequestRepository,
                        CommentRepository commentRepository,
                        CommitRepository commitRepository,
                        FileRepository fileRepository,
                        ProjectRepository projectRepository) {
        this.developerRepository = developerRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.commentRepository = commentRepository;
        this.commitRepository = commitRepository;
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
        this.developerRegistry = developerRegistry;
        this.pullRequestRegistry = pullRequestRegistry;
    }

    @Transactional
    public void registerData() {

        System.out.println("Started registring data at " + new Date());

        registerDevelopersDetails();
        registerPullRequestsDetails();
        registerCommentsDetails();
        registerCommitsDetails();
        registerFilesDetails();
        registerCommentsForPullRequests();
        registerCommentsForDevelopers();
        registerCommitsForPullRequests();
        registerCommitsForDevelopers();
        registerFilesForPullRequests();
        registerCreatedPRsForDeveloper();
        registerMergedPRsForDeveloper();

        System.out.println("Finished with registration of data at " + new Date());
    }

    private void registerCreatedPRsForDeveloper() {
        Map<String, Developer> developers = developerRegistry.getDevelopers();
        for (Map.Entry<String, Developer> developerEntry : developers.entrySet()) {
            List<PullRequest> result = pullRequestRegistry.getPullRequests().stream()
                    .filter(x -> x.getCreatorDev().getDeveloperId().equals(developerEntry.getValue().getDeveloperId()))
                    .collect(Collectors.toList());
            developerEntry.getValue().setCreatedPullRequests(result);
        }
    }

    private void registerMergedPRsForDeveloper() {
        Map<String, Developer> developers = developerRegistry.getDevelopers();
        for (Map.Entry<String, Developer> developerEntry : developers.entrySet()) {
            List<PullRequest> result = pullRequestRegistry.getPullRequests().stream()
                    .filter(x -> (x.getMergeDate() != null) && (x.getMergeDev().getDeveloperId().equals(developerEntry.getValue().getDeveloperId())))
                    .collect(Collectors.toList());
            developerEntry.getValue().setMergedPullRequests(result);
        }
    }

    private void registerFilesForPullRequests() {
        List<PullRequest> pullRequests = pullRequestRegistry.getPullRequests();
        for (PullRequest pullRequest : pullRequests) {
            List<File> result = allFiles.stream()
                    .filter(x -> x.getPullRequest().getPullRequestId().equals(pullRequest.getPullRequestId()))
                    .collect(Collectors.toList());
            pullRequest.setFilesModified(result);
        }
    }

    private void registerCommitsForDevelopers() {
        Map<String, Developer> developers = developerRegistry.getDevelopers();
        for (Map.Entry<String, Developer> developerEntry : developers.entrySet()) {
            List<Commit> result = allCommits.stream()
                    .filter(x -> (x.getAuthor() != null) && (x.getAuthor().getDeveloperId().equals(developerEntry.getValue().getDeveloperId())))
                    .collect(Collectors.toList());
            developerEntry.getValue().setCommits(result);
        }
    }

    private void registerCommitsForPullRequests() {
        List<PullRequest> pullRequests = pullRequestRegistry.getPullRequests();
        for (PullRequest pullRequest : pullRequests) {
            List<Commit> result = allCommits.stream()
                    .filter(x -> x.getPullRequest().getPullRequestId().equals(pullRequest.getPullRequestId()))
                    .collect(Collectors.toList());
            pullRequest.setCommits(result);
        }
    }

    private void registerCommentsForDevelopers() {

        Map<String, Developer> developers = developerRegistry.getDevelopers();
        for (Map.Entry<String, Developer> developerEntry : developers.entrySet()) {
            List<Comment> result = allComments.stream()
                    .filter(x -> (x.getAuthor() != null) && (x.getAuthor().getDeveloperId().equals(developerEntry.getValue().getDeveloperId())))
                    .collect(Collectors.toList());
            developerEntry.getValue().setComments(result);
        }
    }

    private void registerCommentsForPullRequests() {
        List<PullRequest> pullRequests = pullRequestRegistry.getPullRequests();
        for (PullRequest pullRequest : pullRequests) {
            List<Comment> result = allComments.stream()
                    .filter(x -> x.getPullRequest().getPullRequestId().equals(pullRequest.getPullRequestId()))
                    .collect(Collectors.toList());
            pullRequest.setComments(result);
        }
    }

    private void registerFilesDetails() {
        Iterable<FileData> allFileData = fileRepository.findAll();
        List<FileData> filesToAnalyze = Lists.newArrayList(allFileData).stream()
                .filter(x -> x.getPullRequest().getProject().getProjectId().equals(projectToAnalyze.getProjectId()))
                .collect(Collectors.toList());

        for (FileData file : filesToAnalyze) {
            File currentFile = new File();
            currentFile.setFileId(file.getFileId());
            currentFile.setFileName(file.getFileName());
            currentFile.setPullRequest(pullRequestRegistry.getPullRequestById(file.getPullRequest().getPullRequestId()));
            currentFile.setAdditions(file.getAdditions());
            currentFile.setChanges(file.getChanges());
            currentFile.setDeletions(file.getDeletions());
            allFiles.add(currentFile);
        }
    }

    private void registerCommentsDetails() {
        Iterable<CommentData> allCommentData = commentRepository.findAll();
        List<CommentData> commentsToAnalyze = Lists.newArrayList(allCommentData).stream()
                .filter(x -> x.getPullRequest().getProject().getProjectId().equals(projectToAnalyze.getProjectId()))
                .collect(Collectors.toList());

        for (CommentData comment : commentsToAnalyze) {
            Comment currentComment = new Comment();
            currentComment.setCommentId(comment.getCommentId());
            if (comment.getAuthor() != null) {
                currentComment.setAuthor(developerRegistry.getDeveloperById(comment.getAuthor().getDeveloperId()));
            }
            currentComment.setContent(comment.getContent());
            currentComment.setFile(comment.getFile());
            currentComment.setLineOfCode(comment.getLineOfCode());
            currentComment.setParentId(comment.getParentId());
            currentComment.setPullRequest(pullRequestRegistry.getPullRequestById(comment.getPullRequest().getPullRequestId()));
            currentComment.setTimestamp(comment.getTimestamp());
            allComments.add(currentComment);
        }
    }

    private void registerCommitsDetails() {
        Iterable<CommitData> allCommitData = commitRepository.findAll();
        List<CommitData> commitsToAnalyze = Lists.newArrayList(allCommitData).stream()
                .filter(x -> x.getPullRequest().getProject().getProjectId().equals(projectToAnalyze.getProjectId()))
                .collect(Collectors.toList());

        for (CommitData commit : commitsToAnalyze) {
            Commit currentCommit = new Commit();
            currentCommit.setCommitId(commit.getCommitId());
            if (commit.getAuthor() != null) {
                currentCommit.setAuthor(developerRegistry.getDeveloperById(commit.getAuthor().getDeveloperId()));
            }
            currentCommit.setPullRequest(pullRequestRegistry.getPullRequestById(commit.getPullRequest().getPullRequestId()));
            currentCommit.setMessage(commit.getMessage());
            currentCommit.setTimestamp(commit.getTimestamp());
            allCommits.add(currentCommit);
        }
    }

    private void registerPullRequestsDetails() {
        Iterable<PullRequestData> allPullRequestData = pullRequestRepository.findAllByProject(projectToAnalyze);
        List<PullRequest> pullRequests = new ArrayList<>();

        for (PullRequestData pr : allPullRequestData) {
            PullRequest currentPullRequest = new PullRequest();
            currentPullRequest.setPullRequestId(pr.getPullRequestId());
            currentPullRequest.setTitle(pr.getTitle());
            currentPullRequest.setStatus(pr.getStatus().toLowerCase());
            currentPullRequest.setInitBranch(pr.getInitBranch());
            currentPullRequest.setFinalBranch(pr.getFinalBranch());
            currentPullRequest.setCreatorDev(developerRegistry.getDeveloperById(pr.getCreatorDev().getDeveloperId()));
            if (pr.getCloseDate() != null)
                currentPullRequest.setCloseDate(pr.getCloseDate());
            if (pr.getMergeDev() != null) {
                currentPullRequest.setMergeDev(developerRegistry.getDeveloperById(pr.getMergeDev().getDeveloperId()));
                currentPullRequest.setMergeDate(pr.getMergeDate());
            }
            currentPullRequest.setCreationDate(pr.getCreationDate());
            pullRequests.add(currentPullRequest);
        }
        pullRequestRegistry.setPullRequests(pullRequests);
    }

    private void registerDevelopersDetails() {
        Iterable<DeveloperData> allDeveloperData = developerRepository.findAllByProject(projectToAnalyze);
        Map<String, Developer> developers = new HashMap<>();

        for (DeveloperData developerData : allDeveloperData) {
            Developer currentDeveloper = new Developer();
            currentDeveloper.setDeveloperId(developerData.getDeveloperId());
            currentDeveloper.setUsername(developerData.getUsername());
            currentDeveloper.setName(developerData.getName());
            currentDeveloper.setEmail(developerData.getEmail());
            developers.put(developerData.getUsername(), currentDeveloper);
        }
        developerRegistry.setDevelopers(developers);
    }
}