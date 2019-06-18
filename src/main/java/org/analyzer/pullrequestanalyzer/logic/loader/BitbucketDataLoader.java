package org.analyzer.pullrequestanalyzer.logic.loader;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.domain.*;
import org.analyzer.pullrequestanalyzer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Qualifier("bitbucket")
public class BitbucketDataLoader extends DataLoader {

    //Loading is stopped when username or password or owner or repository name are incorrect
    private boolean shouldStop = false;

    @Autowired
    public BitbucketDataLoader(DeveloperRepository developerRepository,
                               PullRequestRepository pullRequestRepository,
                               CommentRepository commentRepository,
                               CommitRepository commitRepository,
                               FileRepository fileRepository,
                               ProjectRepository projectRepository) {
        super(developerRepository, pullRequestRepository, commentRepository, commitRepository, fileRepository, projectRepository);
    }

    @Async
    public void loadData() throws ParseException{

        System.out.println("Started loading data at " + new Date());

        RestTemplate restTemplate = new RestTemplate();
        List<BitbucketPullRequestURL> allPullRequests = new LinkedList<>();
        Set<BitbucketAuthorURL> developers = new HashSet<>();
        UUID projectId;

        getOpenPullRequests(restTemplate, allPullRequests);

        if (shouldStop)
            return;

        getDeclinedPullRequests(restTemplate, allPullRequests);

        getMergedPullRequests(restTemplate, allPullRequests);

        allPullRequests.sort(Comparator.comparing(BitbucketPullRequestURL::getId));

        getAllDevelopersInvolved(restTemplate, allPullRequests, developers);
        List<BitbucketAuthorURL> allDevelopers = new ArrayList<>(developers);

        String URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository;
        ResponseEntity<BitbucketProjectURL> responseObject;
        String uuidString;
        responseObject = restTemplate.exchange(
                URL,
                HttpMethod.GET,
                new HttpEntity<BitbucketProjectURL>(createHeaders(username, password)),
                new ParameterizedTypeReference<BitbucketProjectURL>() {
                });

        uuidString = responseObject.getBody().getUuid();
        uuidString = uuidString.substring(1, uuidString.length() - 1);
        projectId = UUID.fromString(uuidString);

        if (projectRepository.findByProjectId(projectId.getLeastSignificantBits()) != null) {

            updateDeveloperData(allDevelopers, projectId);

            List<PullRequestData> allExistingPullRequests = pullRequestRepository.findAllByProject(projectRepository.findByRepository(repository));
            int index = allExistingPullRequests.indexOf(allExistingPullRequests.get(allExistingPullRequests.size() - 1));
            allPullRequests = allPullRequests.subList(index, allPullRequests.size());

            loadPullRequestData(allPullRequests, projectId);
            loadCommentData(allPullRequests);
            loadCommitData(allPullRequests);
            loadFileData(allPullRequests);
        }
        else {

            loadProjectData(projectId);
            loadDeveloperData(allDevelopers, projectId);
            loadPullRequestData(allPullRequests, projectId);
            loadCommentData(allPullRequests);
            loadCommitData(allPullRequests);
            loadFileData(allPullRequests);
        }

        System.out.println("Finished with data loading at " + new Date());
    }

    private void updateDeveloperData(List<BitbucketAuthorURL> allDevelopers, UUID projectId) {

        for (BitbucketAuthorURL developer : allDevelopers) {

            String uuidString = developer.getUuid();
            uuidString = uuidString.substring(1, uuidString.length() - 1);
            UUID uuid = UUID.fromString(uuidString);
            if (developerRepository.findByDeveloperId(uuid.getLeastSignificantBits()) == null)
                loadDeveloperDetails(projectId, developer);
        }
    }


    private void loadProjectData(UUID projectId) {

        ProjectData currentProject = new ProjectData();
        currentProject.setProjectId(projectId.getLeastSignificantBits());
        currentProject.setOwner(owner);
        currentProject.setRepository(repository);
        projectRepository.save(currentProject);
    }

    private void loadFileData(List<BitbucketPullRequestURL> allPullRequests) {

        for (BitbucketPullRequestURL pr : allPullRequests) {
            try {
                loadFilesAndChanges(pr);
            }catch (HttpClientErrorException.NotFound clientException) {
                continue;
            }catch (NullPointerException nullException) {
                continue;
            }
        }
    }

