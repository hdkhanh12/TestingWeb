package ord.techzonefun.Repositories;


import ord.techzonefun.Entities.TestSuite;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestSuiteRepository extends ElasticsearchRepository<TestSuite, String> {
}