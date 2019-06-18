package org.analyzer.pullrequestanalyzer.registry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Developer {

    private Long developerId;
    private String username;
    private String name;
    private String email;
    private List<PullRequest> createdPullRequests;
    private List<PullRequest> mergedPullRequests;
    private List<Comment> comments;
    private List<Commit> commits;

    public long getMergedPullRequestsByCreator() {

        return createdPullRequests.stream()
                .filter(x -> x.getMergeDate() != null && x.getMergeDev().getDeveloperId().equals(this.getDeveloperId()))
                .count();
    }

    public long getMergedPullRequestsByOthers() {

        return createdPullRequests.stream()
                .filter(x -> x.getMergeDate() != null && !x.getMergeDev().getDeveloperId().equals(this.getDeveloperId()))
                .count();
    }

    public long getMergedPullRequestsOfOthers() {

        return mergedPullRequests.stream()
                .filter(x -> !x.getCreatorDev().getDeveloperId().equals(this.getDeveloperId()))
                .count();
    }

    public long getNoOfComplexPRs() {

        return createdPullRequests.stream()
                .filter(x -> x.isOpenForALongTime() || (x.getMergeDate() != null && (x.getTimeForMerging() / (1000*60*60*24)) > 7))
                .filter(x -> x.getFilesModified().size() < 4)
                .filter(x -> x.getNoOfFollowUpCommits() >= 2)
                .filter(x -> x.getComments().size() >= 8)
                .count();
    }

    public List<Integer> getNoOfCommentsOfCreatedPR() {

        return createdPullRequests.stream()
                .map(x -> x.getComments().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    public List<Integer> getNoOfChangedFiles() {
        return createdPullRequests.stream()
                .map(x -> x.getFilesModified().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    public List<Integer> getNoOfCommits() {

        return createdPullRequests.stream()
                .map(x -> x.getCommits().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    public Long getCommentsOnCreatedPR() {

        return comments.stream()
                .filter(x -> x.getPullRequest().getCreatorDev().getDeveloperId().equals(this.getDeveloperId()))
                .count();
    }

    public Long getCommentsOnOtherPR() {

        return comments.size() - this.getCommentsOnCreatedPR();
    }

    public Long getClosedWithoutMergePR() {

        return createdPullRequests.stream()
                .filter(x -> (x.getStatus().equals("closed") && x.getMergeDate() == null) || (x.getStatus().equals("declined")))
                .count();
    }

    public Long getLongTimePR() {

        return createdPullRequests.stream()
                .filter(x -> x.getStatus().equals("open"))
                .filter(x -> x.isOpenForALongTime())
                .count();
    }

    public Long getLongMergeTimePR() {

        return createdPullRequests.stream()
                .filter(x -> x.getMergeDate() != null)
                .filter(x -> (x.getTimeForMerging() / (1000*60*60*24)) > 7)
                .count();
    }

    public List<Long> getReactionTime(List<PullRequest> allPullRequests) {

         return allPullRequests.stream()
                 .filter(x -> !x.getCreatorDev().getDeveloperId().equals(this.getDeveloperId()))
                 .filter(x -> x.hasAtLeastOneCommentFromDeveloper(this))
                 .map(x -> x.getReactionTimeFromDeveloper(this))
                 .sorted(Comparator.comparing(Long::longValue))
                 .collect(Collectors.toList());
    }

    public List<Long> getMergeTimeOfCreatedPR() {

        return createdPullRequests.stream()
                .filter(x -> x.getMergeDate() != null)
                .map(x -> x.getTimeForMerging())
                .sorted(Comparator.comparing(Long::longValue))
                .collect(Collectors.toList());
    }

    public List<Double> getResponseTimeOnComments() {

        return createdPullRequests.stream()
                .filter(x -> x.hasAtLeastOneCommentFromDeveloper(this))
                .map(x ->  x.getMeanResponseTimeOnCommentsFromDeveloper(this))
                .sorted(Comparator.comparing(Double::doubleValue))
                .collect(Collectors.toList());
    }

    public List<DeveloperActivity> getTimeline() {

        List<DeveloperActivity> activities = createdPullRequests.stream()
                .map(x -> new OpenPRActivity(x.getCreationDate(), "Opened a pull request", x.getPullRequestId()))
                .collect(Collectors.toList());

        activities.addAll(
                mergedPullRequests.stream()
                    .map(x -> new MergePRActivity(x.getMergeDate(), "Merged a pull request", x.getPullRequestId()))
                    .collect(Collectors.toList())
        );

        activities.addAll(
                createdPullRequests.stream()
                    .filter(x -> (x.getStatus().equals("closed") && x.getMergeDate() == null) || x.getStatus().equals("declined"))
                    .map(x -> new ClosePRActivity(x.getCloseDate(), "Closed a pull request", x.getPullRequestId()))
                    .collect(Collectors.toList())
        );

        activities.addAll(
                comments.stream()
                    .map(x -> new CommentActivity(x.getTimestamp(), "Commented on a pull request", x.getCommentId()))
                    .collect(Collectors.toList())
        );

        activities.addAll(
                commits.stream()
                    .map (x -> new CommitActivity(x.getTimestamp(), "Commited on a pull request", x.getCommitId()))
                    .collect(Collectors.toList())
        );

        return activities.stream()
                .sorted(Comparator.comparing(DeveloperActivity::getTimestamp))
                .collect(Collectors.toList());
    }
}

