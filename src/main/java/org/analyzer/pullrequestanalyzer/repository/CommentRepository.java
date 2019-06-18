package org.analyzer.pullrequestanalyzer.repository;

import org.analyzer.pullrequestanalyzer.domain.CommentData;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<CommentData, Long> {
}
