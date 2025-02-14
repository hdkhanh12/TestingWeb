package ord.techzonefun.Repositories;


import ord.techzonefun.Entities.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {
    Optional<User> findByUsername(String username);
}