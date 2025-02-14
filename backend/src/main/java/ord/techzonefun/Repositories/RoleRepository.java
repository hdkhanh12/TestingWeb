package ord.techzonefun.Repositories;

import ord.techzonefun.Entities.Role;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends ElasticsearchRepository<Role, String> {
    Optional<Role> findByName(String name);
}