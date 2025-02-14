package ord.techzonefun.Controllers;

import jakarta.validation.Valid;
import ord.techzonefun.Entities.User;
import ord.techzonefun.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserRepository userRepository;

    // Hiện đang không sử dụng tới phân quyền
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if(optionalUser.isPresent()) {
            User userLogin = optionalUser.get();
            if(user.getPassword().equals(userLogin.getPassword())) {
                return ResponseEntity.ok("Login success");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong username");
    }
}
