package org.analyzer.pullrequestanalyzer.logic.analyzer;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.analysis.DeveloperAnalysis;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.analyzer.pullrequestanalyzer.logic.dev_activity.DeveloperActivity;
import org.analyzer.pullrequestanalyzer.registry.Developer;
import org.analyzer.pullrequestanalyzer.registry.DeveloperRegistry;
import org.analyzer.pullrequestanalyzer.registry.PullRequestRegistry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;



@Data
@Service
public class DeveloperAnalyzer {

    private DeveloperRegistry developerRegistry;
    private PullRequestRegistry pullRequestRegistry;
    private Statistics statistics;

    @Autowired
    public DeveloperAnalyzer(DeveloperRegistry developerRegistry,
                             PullRequestRegistry pullRequestRegistry,
                             Statistics statistics) {

        this.developerRegistry = developerRegistry;
        this.pullRequestRegistry = pullRequestRegistry;
        this.statistics = statistics;
    }

    public List<DeveloperAnalysis> run(ProjectData projectToAnalyze) throws IOException {

        List<DeveloperAnalysis> developerAnalyses = new ArrayList<>();

        Map<String, Integer> openedPullRequests = getOpenedPullRequests();
        Map<String, Long> mergedPullRequestsByCreator = getMergedPullRequestsByCreator();
        Map<String, Long> mergedPullRequestsByOthers = getMergedPullRequestsByOthers();
        Map<String, Long> mergedPullRequestsOfOthers = getMergedPullRequestsOfOthers();
        Map<String, Long> closedWithoutMergePR = getClosedWithoutMergePR();
        Map<String, Long> openForALongTimePR = getOpenForALongTimePR();
        Map<String, Long> longTimeToMergePR = getLongTimeToMergePR();
        Map<String, Long> complexPR = getComplexPR();
        Map<String, List<Integer>> noOfCommentsOfCreatedPR = getNoOfCommentsOfCreatedPR();
        Map<String, Long> commentsOnCreatedPR = getNoOfCommentsOnCreatedPR();
        Map<String, Long> commentsOnOtherPR = getNoOfCommentsOnOtherPR();
        Map<String, List<Integer>> noOfCommits = getNoOfCommits();
        Map<String, List<Integer>> noOfChangedFiles = getNoOfChangedFiles();
        Map<String, List<Long>> reactionTimesOnPR = getReactionTimesOnPR();
        Map<String, List<Long>> mergeTimesOfCreatedPR = getMergeTimesOfCreatedPR();
        Map<String, List<Double>> responseTimesOnComments = getResponseTimesOnComments();
        Map<String, List<DeveloperActivity>> developerTimelines = createDevelopersTimeline();
        Map<String,Map<String,Integer>> interactions = calculateInteractions();
        developerRegistry.getDevelopers().values().stream()
                .forEach(x -> saveAnalysis(x,
                        developerAnalyses,
                        openedPullRequests,
                        mergedPullRequestsByCreator,
                        mergedPullRequestsByOthers,
                        mergedPullRequestsOfOthers,
                        closedWithoutMergePR,
                        openForALongTimePR,
                        longTimeToMergePR,
                        complexPR,
                        noOfCommentsOfCreatedPR,
                        commentsOnCreatedPR,
                        commentsOnOtherPR,
                        noOfCommits,
                        noOfChangedFiles,
                        reactionTimesOnPR,
                        mergeTimesOfCreatedPR,
                        responseTimesOnComments,
                        developerTimelines,
                        interactions));

        //exportAnalysis(developerAnalyses, projectToAnalyze);
        return developerAnalyses;
    }