    private void loadFilesAndChanges(BitbucketPullRequestURL pr) {

        String URL;
        ResponseEntity<String> responseObject;

        URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests/" + pr.getId() + "/diff";
        responseObject = restTemplate.exchange(
                URL,
                HttpMethod.GET,
                new HttpEntity<String>(createHeaders(username, password)),
                new ParameterizedTypeReference<String>() {
                });

        String[] files = responseObject.getBody().split("diff --git ");
        for (int i = 1; i < files.length; i++) {
            String file = files[i];
            String[] lines = file.split("\n");
            loadFileChanges(pr, lines);
        }
    }
    private void loadFileChanges(BitbucketPullRequestURL pr, String [] lines) {

        FileData currentFile = new FileData();
        int startIndex = lines[0].indexOf(" b/");
        String fileName = lines[0].substring(2, startIndex);
        currentFile.setFileName(fileName);
        currentFile.setPullRequest(pullRequestRepository.findByPullRequestId(pr.getId()));

        int additions = 0;
        int deletions = 0;
        for (int i = 1; i < lines.length ; i++){

            String line = lines[i];
            if (line.startsWith("+") && !line.startsWith("+++")) {
                additions++;
            }
            else if (line.startsWith("-") && !line.startsWith("---")) {
                deletions++;
            }
        }
        currentFile.setAdditions(additions);
        currentFile.setDeletions(deletions);
        fileRepository.save(currentFile);
    }

    private void loadCommitData(List<BitbucketPullRequestURL> allPullRequests) throws ParseException {

        Map<BitbucketCommitURL, Long>allCommits = new HashMap<>();

        getAllCommits(allPullRequests, allCommits);

        String uuidString;
        UUID uuid;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date date;

        for (Map.Entry<BitbucketCommitURL, Long> entry : allCommits.entrySet()) {
            CommitData currentCommit = new CommitData();
            if (entry.getKey().getAuthor().getUser() != null) {
                uuidString = entry.getKey().getAuthor().getUser().getUuid();
                uuidString = uuidString.substring(1, uuidString.length() - 1);
                uuid = UUID.fromString(uuidString);
                currentCommit.setAuthor(developerRepository.findByDeveloperId(uuid.getLeastSignificantBits()));
            }
            currentCommit.setPullRequest(pullRequestRepository.findByPullRequestId(entry.getValue()));
            currentCommit.setMessage(entry.getKey().getMessage());
            date = format.parse(entry.getKey().getDate());
            currentCommit.setTimestamp(date);
            commitRepository.save(currentCommit);
        }
    }

    private void loadCommentData(List<BitbucketPullRequestURL> allPullRequests) throws ParseException {

        Map<BitbucketCommentURL, Long>allComments = new HashMap<>();

        getAllComments(allPullRequests, allComments);

        String uuidString;
        UUID uuid;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");
        Date date;

        for (Map.Entry<BitbucketCommentURL, Long> entry : allComments.entrySet()) {
            CommentData currentComment = new CommentData();
            currentComment.setCommentId(entry.getKey().getId());
            if (entry.getKey().getUser() != null) {
                uuidString = entry.getKey().getUser().getUuid();
                uuidString = uuidString.substring(1, uuidString.length() - 1);
                uuid = UUID.fromString(uuidString);
                currentComment.setAuthor(developerRepository.findByDeveloperId(uuid.getLeastSignificantBits()));
            }
            currentComment.setPullRequest(pullRequestRepository.findByPullRequestId(entry.getValue()));
            date = format.parse(entry.getKey().getCreated_on());
            currentComment.setTimestamp(date);
            if (entry.getKey().getParent() != null){
                currentComment.setParentId(entry.getKey().getParent().getId());
            }
            if (entry.getKey().getInline() != null) {
                currentComment.setFile(entry.getKey().getInline().getPath());
                currentComment.setLineOfCode(entry.getKey().getInline().getTo());
            }
            currentComment.setContent(entry.getKey().getContent().getRaw());
            commentRepository.save(currentComment);
        }
    }

    private void getAllCommits(List<BitbucketPullRequestURL> allPullRequests, Map<BitbucketCommitURL, Long> allCommits) {

        String URL;
        ResponseEntity<BitbucketCommitValuesURL> responseObject;
        for (BitbucketPullRequestURL pr : allPullRequests) {
            URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests/" + pr.getId() + "/commits" + "?pagelen=100";
            try {
                responseObject = restTemplate.exchange(
                        URL,
                        HttpMethod.GET,
                        new HttpEntity<BitbucketCommitValuesURL>(createHeaders(username, password)),
                        new ParameterizedTypeReference<BitbucketCommitValuesURL>() {
                        });
            } catch (HttpClientErrorException clientException) {
                continue;
            }

            BitbucketCommitURL[] commitsArray = responseObject.getBody().getValues();
            if (commitsArray.length >= 0) {
                for (BitbucketCommitURL commit : commitsArray) {
                    allCommits.put(commit, pr.getId());
                }
            }
        }
    }

