package org.analyzer.pullrequestanalyzer.controller;

import com.opencsv.CSVWriter;
import com.sun.deploy.ref.Helpers;
import org.analyzer.pullrequestanalyzer.analysis.DeveloperAnalysis;
import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.analyzer.pullrequestanalyzer.dto.*;
import org.analyzer.pullrequestanalyzer.logic.pr_activity.PullRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/pullrequests")
public class PullRequestController {

    private DTORegistry dtoRegistry;

    @Autowired
    public PullRequestController (DTORegistry DTORegistry) {
        this.dtoRegistry = DTORegistry;
    }

    @GetMapping("")
    public List<PullRequestDTO> getAllPullRequests() throws IOException {

        return dtoRegistry.getPullRequestDTOS();
    }

    @GetMapping("/overview")
    public void overview(HttpServletResponse response) throws Exception {

        PullRequestAnalysisDTO pullRequestAnalysisDTO = dtoRegistry.getPullRequestAnalysisDTO();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("source,target,value");

        writer.println("Created PRs,Open PRs," + pullRequestAnalysisDTO.getOpenPRs());
        writer.println("Created PRs,Merged PRs," + pullRequestAnalysisDTO.getMergedPRs());
        writer.println("Created PRs,Declined PRs," + pullRequestAnalysisDTO.getClosedPRsWithoutMerge());
        writer.println("Open PRs,Open for a long time PRs," + pullRequestAnalysisDTO.getOpenForALongTimePRs());
        writer.println("Open PRs, ," + (pullRequestAnalysisDTO.getOpenPRs() - pullRequestAnalysisDTO.getOpenForALongTimePRs()));
        writer.println("Merged PRs,Long time to merge PRs," + pullRequestAnalysisDTO.getLongTimeToMergePRs());
        writer.println("Merged PRs, PRs without activity," + pullRequestAnalysisDTO.getPrsWithoutActivity());
        writer.println("Merged PRs, PRs merged on the same branch," + pullRequestAnalysisDTO.getBranchBranchPRs());
        writer.println("Merged PRs, Complex PRs," + pullRequestAnalysisDTO.getComplexPRs());
        writer.println("Merged PRs,  ," + (pullRequestAnalysisDTO.getMergedPRs() - pullRequestAnalysisDTO.getComplexPRs() - pullRequestAnalysisDTO.getBranchBranchPRs() - pullRequestAnalysisDTO.getLongTimeToMergePRs() - pullRequestAnalysisDTO.getPrsWithoutActivity()));
        writer.close();
    }

    @GetMapping("/daily_created")
    public void dailyCreated(HttpServletResponse response) throws IOException {

        List<PullRequestDTO> pullRequestDTO = dtoRegistry.getPullRequestDTOS();
        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("date,value");

        List<Date> dates = pullRequestDTO.stream().map(x -> x.getCreationDate()).collect(Collectors.toList());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");


        Map<String, Long> days = dates.stream()
                .collect(Collectors.groupingBy(x -> dateFormat.format(new Date(x.getTime())), Collectors.counting()));

        for (Map.Entry<String, Long> entry : days.entrySet())
            writer.println(entry.getKey() + "," + entry.getValue());
        writer.close();
    }

    @GetMapping("/comments")
    public void comments(HttpServletResponse response) throws IOException {

        List<CommentDTO> allComments = dtoRegistry.getCommentDTOS();
        response.setContentType("text/plain; charset=utf-8");

        PrintWriter writer = response.getWriter();
        writer.println("entity,value");
        writer.println("Comments on code," + allComments.stream()
                .filter(x -> x.getLineOfCode() != 0)
                .count());
        writer.println("Simple comments," + allComments.stream()
                .filter(x -> x.getLineOfCode() == 0)
                .count());
        writer.close();
    }

    @GetMapping("/commits")
    public void commits(HttpServletResponse response) throws IOException {

        List<CommitDTO> allCommits = dtoRegistry.getCommitDTOS();
        response.setContentType("text/plain; charset=utf-8");

        PrintWriter writer = response.getWriter();
        writer.println("entity,value");
        long followUpCommits = allCommits.stream()
                .filter(x -> x.getTimestamp().getTime() > dtoRegistry.getPullRequestById(x.getPullRequest()).getCreationDate().getTime())
                .count();
        writer.println("Initial commits," + (allCommits.size() - followUpCommits));
        writer.println("Follow up commits," + followUpCommits);
        writer.close();
    }

    @GetMapping("/mean_comments_commits_files")
    public void meanCommentsAndCommits(HttpServletResponse response) throws IOException {

        PullRequestAnalysisDTO pullRequestAnalysisDTO = dtoRegistry.getPullRequestAnalysisDTO();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("entity,value");
        writer.println("Mean no of comments / PR," + pullRequestAnalysisDTO.getMeanNoOfCommentsPerPR());
        writer.println("Mean no of comments on code / PR," + pullRequestAnalysisDTO.getMeanNoOfCommentsOnCodePerPR());
        writer.println("Mean no of simple comments / PR," + pullRequestAnalysisDTO.getMeanNoOfSimpleCommentsPerPR());
        writer.println("Mean no of commits / PR, " + pullRequestAnalysisDTO.getMeanNoOfCommitsPerPR());
        writer.println("Mean no of follow up commits / PR," + pullRequestAnalysisDTO.getMeanNoOfFollowUpCommitsPerPR());
        writer.println("Mean no of changed files / PR," + pullRequestAnalysisDTO.getMeanNoOfChangedFilesPerPR());
        writer.close();
    }

    @GetMapping("/crowded_periods")
    public void crowdedPeriods(HttpServletResponse response) throws IOException {

        List<PullRequestDTO> pullRequestDTO = dtoRegistry.getPullRequestDTOS();
        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("date,value");

        List<Date> dates = pullRequestDTO.stream()
                .map(x -> x.getCreationDate())
                .collect(Collectors.toList());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        Map<String, Long> days = dates.stream()
                .collect(Collectors.groupingBy(x -> dateFormat.format(new Date(x.getTime())), Collectors.counting()));
        TreeMap<String, Long> orderedDays = new TreeMap<>(days);

        for (Map.Entry<String, Long> entry : orderedDays.entrySet())
            writer.println(entry.getKey() + "," + entry.getValue());
        writer.close();
    }

    @GetMapping("/file_changes")
    public void files (HttpServletResponse response) throws IOException {

        List<FileDTO> fileDTOS = dtoRegistry.getFileDTOS();

        int additions = fileDTOS.stream().mapToInt(x -> x.getAdditions()).sum();
        int deletions = fileDTOS.stream().mapToInt(x -> x.getDeletions()).sum();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();

        writer.println("entity,value");
        writer.println("Additions," + additions);
        writer.println("Deletions," + deletions);
        writer.close();
    }
}


