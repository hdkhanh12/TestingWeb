package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.TestResult;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TestResultRepository extends ElasticsearchRepository<TestResult, String> {
    List<TestResult> findAll();
    List<TestResult> findByTestId(String testId);
}