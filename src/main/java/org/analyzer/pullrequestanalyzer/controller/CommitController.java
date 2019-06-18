package org.analyzer.pullrequestanalyzer.controller;

import org.analyzer.pullrequestanalyzer.dto.CommitDTO;
import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/commits")
public class CommitController {

    private DTORegistry dtoRegistry;

    @Autowired
    public CommitController(DTORegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
    }

    @GetMapping("")
    public List<CommitDTO> getAllCommits() {
        return dtoRegistry.getCommitDTOS();
    }
}
