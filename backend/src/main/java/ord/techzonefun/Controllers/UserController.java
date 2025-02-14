package ord.techzonefun.Controllers;

import jakarta.validation.Valid;
import ord.techzonefun.Entities.User;
import ord.techzonefun.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Received request to create user: {}", user); //  Log request body
        try {
            User createdUser = userRepository.save(user);
            log.info("User created successfully. User ID: {}", createdUser.getId()); //  Log ID
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            log.error("Error creating user", e); //  Log lỗi và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Received request to get all users"); //  Log
        try {
            List<User> users = (List<User>) userRepository.findAll();
            log.info("Returning {} users", users.size()); //  Log số lượng
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users", e); //  Log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        log.info("Received request to get user with ID: {}", id); //  Log ID
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(user -> {
            log.info("Returning user: {}", user); //  Log thông tin user
            return ResponseEntity.ok(user);
        }).orElseGet(() -> {
            log.warn("User not found with ID: {}", id); //  Log warning
            return ResponseEntity.notFound().build();
        });
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @Valid @RequestBody User user) {
        log.info("Received request to update user with ID: {}. New data: {}", id, user); //  Log ID và data
        if (!userRepository.existsById(id)) {
            log.warn("User not found with ID: {} for update", id); // Log warning
            return ResponseEntity.notFound().build();
        }
        try{
            User updatedUser = userRepository.save(user);
            log.info("User with ID: {} updated successfully", id); //  Log thành công
            return ResponseEntity.ok(updatedUser);
        }
        catch (Exception e){
            log.error("Error update user with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Received request to delete user with ID: {}", id); //  Log ID
        if (!userRepository.existsById(id)) {
            log.warn("User not found with ID: {} for deletion", id); //  Log warning
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(id);
            log.info("User with ID: {} deleted successfully", id); //  Log thành công
            return ResponseEntity.noContent().build();
        }
        catch (Exception e){
            log.error("Error when delete user with id: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}