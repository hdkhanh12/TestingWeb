package ord.techzonefun.Controllers;

import jakarta.validation.Valid;
import ord.techzonefun.Entities.TestSuite;
import ord.techzonefun.Services.TestSuiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j; // 1. Import

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/testsuites")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class TestSuiteController {
    // Hiện chưa được dùng tới vì theo logic hiện tại thì testsuite chỉ là nơi lưu trữ các test. chỉ có duy nhất
    @Autowired
    private TestSuiteService testSuiteService;

    @PostMapping
    public ResponseEntity<TestSuite> createTestSuite(@Valid @RequestBody TestSuite testSuite) {
        log.info("Received request to create test suite: {}", testSuite); //  Log request body
        try {
            TestSuite createdTestSuite = testSuiteService.createTestSuite(testSuite);
            log.info("Test suite created successfully. TestSuite ID: {}", createdTestSuite.getId()); //  Log ID
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTestSuite);
        } catch (Exception e) {
            log.error("Error creating test suite", e); //  Log lỗi và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<TestSuite>> getAllTestSuites() {
        log.info("Received request to get all test suites"); //  Log
        try {
            List<TestSuite> testSuites = testSuiteService.getAllTestSuites();
            log.info("Returning {} test suites", testSuites.size()); //  Log số lượng
            return ResponseEntity.ok(testSuites);
        } catch (Exception e) {
            log.error("Error getting all test suites", e); // 5. Log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestSuite> getTestSuiteById(@PathVariable String id) {
        log.info("Received request to get test suite with ID: {}", id); // Log ID
        try {
            TestSuite testSuite = testSuiteService.getTestSuiteById(id);
            log.info("Returning test suite with ID: {}", id); //  Log ID
            return ResponseEntity.ok(testSuite);
        } catch (NoSuchElementException e) {
            log.warn("Test suite not found with ID: {}", id); //  Log warning
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting test suite with ID: {}", id, e); // Log ID và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestSuite> updateTestSuite(@PathVariable String id, @RequestBody TestSuite testSuite) {
        log.info("Received request to update test suite with ID: {}. New data: {}", id, testSuite); //  Log ID và data
        try {
            TestSuite updatedTestSuite = testSuiteService.updateTestSuite(id, testSuite);
            log.info("Test suite with ID: {} updated successfully", id); //  Log thành công
            return new ResponseEntity<>(updatedTestSuite, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Test suite not found with ID: {} for update", id); //  Log warning
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating test suite with ID: {}", id, e); // Log ID và stack trace
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestSuite(@PathVariable String id) {
        log.info("Received request to delete test suite with ID: {}", id); //  Log ID
        try {
            testSuiteService.deleteTestSuite(id);
            log.info("Test suite with ID: {} deleted successfully", id); //  Log thành công
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Test suite not found with ID: {} for deletion", id); //  Log warning
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting test suite with ID: {}", id, e); // Log ID và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}