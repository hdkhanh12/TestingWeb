package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.Customer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.Optional;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {
    Optional<Customer> findByUserId(String userId);
}
