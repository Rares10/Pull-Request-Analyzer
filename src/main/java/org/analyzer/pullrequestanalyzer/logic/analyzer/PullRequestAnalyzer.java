package org.analyzer.pullrequestanalyzer.logic.analyzer;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.analysis.PullRequestAnalysis;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.analyzer.pullrequestanalyzer.logic.pr_activity.PullRequestEvent;
import org.analyzer.pullrequestanalyzer.registry.DeveloperRegistry;
import org.analyzer.pullrequestanalyzer.registry.PullRequest;
import org.analyzer.pullrequestanalyzer.registry.PullRequestRegistry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

@Data
@Service
public class PullRequestAnalyzer {

    private DeveloperRegistry developerRegistry;
    private PullRequestRegistry pullRequestRegistry;
    private Statistics statistics;

    @Autowired
    public PullRequestAnalyzer(DeveloperRegistry developerRegistry,
                               PullRequestRegistry pullRequestRegistry,
                               Statistics statistics) {

        this.developerRegistry = developerRegistry;
        this.pullRequestRegistry = pullRequestRegistry;
        this.statistics = statistics;
    }

    public PullRequestAnalysis run(ProjectData projectToAnalyze) throws IOException {

        List<PullRequest> openPRs = getOpenPRs();
        List<PullRequest> closedPRsWithoutMerge = getClosedPRsWithoutMerge();
        List<PullRequest> mergedPRs = getMergedPRs();
        List<PullRequest> longTimeToMergePRs = getLongTimeToMergePRs();
        List<PullRequest> openForALongTimePRs = getOpenForALongTimePRs();
        List<PullRequest> approvedWithoutActivityPRs = getApprovedWithoutActivityPRs();
        List<PullRequest> complexPRs = getComplexPRs();
        List<Date> dailyOpenedPRs = getDailyOpenedPRs();
        List<Date> dailyMergedPRs = getDailyMergedPRs();
        List<Integer> noOfCommentsPerPR = getNoOfCommentsPerPR();
        List<Integer> noOfCommitsPerPR = getNoOfCommitsPerPR();
        List<Long> noOfFollowUpCommitsPerPR = getNoOfFollowUpCommitsPerPR();
        List<Integer> noOfChangedFiles = getNoOfChangedFilesPerPR();
        List<Long> noOfCommentsOnCodePerPR = getNoOfCommentsOnCodePerPR();
        List<Long> noOfSimpleCommentsPerPR = getNoOfSimpleCommentsPerPR();
        List<PullRequest> branchBranchPRs = getNoOfBranchBranchPRs();
        List<Double> reactionTimesOnPRs = getReactionTimesOnPRs();
        List<Double> responseTimesOfPROwner = getResponseTimesOfPROwner();
        List<Long> mergeTimesOfPRs = getMergeTimesOfPRs();
        TreeMap<Date, Long> crowdedDays = new TreeMap<>();
        TreeMap<Date, Long> crowdedTwoDays = new TreeMap<>();
        TreeMap<Date, Long> crowdedThreeDays = new TreeMap<>();
        calculateCrowdedPeriods(crowdedDays, crowdedTwoDays, crowdedThreeDays);
        Map<Long, List<PullRequestEvent>> timeline = getTimeline();

        PullRequestAnalysis pullRequestAnalysis = new PullRequestAnalysis();
        pullRequestAnalysis.setOwner(projectToAnalyze.getOwner());
        pullRequestAnalysis.setProjectName(projectToAnalyze.getRepository());
        pullRequestAnalysis.setOpenPRs(openPRs);
        pullRequestAnalysis.setClosedPRsWithoutMerge(closedPRsWithoutMerge);
        pullRequestAnalysis.setMergedPRs(mergedPRs);
        pullRequestAnalysis.setLongTimeToMergePRs(longTimeToMergePRs);
        pullRequestAnalysis.setOpenForALongTimePRs(openForALongTimePRs);
        pullRequestAnalysis.setPrsWithoutActivity(approvedWithoutActivityPRs);
        pullRequestAnalysis.setComplexPRs(complexPRs);
        pullRequestAnalysis.setDailyOpenedPRs(dailyOpenedPRs);
        pullRequestAnalysis.setDailyMergedPRs(dailyMergedPRs);
        pullRequestAnalysis.setNoOfCommentsPerPR(noOfCommentsPerPR);
        pullRequestAnalysis.setNoOfCommitsPerPR(noOfCommitsPerPR);
        pullRequestAnalysis.setNoOfFollowUpCommitsPerPR(noOfFollowUpCommitsPerPR);
        pullRequestAnalysis.setNoOfChangedFilesPerPR(noOfChangedFiles);
        pullRequestAnalysis.setNoOfCommentsOnCodePerPR(noOfCommentsOnCodePerPR);
        pullRequestAnalysis.setNoOfSimpleCommentsPerPR(noOfSimpleCommentsPerPR);
        pullRequestAnalysis.setBranchBranchPRs(branchBranchPRs);
        pullRequestAnalysis.setReactionTimesOnPRs(reactionTimesOnPRs);
        pullRequestAnalysis.setResponseTimesOfPROwner(responseTimesOfPROwner);
        pullRequestAnalysis.setMergeTimesOfPRs(mergeTimesOfPRs);
        pullRequestAnalysis.setCrowdedDays(crowdedDays);
        pullRequestAnalysis.setCrowdedTwoDays(crowdedTwoDays);
        pullRequestAnalysis.setCrowdedThreeDays(crowdedThreeDays);
        pullRequestAnalysis.setPRTimeline(timeline);

        //exportAnalysis(pullRequestAnalysis, projectToAnalyze);
        return pullRequestAnalysis;
    }