    private void getAllComments(List<BitbucketPullRequestURL> allPullRequests, Map<BitbucketCommentURL, Long> allComments) {
        String URL;
        ResponseEntity<BitbucketCommentValuesURL> responseObject;
        for (BitbucketPullRequestURL pr : allPullRequests) {
            int page = 1;
            do {
                URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests/" + pr.getId() + "/comments/" + "?page=" + page++;
                responseObject = restTemplate.exchange(
                        URL,
                        HttpMethod.GET,
                        new HttpEntity<BitbucketCommentValuesURL>(createHeaders(username, password)),
                        new ParameterizedTypeReference<BitbucketCommentValuesURL>() {
                        });
                BitbucketCommentURL[] commentsArray = responseObject.getBody().getValues();
                if (commentsArray.length > 0) {
                    for (BitbucketCommentURL comment : commentsArray) {
                        allComments.put(comment, pr.getId());
                    }
                }
            } while (responseObject.getBody().getNext() != null);
        }
    }

    private ResponseEntity<BitbucketPullRequestValuesURL> getPullRequests(RestTemplate restTemplate, List<BitbucketPullRequestURL> allPullRequests, String URL) {
        ResponseEntity<BitbucketPullRequestValuesURL> responseObject = null;
        try {
            responseObject = restTemplate.exchange(
                    URL,
                    HttpMethod.GET,
                    new HttpEntity<BitbucketPullRequestValuesURL>(createHeaders(username, password)),
                    new ParameterizedTypeReference<BitbucketPullRequestValuesURL>() {
                    });
        }catch (HttpClientErrorException.Unauthorized e) {
            JOptionPane.showMessageDialog(null, "Wrong username or password!", "Error", JOptionPane.ERROR_MESSAGE);
            shouldStop = true;
        }catch (HttpClientErrorException.NotFound e) {
            JOptionPane.showMessageDialog(null, "Please check owner and repository name!", "Error", JOptionPane.ERROR_MESSAGE);
            shouldStop = true;
        }

        if (responseObject != null) {
            allPullRequests.addAll(Arrays.asList(responseObject.getBody().getValues()));
        }
        return responseObject;
    }
    private void getDeclinedPullRequests(RestTemplate restTemplate, List<BitbucketPullRequestURL> allPullRequests) {
        String URL;
        ResponseEntity<BitbucketPullRequestValuesURL> responseObject;
        int page = 1;
        do {
            URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests?state=DECLINED" + "&page=" + page++;
            responseObject = getPullRequests(restTemplate, allPullRequests, URL);
        } while (responseObject.getBody().getNext() != null);
    }

    private void getMergedPullRequests(RestTemplate restTemplate, List<BitbucketPullRequestURL> allPullRequests) {
        String URL;
        ResponseEntity<BitbucketPullRequestValuesURL> responseObject;
        int page = 1;
        do {
            URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests?state=MERGED" + "&page=" + page++;
            responseObject = getPullRequests(restTemplate, allPullRequests, URL);
        } while (responseObject.getBody().getNext() != null);
    }

    private void getOpenPullRequests(RestTemplate restTemplate, List<BitbucketPullRequestURL> allPullRequests) {
        String URL;
        ResponseEntity<BitbucketPullRequestValuesURL> responseObject;
        int page = 1;
        do {
            URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests?state=OPEN" + "&page=" + page++;
            responseObject = getPullRequests(restTemplate, allPullRequests, URL);
            if (shouldStop)
                return;
        } while (responseObject.getBody().getNext() != null);
    }

    private void getAllDevelopersInvolved(RestTemplate restTemplate, List<BitbucketPullRequestURL> allPullRequests, Set<BitbucketAuthorURL> developers) {
        String URL;
        for (BitbucketPullRequestURL pr : allPullRequests){
            URL = "https://api.bitbucket.org/2.0/repositories/" + owner + "/" + repository + "/pullrequests/" + pr.getId();
            ResponseEntity<BitbucketSinglePullRequestURL> responseObject = restTemplate.exchange(
                    URL,
                    HttpMethod.GET,
                    new HttpEntity<BitbucketSinglePullRequestURL>(createHeaders(username, password)),
                    new ParameterizedTypeReference<BitbucketSinglePullRequestURL>() {});
            List<BitbucketPullRequestParticipantURL> participants = Arrays.asList(responseObject.getBody().getParticipants());
            for (BitbucketPullRequestParticipantURL participant : participants) {
                developers.add(participant.getUser());
            }
            developers.add(pr.getAuthor());
        }
    }

    private void loadDeveloperData(List<BitbucketAuthorURL> allDevelopers, UUID projectId) {

        for (BitbucketAuthorURL developer : allDevelopers){
            loadDeveloperDetails(projectId, developer);
        }
    }