    private void saveAnalysis(Developer developer,
                              List<DeveloperAnalysis> developerAnalyses,
                              Map<String, Integer> openedPullRequests,
                              Map<String, Long> mergedPullRequestsByCreator,
                              Map<String, Long> mergedPullRequestsByOthers,
                              Map<String, Long> mergedPullRequestsOfOthers,
                              Map<String, Long> closedWithoutMergePR,
                              Map<String, Long> openForALongTimePR,
                              Map<String, Long> longTimeToMergePR,
                              Map<String, Long> complexPR,
                              Map<String,List<Integer>> noOfCommentsOfCreatedPR,
                              Map<String, Long> commentsOnCreatedPR,
                              Map<String, Long> commentsOnOtherPR,
                              Map<String,List<Integer>> noOfCommits,
                              Map<String,List<Integer>> noOfChangedFiles,
                              Map<String,List<Long>> reactionTimesOnPR,
                              Map<String,List<Long>> mergeTimesOfCreatedPR,
                              Map<String,List<Double>> responseTimesOnComments,
                              Map<String, List<DeveloperActivity>> developerTimelines,
                              Map<String, Map<String, Integer>> interactions) {

        DeveloperAnalysis currentDeveloperAnalysis = new DeveloperAnalysis();
        String username = developer.getUsername();

        currentDeveloperAnalysis.setDeveloper(developer);
        currentDeveloperAnalysis.setOpenedPR(openedPullRequests.get(username));
        currentDeveloperAnalysis.setMergedPRByCreator(mergedPullRequestsByCreator.get(username));
        currentDeveloperAnalysis.setMergedPRByOthers(mergedPullRequestsByOthers.get(username));
        currentDeveloperAnalysis.setMergedPROfOthers(mergedPullRequestsOfOthers.get(username));
        currentDeveloperAnalysis.setClosedWithoutMergePR(closedWithoutMergePR.get(username));
        currentDeveloperAnalysis.setOpenForALongTimePR(openForALongTimePR.get(username));
        currentDeveloperAnalysis.setLongTimeToMergePR(longTimeToMergePR.get(username));
        currentDeveloperAnalysis.setNoOfComplexPR(complexPR.get(username));
        currentDeveloperAnalysis.setCommentsOnCreatedPR(commentsOnCreatedPR.get(username));
        currentDeveloperAnalysis.setCommentsOnOtherPR(commentsOnOtherPR.get(username));
        currentDeveloperAnalysis.setNoOfCommentsOfCreatedPR(noOfCommentsOfCreatedPR.get(username));
        currentDeveloperAnalysis.setNoOfCommits(noOfCommits.get(username));
        currentDeveloperAnalysis.setNoOfChangedFiles(noOfChangedFiles.get(username));
        currentDeveloperAnalysis.setReactionTimesOnPR(reactionTimesOnPR.get(username));
        currentDeveloperAnalysis.setMergeTimesOfCreatedPR(mergeTimesOfCreatedPR.get(username));
        currentDeveloperAnalysis.setResponseTimesOnComments(responseTimesOnComments.get(username));
        currentDeveloperAnalysis.setTimeline(developerTimelines.get(username));
        currentDeveloperAnalysis.setInteractions(interactions.get(username));

        developerAnalyses.add(currentDeveloperAnalysis);
    }

    private void exportAnalysis(List<DeveloperAnalysis> developerAnalyses, ProjectData projectToAnalyze) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        exportAnalysisValues(developerAnalyses, workbook);
        exportTimeline(developerAnalyses, workbook);
        exportInteractions(developerAnalyses, workbook);

