package org.analyzer.pullrequestanalyzer.controller;

import org.analyzer.pullrequestanalyzer.dto.CommentDTO;
import org.analyzer.pullrequestanalyzer.dto.DTORegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private DTORegistry dtoRegistry;

    @Autowired
    public CommentController(DTORegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
    }

    @GetMapping("")
    public List<CommentDTO> getAllComments() {
        return dtoRegistry.getCommentDTOS();
    }
}
