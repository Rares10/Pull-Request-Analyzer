package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.FileData;
import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<FileData, Long> {
}
