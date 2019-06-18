package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.DeveloperData;
import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeveloperRepository extends CrudRepository<DeveloperData, Long> {

    DeveloperData findByDeveloperId(Long id);

    List<DeveloperData> findAllByProject(ProjectData project);
}

