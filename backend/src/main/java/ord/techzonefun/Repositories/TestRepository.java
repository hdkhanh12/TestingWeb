package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.Test;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends ElasticsearchRepository<Test, String> {
    // Phương thức truy vấn tùy chỉnh để lấy các Test công khai
    List<Test> findByIsPublicTrue();
    List<Test> findByCustomerId(String customerId);
    // Lấy bài thi gốc từ TESTDEV (customerId = null)
    List<Test> findByCustomerIdIsNull();
}