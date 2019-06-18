package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository <ProjectData, Long> {

    ProjectData findByProjectId(Long id);
    ProjectData findByRepository(String repositoryName);
}
