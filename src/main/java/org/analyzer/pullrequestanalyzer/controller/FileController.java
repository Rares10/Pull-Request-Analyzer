package org.analyzer.pullrequestanalyzer.controller;

import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.analyzer.pullrequestanalyzer.dto.FileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private DTORegistry dtoRegistry;

    @Autowired
    public FileController(DTORegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
    }

    @GetMapping("")
    public List<FileDTO> getAllFiles() {
        return dtoRegistry.getFileDTOS();
    }
}
