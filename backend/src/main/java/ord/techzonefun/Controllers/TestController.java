package ord.techzonefun.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import ord.techzonefun.Entities.Test;
import ord.techzonefun.Entities.TestDto;
import ord.techzonefun.Entities.TestResult;
import ord.techzonefun.Services.TestService;
import ord.techzonefun.payload.request.SubmitTestRequest;
import ord.techzonefun.payload.request.TestResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j; // 1. Import Slf4j

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j // 2. Thêm annotation @Slf4j
public class TestController {

    // private static final Logger logger = LoggerFactory.getLogger(TestController.class); // Không cần dòng này nữa

    @Autowired
    private TestService testService;

    @PostMapping("/createWithPercentage")
    public ResponseEntity<Test> createTestWithPercentage(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam int multichoicePercentage,
            @RequestParam int oralPercentage,
            @RequestParam int scalePercentage) {
        log.info("Received request to create test with percentage: id={}, name={}, description={}, multichoicePercentage={}, oralPercentage={}, scalePercentage={}",
                id, name, description, multichoicePercentage, oralPercentage, scalePercentage);
        try {
            Test test = testService.createTestWithPercentage(id, name, description, multichoicePercentage, oralPercentage, scalePercentage);
            log.info("Successfully created test with id: {}", test.getId());
            return new ResponseEntity<>(test, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating test with percentage", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable String id) {
        log.info("Received request to get test with id: {}", id);
        try {
            Test test = testService.getTest(id);
            log.info("Found test with id: {}", id);
            return new ResponseEntity<>(test, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Test not found with id: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<Test> addQuestionToTest(
            @PathVariable String testId,
            @PathVariable String questionId
    ) {
        log.info("Received request to add question {} to test {}", questionId, testId);
        try {
            Test test = testService.addQuestionToTest(testId, questionId);
            log.info("Successfully added question {} to test {}", questionId, testId);
            return new ResponseEntity<>(test, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Test or question not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error adding question", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<Test> deleteQuestionFromTest(
            @PathVariable String testId,
            @PathVariable String questionId
    ) {
        log.info("Received request to delete question {} from test {}", questionId, testId);
        try {
            Test test = testService.deleteQuestionFromTest(testId, questionId);
            log.info("Successfully deleted question {} from test {}", questionId, testId);
            return new ResponseEntity<>(test, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Test or question not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting question", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Test> updateTest(@PathVariable String id, @RequestBody TestDto testDto) {
        log.info("Received request to update test with id: {}", id);
        try {
            Test updatedTest = testService.updateTest(id, testDto);
            log.info("Successfully updated test with id: {}", id);
            return ResponseEntity.ok(updatedTest);
        } catch (NoSuchElementException e) {
            log.warn("Test not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating test with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{testId}/submit")
    public ResponseEntity<?> submitTest(@PathVariable String testId, @RequestBody SubmitTestRequest submitRequest, HttpServletRequest request) {
        log.info("Received request to submit test with id: {}", testId);
        String customerId = submitRequest.getCustomerId();

        if (customerId == null || customerId.trim().isEmpty()) {
            log.warn("Customer ID is missing in submit request for test: {}", testId);
            return ResponseEntity.status(400).body("Bad Request: Customer ID is required.");
        }
        try {
            TestResultResponse result = testService.submitTest(submitRequest, customerId);
            log.info("Test submitted successfully for testId: {}, customerId: {}, score: {}", testId, customerId, result.getScore()); // Log thông tin quan trọng
            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            log.error("Error submitting test with id: {}, customerId: {}", testId, customerId, e); // Log cả testId và customerId, và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }



    }

    @GetMapping("/results")
    public ResponseEntity<?> getTestResults(
            @RequestParam(required = false) String testId,
            HttpServletRequest request) {

        log.info("Received request to get test results. testId: {}", testId);

        // Kiểm tra quyền (TestDev) hiển chưa được dùng tới
        String role = (String) request.getSession().getAttribute("role");
        // log.debug("Role from session: {}", role); // Log role, ở mức DEBUG.  Nên dùng DEBUG cho thông tin nhạy cảm.

        /*
        // if (role == null) {
        if (role == null || !role.equals("testdev")) { // Bỏ comment để bật lại kiểm tra quyền
            log.warn("Unauthorized access to test results. Role: {}", role);
            return ResponseEntity.status(403).body("Forbidden: Access denied.");
        }

         */

        List<TestResult> results;
        try {
            if (testId != null) {
                results = testService.getResultsByTestId(testId);
                log.info("Returning {} test results for testId: {}", results.size(), testId);
            } else {
                results = testService.getAllTestResults();
                log.info("Returning {} test results", results.size());
            }

            return ResponseEntity.ok(results);
        }
        catch (Exception e){
            log.error("Error when get all test result", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}