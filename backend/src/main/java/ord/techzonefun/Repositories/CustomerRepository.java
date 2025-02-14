package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.Customer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {
}