package ord.techzonefun.Controllers;

import jakarta.validation.Valid;
import ord.techzonefun.Entities.Role;
import ord.techzonefun.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/roles")
@Slf4j
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        log.info("Received request to create role: {}", role); //  Log request body
        try {
            Role createdRole = roleRepository.save(role);
            log.info("Role created successfully. Role ID: {}", createdRole.getId()); //  Log ID của role mới
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (Exception e) {
            log.error("Error creating role", e); //  Log lỗi, bao gồm stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Received request to get all roles"); //  Log
        try {
            List<Role> roles = (List<Role>) roleRepository.findAll();
            log.info("Returning {} roles", roles.size()); //  Log số lượng role
            return ResponseEntity.ok(roles);
        } catch (Exception e){
            log.error("Error getting roles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable String id) {
        log.info("Received request to get role with ID: {}", id); //  Log ID
        Optional<Role> optionalRole = roleRepository.findById(id);
        return optionalRole.map(role -> {
            log.info("Returning role: {}", role); //  Log thông tin role (nếu tìm thấy)
            return ResponseEntity.ok(role);
        }).orElseGet(() -> {
            log.warn("Role not found with ID: {}", id); //  Log warning (nếu không tìm thấy)
            return ResponseEntity.notFound().build();
        });
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable String id, @Valid @RequestBody Role role) {
        log.info("Received request to update role with ID: {}.  New role data: {}", id, role); //  Log ID và data
        if (!roleRepository.existsById(id)) {
            log.warn("Role not found with ID: {} for update", id); //  Log warning (nếu không tìm thấy)
            return ResponseEntity.notFound().build();
        }
        try {
            Role updatedRole = roleRepository.save(role);
            log.info("Role with ID: {} updated successfully", id); //  Log thành công
            return ResponseEntity.ok(updatedRole);
        }
        catch (Exception e){
            log.error("Error update role with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        log.info("Received request to delete role with ID: {}", id); //  Log ID
        if (!roleRepository.existsById(id)) {
            log.warn("Role not found with ID: {} for deletion", id); // Log warning (nếu không tìm thấy)
            return ResponseEntity.notFound().build();
        }
        try{
            roleRepository.deleteById(id);
            log.info("Role with ID: {} deleted successfully", id); // Log thành công
            return ResponseEntity.noContent().build();
        }
        catch (Exception e){
            log.error("Error when delete role with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}