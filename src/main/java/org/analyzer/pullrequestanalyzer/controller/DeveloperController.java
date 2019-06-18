package org.analyzer.pullrequestanalyzer.controller;

import lombok.Data;
import org.analyzer.pullrequestanalyzer.dto.DeveloperAnalysisDTO;
import org.analyzer.pullrequestanalyzer.dto.DeveloperDTO;
import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    private DTORegistry dtoRegistry;

    @Autowired
    public DeveloperController (DTORegistry DTORegistry) {
        this.dtoRegistry = DTORegistry;
    }

    @GetMapping("")
    public List<DeveloperDTO> getAllDevelopers() {

        return dtoRegistry.getDeveloperDTOS();
    }

    @GetMapping("/all_prs")
    public void allPRs(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("group,val1,val2,val3");

        developerAnalysisDTOS.stream()
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getOpenedPR).reversed())
                .filter(x -> x.getOpenedPR() > 0)
                .forEach(x -> printAllPRs(writer, x));
    }

    @GetMapping("/opened_prs")
    public void openedPRs(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("developer,value");

        developerAnalysisDTOS.stream()
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getOpenedPR).reversed())
                .limit(10)
                .filter(x -> x.getOpenedPR() > 0)
                .forEach(x -> printNameAndValue(writer, x.getDeveloperId(), x.getOpenedPR()));
    }

    @GetMapping("/merged_prs")
    public void mergedPRs(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("developer,value");

        developerAnalysisDTOS.stream()
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getMergedPROfOthers).reversed())
                .limit(10)
                .filter(x -> (x.getMergedPROfOthers() + x.getMergedPRByCreator()) > 0 )
                .forEach(x -> printNameAndValue(writer, x.getDeveloperId(), x.getMergedPROfOthers()));
    }

    @GetMapping("/declined_prs")
    public void declinedPRs(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("developer,value");

        developerAnalysisDTOS.stream()
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getClosedWithoutMergePR).reversed())
                .limit(10)
                .filter(x -> x.getClosedWithoutMergePR() > 0)
                .forEach(x -> printNameAndValue(writer, x.getDeveloperId(), x.getClosedWithoutMergePR()));
    }

    private void printAllPRs(PrintWriter writer, DeveloperAnalysisDTO developerAnalysisDTO) {

        DeveloperDTO developerDTO = dtoRegistry.getDeveloperById(developerAnalysisDTO.getDeveloperId());
        if (developerDTO.getName() == null)
            writer.println(developerDTO.getUsername() + "," + developerAnalysisDTO.getOpenedPR() + "," + developerAnalysisDTO.getMergedPROfOthers() + "," + developerAnalysisDTO.getClosedWithoutMergePR());
        else
            writer.println(developerDTO.getName() + "," + developerAnalysisDTO.getOpenedPR() + "," + developerAnalysisDTO.getMergedPROfOthers() + "," + developerAnalysisDTO.getClosedWithoutMergePR());

    }

    private void printNameAndValue(PrintWriter writer, long id, double value) {

        DeveloperDTO developerDTO = dtoRegistry.getDeveloperById(id);
        if (developerDTO.getName() == null)
            writer.println(developerDTO.getUsername() + "," + value);
        else
            writer.println(developerDTO.getName() + "," + value);
    }

    @GetMapping("/comments_commits")
    public void commentsAndCommits(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();

        developerAnalysisDTOS.stream()
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getCommentsOnOtherPR).thenComparing(DeveloperAnalysisDTO::getCommentsOnCreatedPR).reversed())
                .limit(5)
                .filter(x -> x.getCommentsOnOtherPR() > 0)
                .forEach(x -> printDeveloperDetails(writer, x));
    }

    private void printDeveloperDetails(PrintWriter writer, DeveloperAnalysisDTO developerAnalysisDTO) {

        DeveloperDTO developerDTO = dtoRegistry.getDeveloperById(developerAnalysisDTO.getDeveloperId());
        int filesChanged = developerAnalysisDTO.getNoOfChangedFiles().stream().mapToInt(Integer::intValue).sum();

        if (developerDTO.getName() == null)
            writer.println(developerDTO.getUsername() + "," + developerDTO.getComments().size() + "," + developerDTO.getCommits().size() + "," + filesChanged);
        else
            writer.println(developerDTO.getName() + "," + developerDTO.getComments().size() + "," + developerDTO.getCommits().size() + "," + filesChanged);
    }

    @GetMapping("/reactiontime")
    public void reactionTimes(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();

        writer.println("dev,a,b");

        developerAnalysisDTOS.stream()
                .filter(x -> x.getReactionTimesOnPR().size() >= 10)
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getMeanReactionTimeOnPR))
                .filter(x -> x.getMeanReactionTimeOnPR() > 0)
                .forEach(x -> printDeveloperTimes(writer, x, x.getReactionTimesOnPR().size(), x.getMeanReactionTimeOnPR()));
    }

    @GetMapping("/responsetime")
    public void responseTimes(HttpServletResponse response) throws IOException {

        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        response.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = response.getWriter();

        writer.println("dev,a,b");

        developerAnalysisDTOS.stream()
                .filter(x -> x.getCommentsOnCreatedPR() >= 10)
                .sorted(Comparator.comparing(DeveloperAnalysisDTO::getMeanResponseTimesOnComments))
                .filter(x -> x.getMeanResponseTimesOnComments() > 0)
                .forEach(x -> printDeveloperTimes(writer, x, x.getCommentsOnCreatedPR(), x.getMeanResponseTimesOnComments() * 60));
    }

    private void printDeveloperTimes(PrintWriter writer, DeveloperAnalysisDTO developerAnalysisDTO, long a, double b) {

        DeveloperDTO developerDTO = dtoRegistry.getDeveloperById(developerAnalysisDTO.getDeveloperId());
        if (developerDTO.getName() == null)
            writer.println(developerDTO.getUsername() + "," + a + "," + b);
        else
            writer.println(developerDTO.getName() + "," + a + "," + b);
    }

    @GetMapping("/interactions")
    public List<DeveloperInteraction> interactions() {

        List<DeveloperInteraction> interactions = new ArrayList<>();
        List<DeveloperAnalysisDTO> developerAnalysisDTOS = dtoRegistry.getDeveloperAnalysisDTOS();

        for (DeveloperAnalysisDTO developerAnalysisDTO : developerAnalysisDTOS) {
            DeveloperInteraction currentInteraction = new DeveloperInteraction();
            DeveloperDTO  developerDTO = dtoRegistry.getDeveloperById(developerAnalysisDTO.getDeveloperId());
            if (developerDTO.getName() == null)
                currentInteraction.setName(developerDTO.getUsername());
            else
                currentInteraction.setName(developerDTO.getName());
            Map<String, Integer>  interactionsMap = developerAnalysisDTO.getInteractions();
            List<String> developers = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : interactionsMap.entrySet()) {
                if (entry.getValue() > 0) {
                    DeveloperDTO currentDeveloper = dtoRegistry.getDeveloperByUsername(entry.getKey());
                    if (currentDeveloper.getName() == null)
                        developers.add(entry.getKey());
                    else
                        developers.add(currentDeveloper.getName());
                }
            }
            currentInteraction.setSize(developers.size());
            currentInteraction.setImports(developers);
            interactions.add(currentInteraction);
        }
        return interactions;
    }
}

@Data
class DeveloperInteraction {
    String name;
    int size;
    List<String> imports;
}