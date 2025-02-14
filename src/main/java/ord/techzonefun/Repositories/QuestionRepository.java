package ord.techzonefun.Repositories;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ord.techzonefun.Entities.Question;
import java.util.List;

public interface QuestionRepository extends ElasticsearchRepository<Question, String> {
    List<Question> findByType(String type);
    @Query("{\"terms\": {\"testIds\": [\"?0\"]}}")
    List<Question> findQuestionByTestId(String testId);


}