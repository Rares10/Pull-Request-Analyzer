package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.CommitData;
import org.springframework.data.repository.CrudRepository;

public interface CommitRepository extends CrudRepository<CommitData, Long> {

}
