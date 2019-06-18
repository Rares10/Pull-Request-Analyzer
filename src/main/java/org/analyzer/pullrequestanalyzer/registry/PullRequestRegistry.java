package org.analyzer.pullrequestanalyzer.registry;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;


@Data
@Service
public class PullRequestRegistry {
    private List<PullRequest> pullRequests;

    public PullRequest getPullRequestById (Long id) {
        return pullRequests.stream()
                .filter(x -> x.getPullRequestId().equals(id))
                .findAny()
                .orElse(null);
    }
}
