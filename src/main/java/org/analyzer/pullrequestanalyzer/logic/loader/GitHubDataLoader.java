package org.analyzer.pullrequestanalyzer.logic.loader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.domain.*;
import org.analyzer.pullrequestanalyzer.repository.*;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

@Service
@Qualifier("github")
public class GitHubDataLoader extends DataLoader {

    @Autowired
    public GitHubDataLoader(DeveloperRepository developerRepository,
                            PullRequestRepository pullRequestRepository,
                            CommentRepository commentRepository,
                            CommitRepository commitRepository,
                            FileRepository fileRepository,
                            ProjectRepository projectRepository) {
        super(developerRepository, pullRequestRepository, commentRepository, commitRepository, fileRepository, projectRepository);
    }

    @Async
    public void loadData() throws IOException, InterruptedException {

        System.out.println("Started loading data at " + new Date() + "..");

        //authentication
        GitHubClient client = new GitHubClient();
        client.setCredentials(username,password);

        //services used by GitHub API
        RepositoryService repositoryService = new RepositoryService(client);
        UserService userService = new UserService(client);
        PullRequestService pullRequestService = new PullRequestService(client);
        IssueService issueService = new IssueService(client);

        Repository gitHubRepository = null;
        try {
            gitHubRepository = repositoryService.getRepository(owner, repository);
        }catch (RequestException e) {
            if (e.getMessage().contains("Not Found"))
                JOptionPane.showMessageDialog(null, "Please check the owner and repository name!", "Error", JOptionPane.ERROR_MESSAGE);
            if (e.getMessage().contains("Bad credentials"))
                JOptionPane.showMessageDialog(null, "Wrong username or password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<PullRequest> allPullRequests = pullRequestService.getPullRequests(gitHubRepository,"all");
        allPullRequests.sort(Comparator.comparing(PullRequest::getId));

        Set<String> developers = new HashSet<>();
        getAllDevelopers(repositoryService, gitHubRepository, allPullRequests, developers);
        List<String> orderedDevelopers = new ArrayList<>(developers);
        Collections.sort(orderedDevelopers);

        if (projectRepository.findByProjectId(gitHubRepository.getId()) != null) {

            updateDeveloperData(orderedDevelopers, userService, gitHubRepository);
            updatePullRequestData(pullRequestService, issueService, allPullRequests, gitHubRepository);
        }
        else {

            Date start = new Date();
            loadProjectData(gitHubRepository);
            loadDeveloperData(userService, orderedDevelopers, gitHubRepository, "");
            loadPullRequestData(pullRequestService, issueService, allPullRequests, gitHubRepository, 0, start);
        }
        System.out.println("Finished with data loading at " + new Date());
    }

    private void updateDeveloperData(List<String> orderedDevelopers, UserService userService, Repository gitHubRepository) throws IOException {

        for (String developer : orderedDevelopers) {

            User user = userService.getUser(developer);
            if (developerRepository.findByDeveloperId((long)user.getId()) == null) {
                loadDeveloperDetails(gitHubRepository, user);
            }
        }
    }

    private void updatePullRequestData(PullRequestService pullRequestService,
                                       IssueService issueService,
                                       List<PullRequest> allPullRequests,
                                       Repository gitHubRepository) throws InterruptedException {

        List<PullRequestData> allExistingPullRequests = pullRequestRepository.findAllByProject(projectRepository.findByRepository(repository));
        int index = allExistingPullRequests.indexOf(allExistingPullRequests.get(allExistingPullRequests.size() - 1));
        Date start = new Date();
        loadPullRequestData(pullRequestService, issueService, allPullRequests, gitHubRepository, index, start);
    }

    private void loadProjectData(Repository gitHubRepository) {

        ProjectData currentProject = new ProjectData();
        currentProject.setProjectId(gitHubRepository.getId());
        currentProject.setOwner(owner);
        currentProject.setRepository(repository);
        projectRepository.save(currentProject);
    }

    private void getAllDevelopers(RepositoryService repositoryService, Repository gitHubRepository, List<PullRequest> allPullRequests, Set<String> developers) throws IOException {
        List<Contributor> contributors = repositoryService.getContributors(gitHubRepository, false);

        for (PullRequest pr : allPullRequests) {
            developers.add(pr.getUser().getLogin());
        }
        for (Contributor c : contributors){
            developers.add(c.getLogin());
        }
    }

    private void loadRemainingPullRequestData(PullRequestService pullRequestService,
                                              IssueService issueService,
                                              Repository gitHubRepository,
                                              PullRequest pr) throws IOException {

        List<CommitComment> codeComments = pullRequestService.getComments(gitHubRepository, pr.getNumber());
        List<Comment> simpleComments = issueService.getComments(gitHubRepository, pr.getNumber());

        List<RepositoryCommit> commits = pullRequestService.getCommits(gitHubRepository, pr.getNumber());
        List<CommitFile> files = pullRequestService.getFiles(gitHubRepository, pr.getNumber());
        loadCommentData(simpleComments, codeComments, pr);
        loadCommitData(commits, pr);
        loadFileData(files, pr);
    }

    private void loadFileData(List<CommitFile> files, PullRequest pr) {

        for (CommitFile file : files){

            FileData currentFile = new FileData();
            String fileName = file.getFilename();
            if (fileName != null)
                currentFile.setFileName(fileName);
            currentFile.setPullRequest(pullRequestRepository.findByPullRequestId(pr.getId()));
            currentFile.setAdditions(file.getAdditions());
            currentFile.setChanges(file.getChanges());
            currentFile.setDeletions(file.getDeletions());
            fileRepository.save(currentFile);
        }
    }

    private void loadCommitData(List<RepositoryCommit> commits, PullRequest pr) {

        for (RepositoryCommit commit : commits) {

            CommitData currentCommit = new CommitData();
            if (commit.getAuthor() != null)
                currentCommit.setAuthor(developerRepository.findByDeveloperId((long)commit.getAuthor().getId()));
            currentCommit.setMessage(commit.getCommit().getMessage());
            currentCommit.setPullRequest(pullRequestRepository.findByPullRequestId(pr.getId()));
            currentCommit.setTimestamp(commit.getCommit().getAuthor().getDate());
            commitRepository.save(currentCommit);
        }
    }

    private void loadCommentData(List<Comment> simpleComments, List<CommitComment> codeComments, PullRequest pr) {

        for (CommitComment comment : codeComments){

            CommentData currentComment = new CommentData();
            currentComment.setCommentId(comment.getId());
            if (comment.getUser() != null)
                currentComment.setAuthor(developerRepository.findByDeveloperId((long) comment.getUser().getId()));
            currentComment.setPullRequest(pullRequestRepository.findByPullRequestId(pr.getId()));
            currentComment.setTimestamp(comment.getCreatedAt());
            currentComment.setFile(comment.getPath());
            currentComment.setContent(comment.getBody());

            String URL = "https://api.github.com/repos/" + owner + "/" + repository + "/pulls/comments/" + comment.getId();
            ResponseEntity<GitHubCommentURL> responseObject = restTemplate.exchange(URL, HttpMethod.GET,
                    new HttpEntity<GitHubCommentURL>(createHeaders(username, password)), GitHubCommentURL.class);
            if (responseObject.getBody() != null) {
                currentComment.setParentId(responseObject.getBody().getIn_reply_to_id());
                currentComment.setLineOfCode(responseObject.getBody().getOriginal_position());
            }
            commentRepository.save(currentComment);
        }

        for (Comment comment : simpleComments) {

            CommentData currentComment = new CommentData();
            currentComment.setCommentId(comment.getId());
            if (comment.getUser() != null)
                currentComment.setAuthor(developerRepository.findByDeveloperId((long) comment.getUser().getId()));
            currentComment.setPullRequest(pullRequestRepository.findByPullRequestId(pr.getId()));
            currentComment.setTimestamp(comment.getCreatedAt());
            currentComment.setContent(comment.getBody());
            commentRepository.save(currentComment);
        }
    }

    private void loadPullRequestData(PullRequestService pullRequestService,
                                     IssueService issueService,
                                     List<PullRequest> allPullRequests,
                                     Repository gitHubRepository,
                                     int startingIndex,
                                     Date startTime) throws HttpServerErrorException.BadGateway, InterruptedException{

        allPullRequests = allPullRequests.subList(startingIndex, allPullRequests.size());

        try {
            for (PullRequest pr : allPullRequests) {
                startingIndex = allPullRequests.indexOf(pr);
                loadPullRequestDetails(gitHubRepository, pr);
                loadRemainingPullRequestData(pullRequestService, issueService, gitHubRepository, pr);
            }
        } catch (RequestException | HttpClientErrorException limitException) {
            sleep(startingIndex, startTime);
            System.out.println("I'm back from sleeping at " + new Date());
            loadPullRequestData(pullRequestService, issueService, allPullRequests, gitHubRepository, startingIndex, new Date());
        } catch (HttpServerErrorException.BadGateway | IOException serverErrorException){
            System.out.println("Loading pull requests failed (Bad gateway)");
            System.out.println("Restarting loading from pull request id " + startingIndex);
            loadPullRequestData(pullRequestService, issueService, allPullRequests, gitHubRepository, startingIndex, startTime);
        }
    }

    private void sleep(int startingIndex, Date startTime) throws InterruptedException {

        Date now = new Date();
        long time = 1000 * 60 * 60 - (now.getTime() - startTime.getTime());
        System.out.println("API limit exceeded in pull request loading. Last pull request id loaded was " + startingIndex);
        System.out.println("Continue after " + time / (1000 * 60) + " minutes.., now is " + new Date());
        Thread.sleep(1000*60*30);
    }

    private void loadPullRequestDetails(Repository gitHubRepository, PullRequest pr) {
        PullRequestData currentPullRequest = new PullRequestData();
        currentPullRequest.setPullRequestId((pr.getId()));
        currentPullRequest.setProject(projectRepository.findByProjectId(gitHubRepository.getId()));
        String prTitle = pr.getTitle();
        if (prTitle != null)
            currentPullRequest.setTitle(prTitle);
        currentPullRequest.setStatus(pr.getState());
        currentPullRequest.setCreationDate(pr.getCreatedAt());
        Date prMergeDate = pr.getMergedAt();
        if (prMergeDate != null)
            currentPullRequest.setMergeDate(prMergeDate);
        Date prCloseDate = pr.getClosedAt();
        if (prCloseDate != null)
            currentPullRequest.setCloseDate(prCloseDate);

        String URL = "https://api.github.com/repos/" + owner + "/" + repository + "/pulls/" + pr.getNumber();
        ResponseEntity<GitHubPullRequestURL> responseObject = restTemplate.exchange(URL,
                HttpMethod.GET,
                new HttpEntity<GitHubPullRequestURL>(createHeaders(username, password)),
                GitHubPullRequestURL.class);
        currentPullRequest.setCreatorDev(developerRepository.findByDeveloperId(Long.valueOf(pr.getUser().getId())));
        if (responseObject.getBody().isMerged())
            currentPullRequest.setMergeDev(developerRepository.findByDeveloperId(Long.valueOf(responseObject.getBody().getMerged_by().getId())));
        currentPullRequest.setInitBranch(pr.getHead().getRef());
        currentPullRequest.setFinalBranch(pr.getBase().getRef());
        pullRequestRepository.save(currentPullRequest);
    }

    private void loadDeveloperData(UserService userService, List<String> orderedDevelopers, Repository gitHubRepository, String lastDeveloperAdded) {
        int startingIndex = orderedDevelopers.indexOf(lastDeveloperAdded);
        if (startingIndex == -1)
            startingIndex = 0;
        orderedDevelopers = orderedDevelopers.subList(startingIndex, orderedDevelopers.size());
        try {
            for (String developer : orderedDevelopers) {

                lastDeveloperAdded = developer;
                User user = userService.getUser(developer);
                loadDeveloperDetails(gitHubRepository, user);
            }
        }catch (IOException exception) {
            System.out.println("Loading developers failed (Bad Gateway");
            System.out.println("Restarting loading from developer " + lastDeveloperAdded);
            loadDeveloperData(userService, orderedDevelopers, gitHubRepository, lastDeveloperAdded);
        }
    }

    private void loadDeveloperDetails(Repository gitHubRepository, User user) {

        DeveloperData currentDeveloper = new DeveloperData();
        currentDeveloper.setDeveloperId(Long.valueOf(user.getId()));
        currentDeveloper.setProject(projectRepository.findByProjectId(gitHubRepository.getId()));
        currentDeveloper.setUsername(user.getLogin());
        String userName = user.getName();
        if (userName != null)
            currentDeveloper.setName(userName);
        String userEmail = user.getEmail();
        if (userEmail != null)
            currentDeveloper.setEmail(userEmail);
        developerRepository.save(currentDeveloper);
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GitHubCommentURL {
    private Long in_reply_to_id;
    private int original_position;
}

@Data
@NoArgsConstructor
class GitHubPullRequestURL {
    private boolean merged;
    private User merged_by;
}