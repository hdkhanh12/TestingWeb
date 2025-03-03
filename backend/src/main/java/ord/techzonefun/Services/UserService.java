package ord.techzonefun.Services;

import lombok.extern.slf4j.Slf4j;
import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Entities.User;
import ord.techzonefun.Repositories.CustomerRepository;
import ord.techzonefun.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    public User registerUser(String username, String password, String phoneNumber) {
        log.info("Registering user with username: {}, phoneNumber: {}", username, phoneNumber);

        // 1. Kiểm tra username đã tồn tại
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("Username already exists: {}", username);
            throw new IllegalArgumentException("Tài khoản đã tồn tại.");
        }

        // 2. Validate phoneNumber
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            log.error("Invalid phone number: {}", phoneNumber);
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số");
        }

        // 3. Kiểm tra phoneNumber đã được sử dụng
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            log.error("Phone number already in use: {}", phoneNumber);
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng. Vui lòng nhập số khác.");
        }

        // 4. Tạo User
        String userId = UUID.randomUUID().toString();
        log.debug("Generated User ID: {}", userId);

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList("CUSTOMER"));
        user.setPhoneNumber(phoneNumber);

        User savedUser = userRepository.save(user);
        log.debug("Saved User ID: {}", savedUser.getId());

        // 5. Tạo Customer liên kết với User
        Customer customer = new Customer();
        customer.setId(userId);
        customer.setUserId(userId);
        customer.setName(username);
        customer.setPhoneNumber(phoneNumber);
        customerRepository.save(customer);
        log.info("Customer created with ID: {}, PhoneNumber: {}", customer.getId(), customer.getPhoneNumber());

        log.info("Successfully registered user with ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> loginUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    User authenticatedUser = new User();
                    authenticatedUser.setId(user.getId());            // Set ID từ DB
                    authenticatedUser.setUsername(user.getUsername());
                    authenticatedUser.setPassword(user.getPassword());
                    authenticatedUser.setRoles(user.getRoles());      // Set role từ DB
                    return authenticatedUser;
                });
    }


}
