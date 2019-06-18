package org.analyzer.pullrequestanalyzer.logic.loader;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.analyzer.pullrequestanalyzer.repository.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.egit.github.core.client.RequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

@Service
@NoArgsConstructor
@Data
public abstract class DataLoader {

    protected String username;
    protected String password;

    protected String owner;
    protected String repository;

    protected DeveloperRepository developerRepository;
    protected PullRequestRepository pullRequestRepository;
    protected CommentRepository commentRepository;
    protected CommitRepository commitRepository;
    protected FileRepository fileRepository;
    protected ProjectRepository projectRepository;

    protected RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public DataLoader (DeveloperRepository developerRepository,
                       PullRequestRepository pullRequestRepository,
                       CommentRepository commentRepository,
                       CommitRepository commitRepository,
                       FileRepository fileRepository,
                       ProjectRepository projectRepository) {
        this.developerRepository = developerRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.commentRepository = commentRepository;
        this.commitRepository = commitRepository;
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
    }

    //Used for http requests
    HttpHeaders createHeaders (String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    @Async
    public abstract void loadData() throws IOException, ParseException, InterruptedException;
}
