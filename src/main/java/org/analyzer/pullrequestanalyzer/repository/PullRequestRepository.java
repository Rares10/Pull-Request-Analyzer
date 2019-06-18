package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.analyzer.pullrequestanalyzer.domain.PullRequestData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PullRequestRepository extends CrudRepository<PullRequestData, Long>{

    PullRequestData findByPullRequestId(Long id);
    List<PullRequestData> findAllByProject(ProjectData projectData);
}