    private void loadDeveloperDetails(UUID projectId, BitbucketAuthorURL developer) {

        DeveloperData currentDeveloper = new DeveloperData();
        String uuidString = developer.getUuid();
        uuidString = uuidString.substring(1, uuidString.length()-1);
        UUID uuid = UUID.fromString(uuidString);
        currentDeveloper.setDeveloperId(uuid.getLeastSignificantBits());
        currentDeveloper.setProject(projectRepository.findByProjectId(projectId.getLeastSignificantBits()));
        currentDeveloper.setUsername(developer.getUsername());
        currentDeveloper.setName(developer.getDisplay_name());
        developerRepository.save(currentDeveloper);
    }

    private void loadPullRequestData(List<BitbucketPullRequestURL> allPullRequests, UUID projectId) throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");
        Date date;
        String uuidString;
        UUID uuid;

        for (BitbucketPullRequestURL pr : allPullRequests) {
            PullRequestData currentPullRequest = new PullRequestData();
            currentPullRequest.setPullRequestId(pr.getId());
            currentPullRequest.setProject(projectRepository.findByProjectId(projectId.getLeastSignificantBits()));
            currentPullRequest.setStatus(pr.getState());
            date = format.parse(pr.getCreated_on());
            currentPullRequest.setCreationDate(date);
            date = format.parse(pr.getUpdated_on());
            if (pr.getState().equals("DECLINED")){
                currentPullRequest.setCloseDate(date);
            }
            if (pr.getState().equals("MERGED")){
                currentPullRequest.setMergeDate(date);
            }
            currentPullRequest.setTitle(pr.getTitle());
            currentPullRequest.setInitBranch(pr.getSource().getBranch().getName());
            currentPullRequest.setFinalBranch(pr.getDestination().getBranch().getName());
            uuidString = pr.getAuthor().getUuid();
            uuidString = uuidString.substring(1, uuidString.length()-1);
            uuid = UUID.fromString(uuidString);
            currentPullRequest.setCreatorDev(developerRepository.findByDeveloperId(uuid.getLeastSignificantBits()));
            if (pr.getState().equals("MERGED")){
                uuidString = pr.getAuthor().getUuid();
                uuidString = uuidString.substring(1, uuidString.length()-1);
                uuid = UUID.fromString(uuidString);
                currentPullRequest.setMergeDev(developerRepository.findByDeveloperId(uuid.getLeastSignificantBits()));
            }
            pullRequestRepository.save(currentPullRequest);
        }
    }
}

@Data
@NoArgsConstructor
class BitbucketPullRequestValuesURL {
    private BitbucketPullRequestURL values[];
    private String next;
}

@Data
@NoArgsConstructor
class BitbucketPullRequestURL {
    private String title;
    private Long id;
    private BitbucketAuthorURL author;
    private String created_on;
    private String updated_on;
    private String state;
    private BitbucketPullRequestSourceURL source;
    private BitbucketPullRequestDestinationURL destination;
    private BitbucketAuthorURL closed_by;
}

@Data
@NoArgsConstructor
class BitbucketAuthorURL {
    private String uuid;
    private String username;
    private String display_name;
}

@Data
@NoArgsConstructor
class BitbucketSinglePullRequestURL {
    private BitbucketPullRequestParticipantURL[] participants;
}

@Data
@NoArgsConstructor
class BitbucketPullRequestParticipantURL {
    private BitbucketAuthorURL user;
}

@Data
@NoArgsConstructor
class BitbucketPullRequestSourceURL {
    private BitbucketPullRequestBranchURL branch;
}

@Data
@NoArgsConstructor
class BitbucketPullRequestDestinationURL {
    private BitbucketPullRequestBranchURL branch;
}

@Data
@NoArgsConstructor
class BitbucketPullRequestBranchURL {
    private String name;
}

@Data
@NoArgsConstructor
class BitbucketCommentValuesURL {
    private BitbucketCommentURL[] values;
    private String next;
}

@Data
@NoArgsConstructor
class BitbucketCommentURL {
    private Long id;
    private BitbucketAuthorURL user;
    private String created_on;
    private BitbucketCommentContentURL content;
    private BitbucketCommentInlineURL inline;
    private BitbucketParentURL parent;
}

@Data
@NoArgsConstructor
class BitbucketCommentContentURL {
    private String raw;
}

@Data
@NoArgsConstructor
class BitbucketCommentInlineURL {
    private int to;
    private String path;
}

@Data
@NoArgsConstructor
class BitbucketParentURL {
    private Long id;
}

@Data
@NoArgsConstructor
class BitbucketCommitValuesURL {
    private BitbucketCommitURL[] values;
    private String next;
}

@Data
@NoArgsConstructor
class BitbucketCommitURL {
    private String message;
    private String date;
    private BitbucketCommitAuthorURL author;
}

@Data
@NoArgsConstructor
class BitbucketCommitAuthorURL {
    private BitbucketAuthorURL user;
}

@Data
@NoArgsConstructor
class BitbucketProjectURL {
    private String uuid;
}