        FileOutputStream file = new FileOutputStream("results/" + projectToAnalyze.getOwner() + "-" + projectToAnalyze.getRepository() + " developers_results.xlsx");
        workbook.write(file);
        file.close();
        workbook.close();
    }

    private void exportAnalysisValues(List<DeveloperAnalysis> developerAnalyses, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Developer Analysis");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Developer name",
                "Opened PRs",
                "Merged PRs of his own by him",
                "Merged PRs of his own by others",
                "Merged PRs of others",
                "Closed without merge PRs",
                "Long time to merge PRs",
                "Open for a long time PRs",
                "No of complex PRs",
                "Mean No of comments of created PR",
                "Median value of comments of created PR",
                "Comments on created PRs",
                "Comments on other PRs",
                "Mean no of commits",
                "Median value of commits",
                "Mean no of changed files",
                "Median value of changed files",
                "Mean reaction time on PR (hrs)",
                "Median value of reaction times (hrs)",
                "Mean merge time of created PR (days)",
                "Median value of merge times of created PR (days)",
                "Mean response time on comments (hrs)",
                "Median value of response times (hrs)"};

        for(int i = 0; i < columns.length; i++) {

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        int rowNo = 1;

        for (DeveloperAnalysis developerAnalysis : developerAnalyses) {

            int i = 0;

            Row row = sheet.createRow(rowNo++);

            Cell cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getDeveloper().getUsername());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getOpenedPR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getMergedPRByCreator());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getMergedPRByOthers());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getMergedPROfOthers());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getClosedWithoutMergePR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getLongTimeToMergePR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getOpenForALongTimePR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getNoOfComplexPR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntegersAverage(developerAnalysis.getNoOfCommentsOfCreatedPR()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntMedianValue(developerAnalysis.getNoOfCommentsOfCreatedPR()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getCommentsOnCreatedPR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(developerAnalysis.getCommentsOnOtherPR());

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntegersAverage(developerAnalysis.getNoOfCommits()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntMedianValue(developerAnalysis.getNoOfCommits()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntegersAverage(developerAnalysis.getNoOfChangedFiles()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateIntMedianValue(developerAnalysis.getNoOfChangedFiles()));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateLongsAverage(developerAnalysis.getReactionTimesOnPR()) / (1000*60*60));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateLongMedianValue(developerAnalysis.getReactionTimesOnPR()) / (1000*60*60));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateLongsAverage(developerAnalysis.getMergeTimesOfCreatedPR()) / (1000*60*60*24));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateLongMedianValue(developerAnalysis.getMergeTimesOfCreatedPR()) / (1000*60*60*24));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateDoublesAverage(developerAnalysis.getResponseTimesOnComments()) / (1000*60*60));

            cell = row.createCell(i++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(statistics.calculateDoubleMedianValue(developerAnalysis.getResponseTimesOnComments()) / (1000*60*60));
        }

        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportTimeline(List<DeveloperAnalysis> developerAnalyses, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Timeline");
        CreationHelper createHelper = workbook.getCreationHelper();

        int rowNo = 0;
        int cellNo;
        int maxCellNo = -1;

        for (DeveloperAnalysis developerAnalysis : developerAnalyses) {

            Row timeRow = sheet.createRow(rowNo++);

            CellStyle timeCellStyle = workbook.createCellStyle();
            timeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy HH:mm:ss"));
            timeCellStyle.setAlignment(HorizontalAlignment.CENTER);

            cellNo = 1;

            for (DeveloperActivity developerActivity : developerAnalysis.getTimeline()) {

                Cell cell = timeRow.createCell(cellNo++);
                cell.setCellValue(developerActivity.getTimestamp());
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
            nameCell.setCellValue(developerAnalysis.getDeveloper().getUsername());

            cellNo = 1;

            for (DeveloperActivity developerActivity : developerAnalysis.getTimeline()) {

                Cell cell = activityRow.createCell(cellNo++);
                cell.setCellValue(developerActivity.toString());
                cell.setCellStyle(activityCellStyle);
            }

            if (cellNo > maxCellNo)
                maxCellNo = cellNo;
            rowNo = rowNo + 2;
        }
        for (int i = 0; i <= maxCellNo; i++)
            sheet.autoSizeColumn(i);
    }

    private void exportInteractions(List<DeveloperAnalysis> developerAnalyses, Workbook workbook) {

        Sheet sheet = workbook.createSheet("Interactions");

        int rowNo = 0;

        Row nameRow = sheet.createRow(rowNo++);

        Font nameFont = workbook.createFont();
        nameFont.setBold(true);
        nameFont.setFontHeightInPoints((short) 14);
        nameFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle nameCellStyle = workbook.createCellStyle();
        nameCellStyle.setAlignment(HorizontalAlignment.CENTER);
        nameCellStyle.setFont(nameFont);

        int cellNo = 1;

        for (DeveloperAnalysis developerAnalysis : developerAnalyses) {

            Cell nameCell = nameRow.createCell(cellNo++);
            nameCell.setCellStyle(nameCellStyle);
            nameCell.setCellValue(developerAnalysis.getDeveloper().getUsername());
        }

        Font firstCellFont = workbook.createFont();
        firstCellFont.setBold(true);
        firstCellFont.setFontHeightInPoints((short) 14);
        firstCellFont.setColor(IndexedColors.BLUE.getIndex());

        CellStyle firstCellStyle = workbook.createCellStyle();
        firstCellStyle.setAlignment(HorizontalAlignment.CENTER);
        firstCellStyle.setFont(firstCellFont);

        CellStyle interactionsCellStyle = workbook.createCellStyle();
        interactionsCellStyle.setAlignment(HorizontalAlignment.CENTER);

        for (DeveloperAnalysis developerAnalysis : developerAnalyses) {

            Row interactionsRow = sheet.createRow(rowNo++);

            cellNo = 0;

            Cell firstCell = interactionsRow.createCell(cellNo++);
            firstCell.setCellStyle(firstCellStyle);
            firstCell.setCellValue(developerAnalysis.getDeveloper().getUsername());

            for (DeveloperAnalysis currentNameAnalysis : developerAnalyses) {

                Cell interactionsCell = interactionsRow.createCell(cellNo++);
                interactionsCell.setCellStyle(interactionsCellStyle);
                 if (developerAnalysis.getInteractions().get(currentNameAnalysis.getDeveloper().getUsername()) == -1)
                    interactionsCell.setCellValue("-");
                else
                    interactionsCell.setCellValue(developerAnalysis.getInteractions().get(currentNameAnalysis.getDeveloper().getUsername()));
            }
        }

        for (int i = 0; i <= developerAnalyses.size(); i++)
            sheet.autoSizeColumn(i);
    }

    private Map<String, Integer> getOpenedPullRequests(){

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(),x -> x.getValue().getCreatedPullRequests().size()));
    }


    private Map<String, Long> getMergedPullRequestsByCreator() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getMergedPullRequestsByCreator()));
    }

    private Map<String, Long> getMergedPullRequestsByOthers() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getMergedPullRequestsByOthers()));
    }

    private  Map<String, Long> getMergedPullRequestsOfOthers() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getMergedPullRequestsOfOthers()));
    }

    private Map<String, Long> getComplexPR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getNoOfComplexPRs()));
    }

    private Map<String, List<Integer>> getNoOfCommentsOfCreatedPR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getNoOfCommentsOfCreatedPR()));
    }

    private Map<String, List<Integer>> getNoOfChangedFiles() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getNoOfChangedFiles()));
    }

    private Map<String, List<Integer>> getNoOfCommits() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getNoOfCommits()));
    }

    private Map<String, Long> getNoOfCommentsOnCreatedPR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getCommentsOnCreatedPR()));
    }

    private Map<String, Long> getNoOfCommentsOnOtherPR() {
        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getCommentsOnOtherPR()));
    }

    private Map<String, Long> getClosedWithoutMergePR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getClosedWithoutMergePR()));
    }

    private Map<String, Long> getLongTimeToMergePR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getLongMergeTimePR()));
    }

    private Map<String, Long> getOpenForALongTimePR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getLongTimePR()));
    }

    private Map<String, List<Long>> getReactionTimesOnPR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getReactionTime(pullRequestRegistry.getPullRequests())));
    }

    private Map<String, List<Long>> getMergeTimesOfCreatedPR() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getMergeTimeOfCreatedPR()));

    }

    private Map<String, List<Double>> getResponseTimesOnComments() {

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getResponseTimeOnComments()));
    }

    private Map<String, List<DeveloperActivity>> createDevelopersTimeline(){

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getTimeline()));
    }

    private Map<String, Map<String, Integer>> calculateInteractions() {

        List<String> developers = new ArrayList<>(developerRegistry.getDevelopers().keySet());
        int size= developers.size();
        int[][] interactions = new int[size][size];

        pullRequestRegistry.getPullRequests().stream()
                .forEach(x -> x.calculateInteractions(developers, interactions));

        return developerRegistry.getDevelopers().entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> getInteractions(x.getValue(), developers, interactions)));
    }

    private Map<String, Integer> getInteractions(Developer dev, List<String> developers, int[][] interactions) {

        Map<String, Integer> developerInteractionsWithOthers = new HashMap<>();
        int position = developers.indexOf(dev.getUsername());

        for (int i = 0; i < developers.size(); i++) {
            developerInteractionsWithOthers.put(developers.get(i), interactions[position][i]);
            if (developers.get(i).equals(dev.getUsername()))
                developerInteractionsWithOthers.put(developers.get(i), -1);
        }
        return developerInteractionsWithOthers;
    }

}
