package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.Test;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends ElasticsearchRepository<Test, String> {
}