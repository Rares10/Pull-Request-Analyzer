package org.analyzer.pullrequestanalyzer.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.logic.pr_activity.*;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PullRequest {

    private Long pullRequestId;
    private String title;
    private String status;
    private Date creationDate;
    private Date mergeDate;
    private Date closeDate;
    private Developer creatorDev;
    private Developer mergeDev;
    private List<Comment> comments;
    private List<Commit> commits;
    private List<File> filesModified;
    private String initBranch;
    private String finalBranch;

    public Date getOnlyDateOfCreation() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(creationDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public boolean isOpenForALongTime() {

        Date today = new Date();
        long time = today.getTime() - creationDate.getTime();
        long days = time / (1000*60*60*24);
        if (days > 7)
            return true;
        return false;
    }

    public long getTimeForMerging() {

        return (mergeDate.getTime() - creationDate.getTime());
    }

    public long getReactionTimeFromDeveloper(Developer developer) {

        Comment firstComment = comments.stream()
                .filter(x -> x.getAuthor() != null && x.getAuthor().getDeveloperId().equals(developer.getDeveloperId()))
                .min(Comparator.comparing(Comment::getTimestamp)).orElse(null);

        if (firstComment != null){
            return firstComment.getTimeSincePRCreation();
        }
        return 0L;
    }

    public boolean hasAtLeastOneCommentFromDeveloper(Developer developer) {

        Comment comment = comments.stream()
                .filter(x -> x.getAuthor() != null && x.getAuthor().getDeveloperId().equals(developer.getDeveloperId()))
                .findAny().orElse(null);

        if (comment == null)
            return false;
        return true;
    }

    public double getMeanResponseTimeOnCommentsFromDeveloper(Developer developer) {

        Map<String, List<Comment>> filesWithCommentsWithFile = comments.stream()
                .filter(x -> x.getFile() != null)
                .collect(Collectors.groupingBy(Comment::getFile, Collectors.toList()));

        List<Comment> commentsWithoutFile = comments.stream()
                .filter(x -> x.getFile() == null)
                .collect(Collectors.toList());

        Map<String, Double> filesWithResponseTime = filesWithCommentsWithFile.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> getMeanResponseTime(x.getValue(), developer, comments)));

        double meanResponseTimeOnCommentsWithFile = filesWithResponseTime.values().stream()
                .filter(x -> !x.equals(0.0))
                .mapToDouble(x -> x)
                .average().orElse(0);

        double meanResponseTimeOnCommentsWithoutFile = getMeanResponseTime(commentsWithoutFile, developer, comments);

        if (meanResponseTimeOnCommentsWithFile == 0)
            return meanResponseTimeOnCommentsWithoutFile;
        else if (meanResponseTimeOnCommentsWithoutFile == 0)
            return meanResponseTimeOnCommentsWithFile;
        else
            return (meanResponseTimeOnCommentsWithFile + meanResponseTimeOnCommentsWithoutFile) / 2;
    }

    private double getMeanResponseTime(List<Comment> comments, Developer developer, List<Comment> allComments) {

        long responseTimes = 0;
        long noOfTimes = 0;

        comments.sort(Comparator.comparing(Comment::getTimestamp));
        Comment[] commentsArray = new Comment[comments.size()];
        commentsArray = comments.toArray(commentsArray);

        Comment previousComment;

        for (int i = 1; i < commentsArray.length; i++) {

            previousComment = commentsArray[i-1];

            if (commentsArray[i].getAuthor() != null &&
                    commentsArray[i].getAuthor().getDeveloperId().equals(developer.getDeveloperId()) &&
                    commentsArray[i].getParentId() != null) {

                responseTimes += commentsArray[i].getTimestamp().getTime() - getTimeFromCommentId(commentsArray[i].getParentId(), allComments);
                noOfTimes++;
            }
            else if (commentsArray[i].getAuthor() != null &&
                    commentsArray[i].getAuthor().getDeveloperId().equals(developer.getDeveloperId()) &&
                    previousComment.getAuthor() != null &&
                    !previousComment.getAuthor().getDeveloperId().equals(developer.getDeveloperId())) {

                responseTimes += commentsArray[i].getTimestamp().getTime() - previousComment.getTimestamp().getTime();
                noOfTimes++;
            }
        }
        if (noOfTimes == 0)
            return 0;
        else
            return (double) responseTimes / noOfTimes;
    }

    private long getTimeFromCommentId(long id, List<Comment> comments) {

        Comment comment = comments.stream()
                .filter(x -> x.getCommentId().equals(id))
                .findAny().orElse(null);

        if (comment == null)
            return 0;
        return comment.getTimestamp().getTime();
    }

    public boolean hasTwoCommentsFromDeveloper(String developer) {

        long commentCount = comments.stream()
                .filter(x -> x.getAuthor() != null && x.getAuthor().getUsername().equals(developer))
                .count();
        if (commentCount >= 2)
            return true;
        return false;
    }

    public void calculateInteractions(List<String> developers, int[][] interactions) {

        Set<String> authors = comments.stream()
                .filter(x -> x.getAuthor() != null)
                .map(x -> x.getAuthor().getUsername())
                .collect(Collectors.toSet());

        List<String> developersWhoCommunicate = authors.stream()
                .filter(x -> this.hasTwoCommentsFromDeveloper(x))
                .collect(Collectors.toList());

        if (developersWhoCommunicate.size() > 1) {

            String[] devs = new String[developersWhoCommunicate.size()];
            devs = developersWhoCommunicate.toArray(devs);

            for (int i = 0 ; i < devs.length - 1; i++) {
                for (int j= i+1; j < devs.length; j++) {
                    int devA = developers.indexOf(devs[i]);
                    int devB = developers.indexOf(devs[j]);
                    interactions[devA][devB]++;
                    interactions[devB][devA]++;
                }
            }
        }
    }

    public long getCommentsOnCode() {

        return comments.stream()
                .filter(x -> x.getLineOfCode() != 0)
                .count();
    }

    public long getSimpleComments() {

        return comments.size() - getCommentsOnCode();
    }

    public double getReactionTime() {

        Comment firstComment = comments.stream()
                .min(Comparator.comparing(Comment::getTimestamp)).orElse(null);

        if (firstComment != null)
            return firstComment.getTimeSincePRCreation();
        else
            return 0;
    }

    public long getNoOfFollowUpCommits() {

        return commits.stream()
                .filter(x -> x.getTimestamp().getTime() > x.getPullRequest().getCreationDate().getTime())
                .count();
    }

    public List<PullRequestEvent> getTimeline() {

        List<PullRequestEvent> activities = comments.stream()
                .filter(x -> x.getAuthor() != null)
                .map (x -> new CommentEvent(x.getTimestamp(), x.getAuthor().getUsername() + " commented ", x.getCommentId(), x.getAuthor().getDeveloperId()))
                .collect(Collectors.toList());

        List<PullRequestEvent> commitActivities = commits.stream()
                .filter(x -> x.getTimestamp().getTime() > x.getPullRequest().getCreationDate().getTime())
                .filter(x -> x.getAuthor() != null)
                .map(x -> new FollowUpCommitEvent(x.getTimestamp(), x.getAuthor().getUsername() + " commited ",x.getCommitId(), x.getAuthor().getDeveloperId()))
                .collect(Collectors.toList());

        activities.addAll(commitActivities);
        activities.add(new OpenEvent(this.getCreationDate(), this.getCreatorDev().getUsername() + " opened this pull request ", this.getCreatorDev().getDeveloperId()));
        if (mergeDate != null)
            activities.add(new MergeEvent(this.getMergeDate(), this.getMergeDev().getUsername() + " merged this pull request ", this.getMergeDev().getDeveloperId()));
        if (this.getStatus().equals("closed") && this.mergeDate == null)
            activities.add(new CloseEvent(this.getCloseDate(), this.getCreatorDev().getUsername() + " closed this pull request ", this.getCreatorDev().getDeveloperId()));

        activities.sort(Comparator.comparing(PullRequestEvent::getTimestamp));
        return activities;
    }
}

