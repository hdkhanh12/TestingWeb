package ord.techzonefun.Controllers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Entities.CustomerInfoDTO;
import ord.techzonefun.Entities.User;
import ord.techzonefun.Repositories.CustomerRepository;
import ord.techzonefun.Repositories.UserRepository;
import ord.techzonefun.Services.CustomerService;
import ord.techzonefun.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")

public class AuthenticationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Mã hóa mật khẩu

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;


    private static final String SECRET_KEY = "hdktn123"; // Nên đưa vào application.

    private static final String TESTDEV_USERNAME = "testdev"; // Tên người dùng duy nhất
    private static final String TESTDEV_PASSWORD = "password";

    // Đăng nhập Testdev
    @PostMapping("/testdev/login")
    public ResponseEntity<?> loginTestdev(HttpServletRequest request, @RequestBody User user) {

        // Kiểm tra xem người dùng có nhập đúng username và password không
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Tên người dùng và mật khẩu không được để trống."));
        }

        if (TESTDEV_USERNAME.equals(user.getUsername()) && TESTDEV_PASSWORD.equals(user.getPassword())) {
            // Tạo đối tượng Authentication với vai trò 'ROLE_TESTDEV'
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(TESTDEV_USERNAME, null, AuthorityUtils.createAuthorityList("ROLE_TESTDEV"));

            // Đặt thông tin xác thực vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ Lưu SecurityContext vào session
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 🔥 Debug xem session có lưu đúng không
            System.out.println("Session ID: " + session.getId());
            System.out.println("User Roles: " + authentication.getAuthorities());

            // Trả về phản hồi thành công
            return ResponseEntity.ok(Collections.singletonMap("message", "Đăng nhập thành công. Chuyển hướng tới trang /testdev."));
        } else {
            // Trả về lỗi nếu tài khoản hoặc mật khẩu không đúng
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Sai tài khoản hoặc mật khẩu."));
        }
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. Kiểm tra username đã tồn tại
            if (userService.loginUser(user.getUsername(), user.getPassword()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tài khoản đã tồn tại"));
            }

            // 2. Tạo User với phoneNumber
            User savedUser = userService.registerUser(user.getUsername(), user.getPassword(), user.getPhoneNumber());
            log.info("User created: ID={}, Username={}, PhoneNumber={}", savedUser.getId(), savedUser.getUsername(), savedUser.getPhoneNumber());

            // 3. Tạo Customer tương ứng
            Customer customer = new Customer();
            customer.setId(UUID.randomUUID().toString());
            customer.setName(user.getUsername());
            customer.setUserId(savedUser.getId());
            customer.setPhoneNumber(savedUser.getPhoneNumber()); // Lưu phoneNumber
            customerRepository.save(customer);
            log.info("Customer created: ID={}, Name={}, PhoneNumber={}", customer.getId(), customer.getName(), customer.getPhoneNumber());

            // 4. Thiết lập xác thực và session
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    savedUser.getUsername(), null, AuthorityUtils.createAuthorityList("ROLE_CUSTOMER")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 5. Trả về kết quả với JSESSIONID trong cookie
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Để false nếu dùng localhost
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                    "message", "Đăng ký thành công",
                    "userId", savedUser.getId(),
                    "customerId", customer.getId(),
                    "phoneNumber", savedUser.getPhoneNumber()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký người dùng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống, vui lòng thử lại sau"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        Optional<User> optionalUser = userService.loginUser(user.getUsername(), user.getPassword());
        if (optionalUser.isPresent()) {
            User userLogin = optionalUser.get();
            if ("CUSTOMER".equals(userLogin.getRoles().get(0))) {
                try {
                    Customer customer = customerRepository.findByUserId(userLogin.getId())
                            .orElseThrow(() -> new NoSuchElementException("Customer not found with userId: " + userLogin.getId()));

                    // Tạo Authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userLogin, null,
                            AuthorityUtils.createAuthorityList(userLogin.getRoles().stream()
                                    .map(role -> "ROLE_" + role.toUpperCase())
                                    .toArray(String[]::new)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Lưu SecurityContext vào session
                    HttpSession session = request.getSession(true); // Tạo hoặc lấy session
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    log.info("SecurityContext saved to session ID: {}", session.getId());

                    // Tạo cookie JSESSIONID
                    Cookie cookie = new Cookie("JSESSIONID", session.getId());
                    cookie.setHttpOnly(true);
                    cookie.setSecure(false);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("message", "Đăng nhập thành công.");
                    responseBody.put("customerId", customer.getId());
                    responseBody.put("username", userLogin.getUsername());
                    return ResponseEntity.ok(responseBody);
                } catch (Exception e) {
                    log.warn("Không tìm thấy customer với userId: {}", userLogin.getId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thông tin khách hàng.");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu.");
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ROLE_TESTDEV')")
    public ResponseEntity<List<CustomerInfoDTO>> getAllCustomers(HttpServletRequest request) {
        log.info("Received GET /api/customers request from session: {}", request.getSession().getId());
        try {
            List<CustomerInfoDTO> customers = customerService.getAllCustomersWithUserInfo();
            log.info("Returning {} customers", customers.size());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error fetching customers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/customers/{customerId}")
    @PreAuthorize("hasRole('ROLE_TESTDEV')")
    public ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable String customerId) {
        log.info("Received DELETE /api/testdev/customers/{} request", customerId);
        try {
            customerService.deleteCustomer(customerId);
            return ResponseEntity.ok(Map.of("message", "Xóa khách hàng thành công"));
        } catch (NoSuchElementException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi hệ thống, vui lòng thử lại sau"));
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate(); // Xóa session
        SecurityContextHolder.clearContext(); // Xóa context authentication
        return ResponseEntity.ok(Collections.singletonMap("message", "Đã đăng xuất"));
    }

    /*
    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return ResponseEntity.ok(csrfToken);
    }

     */

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            csrfToken = csrfTokenRepository.generateToken(request);
            log.info("Generated new CSRF token: {}", csrfToken.getToken());
        } else {
            log.info("Retrieved existing CSRF token: {}", csrfToken.getToken());
        }

        // Lưu token vào session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("_csrf", csrfToken);
            log.info("CSRF token saved to session ID: {}", session.getId());
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", csrfToken.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers/{customerId}/reset-password")
    @PreAuthorize("hasRole('ROLE_TESTDEV')")
    public ResponseEntity<Map<String, String>> resetCustomerPassword(@PathVariable String customerId) {
        log.info("Received POST /api/testdev/customers/{}/reset-password request", customerId);
        try {
            String tempPassword = customerService.resetPassword(customerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đặt lại mật khẩu thành công",
                    "tempPassword", tempPassword
            ));
        } catch (NoSuchElementException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi hệ thống, vui lòng thử lại sau"));
        }
    }

    @Autowired
    private CsrfTokenRepository csrfTokenRepository; // Đảm bảo inject repository
}
