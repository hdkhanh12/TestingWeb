package ord.techzonefun.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Entities.Test;
import ord.techzonefun.Entities.TestResult;
import ord.techzonefun.Repositories.RoleRepository;
import ord.techzonefun.Repositories.TestRepository;
import ord.techzonefun.Services.CustomerService;
import ord.techzonefun.Services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TestService testService;

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/me")
    public ResponseEntity<Customer> getCurrentUserCustomer(@RequestParam String userId) {
        log.info("Getting customer for logged-in user with ID: {}", userId);
        try {
            Customer customer = customerService.getCustomerByUserId(userId);
            return ResponseEntity.ok(customer);
        } catch (NoSuchElementException e) {
            log.warn("Customer not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @PathVariable String id,
            HttpServletRequest request,
            Authentication authentication) {
        String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "No session";
        String cookies = request.getHeader("Cookie") != null ? request.getHeader("Cookie") : "No cookies";
        log.info("Received request to get customer with ID: {}", id);
        log.info("Request Session ID: {}", sessionId);
        log.info("Cookies received: {}", cookies);
        log.info("User: {}, Roles: {}",
                authentication != null ? authentication.getName() : "anonymous",
                authentication != null ? authentication.getAuthorities() : "none");

        try {
            Customer customer = customerService.getCustomerById(id);
            log.info("Customer found. Returning customer: {}", customer);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Customer not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error retrieving customer with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tests/{customerId}")
    public ResponseEntity<List<Test>> getCustomerTests(@PathVariable String customerId) {
        log.info("Fetching tests for customer: {}", customerId);
        try {
            List<Test> tests = testService.getTestsForCustomer(customerId);
            if (tests.isEmpty()) {
                log.info("No tests found for customer: {}", customerId);
            }
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            log.error("Error fetching tests for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tests/detail/{testId}")
    public ResponseEntity<Test> getTestDetail(@PathVariable String testId) {
        log.info("Fetching test detail for testId: {}", testId);
        try {
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new NoSuchElementException("Test not found with id: " + testId));
            return ResponseEntity.ok(test);
        } catch (NoSuchElementException e) {
            log.error("Test not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error fetching test detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{customerId}/recharge")
    public ResponseEntity<Map<String, String>> rechargeTestAttempts(
            @PathVariable String customerId,
            @RequestBody Map<String, Integer> requestBody) {
        log.info("Received recharge request for customer: {}", customerId);
        try {
            int amount = requestBody.getOrDefault("amount", 0);
            if (amount <= 0) {
                log.warn("Invalid recharge amount: {}", amount);
                return ResponseEntity.badRequest().body(Map.of("message", "Số lần nạp phải lớn hơn 0"));
            }
            customerService.rechargeTestAttempts(customerId, amount);
            return ResponseEntity.ok(Map.of("message", "Nạp " + amount + " lần làm bài thành công"));
        } catch (NoSuchElementException e) {
            log.warn("Customer not found: {}", customerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy khách hàng"));
        } catch (Exception e) {
            log.error("Error recharging test attempts for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi khi nạp lần làm bài"));
        }
    }

    @GetMapping("/{customerId}/results")
    public ResponseEntity<List<TestResult>> getCustomerTestResults(@PathVariable String customerId) {
        log.info("Fetching test results for customer: {}", customerId);
        try {
            List<TestResult> results = testService.getResultsByCustomerId(customerId);
            if (results.isEmpty()) {
                log.info("No test results found for customer: {}", customerId);
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error fetching test results for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}