    private void exportAnalysis(PullRequestAnalysis pullRequestAnalysis, ProjectData projectToAnalyze) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        exportAnalysisValues(pullRequestAnalysis, workbook);
        exportCrowdedPeriods(pullRequestAnalysis, workbook);
        exportTimeline(pullRequestAnalysis, workbook);

        FileOutputStream file = new FileOutputStream("results/" + projectToAnalyze.getOwner() + "-" + projectToAnalyze.getRepository() + " pullrequests_results.xlsx");
        workbook.write(file);
        file.close();
        workbook.close();
    }

    private void exportTimeline(PullRequestAnalysis pullRequestAnalysis, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Timeline");
        CreationHelper createHelper = workbook.getCreationHelper();

        int rowNo = 0;
        int cellNo;
        int maxCellNo = -1;

        for (Map.Entry<Long, List<PullRequestEvent>> entry : pullRequestAnalysis.getPRTimeline().entrySet()) {

            Row timeRow = sheet.createRow(rowNo++);

            CellStyle timeCellStyle = workbook.createCellStyle();
            timeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy HH:mm:ss"));
            timeCellStyle.setAlignment(HorizontalAlignment.CENTER);

            cellNo = 1;

            for (PullRequestEvent prActivity : entry.getValue()) {

                Cell cell = timeRow.createCell(cellNo++);
                cell.setCellValue(prActivity.getTimestamp());
                cell.setCellStyle(timeCellStyle);
            }

            Row activityRow = sheet.createRow(rowNo++);

            CellStyle activityCellStyle = workbook.createCellStyle();
            activityCellStyle.setAlignment(HorizontalAlignment.CENTER);

            Font nameFont = workbook.createFont();
            nameFont.setBold(true);
            nameFont.setFontHeightInPoints((short) 14);
            nameFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle nameCellStyle = workbook.createCellStyle();
            nameCellStyle.setAlignment(HorizontalAlignment.CENTER);
            nameCellStyle.setFont(nameFont);

            Cell nameCell = activityRow.createCell(0);
            nameCell.setCellStyle(nameCellStyle);
            nameCell.setCellValue(entry.getKey());

            cellNo = 1;

            for (PullRequestEvent prActivity : entry.getValue()) {

                Cell cell = activityRow.createCell(cellNo++);
                cell.setCellValue(prActivity.toString());
                cell.setCellStyle(activityCellStyle);
            }

            if (cellNo > maxCellNo)
                maxCellNo = cellNo;
            rowNo = rowNo + 2;
        }

        for (int i = 0; i <= maxCellNo; i++)
            sheet.autoSizeColumn(i);
    }

    private void exportCrowdedPeriods(PullRequestAnalysis pullRequestAnalysis, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Crowded periods");

        CreationHelper createHelper = workbook.getCreationHelper();

        CellStyle timeCellStyle = workbook.createCellStyle();
        timeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        timeCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Font firstCollumnFont = workbook.createFont();
        firstCollumnFont.setBold(true);
        firstCollumnFont.setFontHeightInPoints((short) 14);
        firstCollumnFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle firstCollumnStyle = workbook.createCellStyle();
        firstCollumnStyle.setAlignment(HorizontalAlignment.CENTER);
        firstCollumnStyle.setFont(firstCollumnFont);

        Row firstRow = createFirstCell(sheet, firstCollumnStyle, "Crowded days", 0);
        Row secondRow = createFirstCell(sheet, firstCollumnStyle, "", 1);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        List<Date> listOfCrowdedDays = new ArrayList<>(pullRequestAnalysis.getCrowdedDays().keySet());

        for (int i = 0; i < listOfCrowdedDays.size(); i++) {

            Cell cell = firstRow.createCell(i+1);
            cell.setCellStyle(timeCellStyle);
            cell.setCellValue(listOfCrowdedDays.get(i));

            cell = secondRow.createCell(i+1);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(pullRequestAnalysis.getCrowdedDays().get(listOfCrowdedDays.get(i)));
        }

        firstRow = createFirstCell(sheet, firstCollumnStyle, "Crowded two days", 3);
        secondRow = createFirstCell(sheet, firstCollumnStyle, "", 4);

        List<Date> listOfCrowdedTwoDays = new ArrayList<>(pullRequestAnalysis.getCrowdedTwoDays().keySet());

        for (int i = 0; i < listOfCrowdedTwoDays.size(); i++) {

            Cell cell = firstRow.createCell(i+1);
            cell.setCellStyle(timeCellStyle);
            cell.setCellValue(listOfCrowdedTwoDays.get(i));

            cell = secondRow.createCell(i+1);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(pullRequestAnalysis.getCrowdedTwoDays().get(listOfCrowdedTwoDays.get(i)));
        }

        firstRow = createFirstCell(sheet, firstCollumnStyle, "Crowded three days", 6);
        secondRow = createFirstCell(sheet, firstCollumnStyle, "", 7);

        List<Date> listOfCrowdedThreeDays = new ArrayList<>(pullRequestAnalysis.getCrowdedThreeDays().keySet());

        for (int i = 0; i < listOfCrowdedThreeDays.size(); i++) {

            Cell cell = firstRow.createCell(i+1);
            cell.setCellStyle(timeCellStyle);
            cell.setCellValue(listOfCrowdedThreeDays.get(i));

            cell = secondRow.createCell(i+1);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(pullRequestAnalysis.getCrowdedThreeDays().get(listOfCrowdedThreeDays.get(i)));
        }

        sheet.setDefaultColumnWidth(20);
    }

    private Row createFirstCell(Sheet sheet, CellStyle cellStyle, String value, int i) {

        Row row = sheet.createRow(i);

        Cell firstCell = row.createCell(0);
        firstCell.setCellStyle(cellStyle);
        firstCell.setCellValue(value);

        return row;
    }

    private void exportAnalysisValues(PullRequestAnalysis pullRequestAnalysis, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Pullrequests Analysis");

        Font firstCollumnFont = workbook.createFont();
        firstCollumnFont.setFontHeightInPoints((short) 14);
        firstCollumnFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle firstCollumnStyle = workbook.createCellStyle();
        firstCollumnStyle.setFont(firstCollumnFont);

        String[] rows = {"Project name",
                "Project owner",
                "Created PRs",
                "Open PRs",
                "Closed without merge PRs",
                "Merged PRs",
                "Long time to merge PRs",
                "Open for a long time PRs",
                "No of PRs without activity",
                "No of complex PRs",
                "Mean open PRs per week",
                "Median value of open PRs per week",
                "Mean open PRs per day",
                "Median value of open PRs per day",
                "Mean merged PRs per week",
                "Median value of merged PRs per week",
                "Mean merged PRs per day",
                "Median value of merged PRs per day",
                "Mean no of comments per PR",
                "Median value of comments per PR",
                "Mean no of comments on code per PR",
                "Median value of comments on code per PR",
                "Mean no of simple comments per PR",
                "Median value of simple comments per PR",
                "Mean no of commits per PR",
                "Median value of commits per PR",
                "Mean no of follow up commits per PR",
                "Median value of follow up commits per PR",
                "Mean no of changed files per PR",
                "Median value of changed files per PR",
                "No of PRs made on the same branch",
                "Mean reaction time on PRs (hrs)",
                "Median value of reaction times on PRs (hrs)",
                "Mean response time of PR owner (hrs)",
                "Median value of response times of PR owner (hrs)",
                "Mean merge time of PRs (days)",
                "Median value of merge times of PRs (days)"};


        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        int i = 0;

        Row row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        Cell cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getProjectName());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getOwner());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getOpenPRs().size() + pullRequestAnalysis.getMergedPRs().size() + pullRequestAnalysis.getClosedPRsWithoutMerge().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getOpenPRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getClosedPRsWithoutMerge().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getMergedPRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getLongTimeToMergePRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getOpenForALongTimePRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getPrsWithoutActivity().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getComplexPRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateWeeklyAverage(pullRequestAnalysis.getDailyOpenedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateWeeklyMedianValue(pullRequestAnalysis.getDailyOpenedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDailyAverage(pullRequestAnalysis.getDailyOpenedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDailyMedianValue(pullRequestAnalysis.getDailyOpenedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateWeeklyAverage(pullRequestAnalysis.getDailyMergedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateWeeklyMedianValue(pullRequestAnalysis.getDailyMergedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDailyAverage(pullRequestAnalysis.getDailyMergedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDailyMedianValue(pullRequestAnalysis.getDailyMergedPRs()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfCommentsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfCommentsPerPR()));


        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfCommentsOnCodePerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfCommentsOnCodePerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfSimpleCommentsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfSimpleCommentsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfCommitsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfCommitsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongsAverage(pullRequestAnalysis.getNoOfFollowUpCommitsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongMedianValue(pullRequestAnalysis.getNoOfFollowUpCommitsPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntegersAverage(pullRequestAnalysis.getNoOfChangedFilesPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateIntMedianValue(pullRequestAnalysis.getNoOfChangedFilesPerPR()));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(pullRequestAnalysis.getBranchBranchPRs().size());

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDoublesAverage(pullRequestAnalysis.getReactionTimesOnPRs()) / (1000*60*60));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDoubleMedianValue(pullRequestAnalysis.getReactionTimesOnPRs()) / (1000*60*60));


        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDoublesAverage(pullRequestAnalysis.getResponseTimesOfPROwner()) / (1000*60*60));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateDoubleMedianValue(pullRequestAnalysis.getResponseTimesOfPROwner()) / (1000*60*60));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongsAverage(pullRequestAnalysis.getMergeTimesOfPRs()) / (1000*60*60*24));

        row = createFirstCell(sheet, firstCollumnStyle, rows[i], i++);

        cell = row.createCell(1);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(statistics.calculateLongMedianValue(pullRequestAnalysis.getMergeTimesOfPRs()) / (1000*60*60*24));

       sheet.setDefaultColumnWidth(60);
    }

    private List<PullRequest> getOpenPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getStatus().equals("open") && x.getMergeDev() == null)
                .collect(Collectors.toList());
    }

    private List<PullRequest> getMergedPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getMergeDate() != null)
                .collect(Collectors.toList());
    }

    private List<PullRequest> getLongTimeToMergePRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getMergeDate() != null)
                .filter(x -> (x.getTimeForMerging() / (1000*60*60*24)) > 10)
                .collect(Collectors.toList());
    }

    private List<PullRequest> getClosedPRsWithoutMerge() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getStatus().equals("declined") || (x.getStatus().equals("closed") && x.getMergeDev() == null))
                .collect(Collectors.toList());
    }

    private List<PullRequest> getOpenForALongTimePRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getStatus().equals("open"))
                .filter(x -> x.isOpenForALongTime())
                .collect(Collectors.toList());
    }

    private List<PullRequest> getApprovedWithoutActivityPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getMergeDate() != null)
                .filter(x -> x.getComments().size() == 0)
                .collect(Collectors.toList());
    }

    private List<PullRequest> getComplexPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.isOpenForALongTime() || (x.getMergeDate() != null && (x.getTimeForMerging() / (1000*60*60*24)) > 7))
                .filter(x -> x.getFilesModified().size() < 4)
                .filter(x -> x.getNoOfFollowUpCommits() >= 2)
                .filter(x -> x.getComments().size() >= 8)
                .collect(Collectors.toList());
    }

    private List<Date> getDailyOpenedPRs() {

       return pullRequestRegistry.getPullRequests().stream()
               .sorted(Comparator.comparing(PullRequest::getCreationDate))
               .map(x -> x.getCreationDate())
               .collect(Collectors.toList());
    }

    private List<Date> getDailyMergedPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getMergeDate() != null)
                .sorted(Comparator.comparing(PullRequest::getMergeDate))
                .map(x -> x.getMergeDate())
                .collect(Collectors.toList());

    }

    private List<Integer> getNoOfCommentsPerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getComments().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    private List<Long> getNoOfCommentsOnCodePerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getCommentsOnCode())
                .sorted(Comparator.comparing(Long::longValue))
                .collect(Collectors.toList());
    }

    private List<Long> getNoOfSimpleCommentsPerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getSimpleComments())
                .sorted(Comparator.comparing(Long::longValue))
                .collect(Collectors.toList());
    }

    private List<Integer> getNoOfCommitsPerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getCommits().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    private List<Long> getNoOfFollowUpCommitsPerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getNoOfFollowUpCommits())
                .collect(Collectors.toList());
    }

    private List<Integer> getNoOfChangedFilesPerPR() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getFilesModified().size())
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
    }

    private List<PullRequest> getNoOfBranchBranchPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getInitBranch().equals(x.getFinalBranch()))
                .collect(Collectors.toList());
    }

    private List<Double> getReactionTimesOnPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .map(x -> x.getReactionTime())
                .filter(x -> x != 0)
                .sorted(Comparator.comparing(Double::doubleValue))
                .collect(Collectors.toList());
    }

    private List<Double> getResponseTimesOfPROwner() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.hasAtLeastOneCommentFromDeveloper(x.getCreatorDev()))
                .map(x -> x.getMeanResponseTimeOnCommentsFromDeveloper(x.getCreatorDev()))
                .filter(x -> x != 0)
                .sorted(Comparator.comparing(Double::doubleValue))
                .collect(Collectors.toList());
    }

    private List<Long> getMergeTimesOfPRs() {

        return pullRequestRegistry.getPullRequests().stream()
                .filter(x -> x.getMergeDate() != null)
                .map(x -> x.getTimeForMerging())
                .sorted(Comparator.comparing(Long::longValue))
                .collect(Collectors.toList());
    }

    private void calculateCrowdedPeriods(TreeMap<Date, Long> manyPRsOneDay,
                                         TreeMap<Date, Long> manyPRsTwoDays,
                                         TreeMap<Date, Long> manyPRsThreeDays) {

        final long oneDayThreshold = 5;
        final long twoDaysThreshold = 12;
        final long threeDaysThreshold = 20;

        Map<Date, Long> dailyPRs = pullRequestRegistry.getPullRequests().stream()
                .sorted(Comparator.comparing(PullRequest::getCreationDate))
                .collect(Collectors.groupingBy(PullRequest::getOnlyDateOfCreation, Collectors.counting()));

        dailyPRs = new TreeMap<>(dailyPRs);


        for (Map.Entry<Date, Long> mapEntry : dailyPRs.entrySet()) {

            if (mapEntry.getValue() > oneDayThreshold)
                manyPRsOneDay.put(mapEntry.getKey(), mapEntry.getValue());
        }

        List<Date> dates = new ArrayList<>(dailyPRs.keySet());

        for (int i = 1; i < dates.size(); i++) {

            Long first = dailyPRs.get(dates.get(i-1));
            Long second = dailyPRs.get(dates.get(i));
            double proportion = 0.7 * (first + second);

            if (first + second > twoDaysThreshold && first < proportion && second < proportion) {

                if (getDayNo(dates.get(i)) == getDayNo(dates.get(i-1)) + 1) {

                    manyPRsTwoDays.put(dates.get(i - 1), first);
                    manyPRsTwoDays.put(dates.get(i), second);
                }
            }
        }

        for (int i = 2; i < dates.size(); i++) {

            Long first = dailyPRs.get(dates.get(i-2));
            Long second = dailyPRs.get(dates.get(i-1));
            Long third = dailyPRs.get(dates.get(i));
            double proportion = 0.2 * (first + second + third);

            if (first + second + third > threeDaysThreshold && first > proportion && second > proportion && third > proportion) {

                if ((getDayNo(dates.get(i)) == getDayNo(dates.get(i-1)) + 1) && (getDayNo(dates.get(i-1)) == getDayNo(dates.get(i-2)) + 1)) {

                    manyPRsThreeDays.put(dates.get(i - 2), first);
                    manyPRsThreeDays.put(dates.get(i - 1), second);
                    manyPRsThreeDays.put(dates.get(i), third);
                }
            }
        }

        int i;

        List<Date> listOfKeysForTwoDays = new ArrayList<>(manyPRsTwoDays.keySet());

        for (i = 0; i < manyPRsTwoDays.size(); i++) {

            if (manyPRsOneDay.containsKey(listOfKeysForTwoDays.get(i)))
                manyPRsOneDay.remove(listOfKeysForTwoDays.get(i));
        }

        List<Date> listOfKeysForThreeDays = new ArrayList<>(manyPRsThreeDays.keySet());

        for (i = 0; i < manyPRsThreeDays.size(); i++) {

            if (manyPRsTwoDays.containsKey(listOfKeysForThreeDays.get(i)))
                manyPRsTwoDays.remove(listOfKeysForThreeDays.get(i));
        }
    }

    private int getDayNo(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    private Map<Long, List<PullRequestEvent>> getTimeline() {

        return pullRequestRegistry.getPullRequests().stream()
                .collect(Collectors.toMap(x -> x.getPullRequestId(), x -> x.getTimeline()));
    }
}
