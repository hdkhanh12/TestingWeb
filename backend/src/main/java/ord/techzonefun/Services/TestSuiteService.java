package ord.techzonefun.Services;

import ord.techzonefun.Entities.TestSuite;
import ord.techzonefun.Repositories.TestSuiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j; // 1. Import

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@Slf4j
public class TestSuiteService {
    @Autowired
    private TestSuiteRepository testSuiteRepository;

    public TestSuite createTestSuite(TestSuite testSuite) {
        log.info("Creating test suite: {}", testSuite); // 3. Log thông tin testSuite
        try {
            TestSuite createdTestSuite = testSuiteRepository.save(testSuite);
            log.info("Test suite created successfully. TestSuite ID: {}", createdTestSuite.getId()); // 4. Log ID
            return createdTestSuite;
        } catch (Exception e) {
            log.error("Error creating test suite", e); // 5. Log lỗi và stack trace
            throw e; // Re-throw
        }
    }

    public List<TestSuite> getAllTestSuites() {
        log.info("Retrieving all test suites"); // 3. Log
        try {
            Iterable<TestSuite> iterable = testSuiteRepository.findAll();
            List<TestSuite> testSuites = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
            log.info("Returning {} test suites", testSuites.size()); // 4. Log số lượng
            // Không cần log từng test suite ở đây nữa, vì có thể có rất nhiều
            return testSuites;
        }
        catch (Exception e){
            log.error("Error when get all test suites",e);
            throw e;
        }
    }

    public TestSuite getTestSuiteById(String id) {
        log.info("Retrieving test suite with ID: {}", id); // 3. Log ID
        try {
            return testSuiteRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Test suite not found with ID: {}", id); // 4. Log warning
                        return new NoSuchElementException("Test suite not found with id: " + id);
                    });
        }
        catch (NoSuchElementException e){
            log.warn("Test suite not found with ID: {}", id);
            throw  e;
        }
        catch (Exception e){
            log.error("Error when get test suite by id: " + id, e);
            throw e;
        }

    }

    public TestSuite getTestSuiteWithQuestionById(String id) {
        log.info("Getting test suite with question by id: {}", id); // 3. Log ID
        try {
            TestSuite testSuite = testSuiteRepository.findById(id).orElseThrow(() -> {
                log.warn("Test suite not found with id: {}", id); //4. Log warn
                return new NoSuchElementException("Not found id " + id);
            });
            log.info("Found test suite with id: {}" ,id); //sửa lại log info
            return testSuite;
        } catch (NoSuchElementException e) {
            log.warn("Test suite not found with id: {}", id); // 5. Log warning và re-throw
            throw e;
        } catch (Exception e) {
            log.error("Error getting test suite with question by id: {}", id, e); // Log ID và stack trace
            throw e; // Re-throw
        }
    }

    public TestSuite updateTestSuite(String id, TestSuite testSuite) {
        log.info("Updating test suite with id: {}, data: {}", id, testSuite); // 3. Log ID và data
        if (!testSuiteRepository.existsById(id)) {
            log.warn("Test suite not found with id: {} for update", id); // 4. Log warning
            throw new NoSuchElementException("Test suite not found with id: " + id);
        }
        testSuite.setId(id);
        try {
            TestSuite updatedTestSuite = testSuiteRepository.save(testSuite);
            log.info("Test suite with ID: {} updated successfully", id); // 5. Log thành công
            return updatedTestSuite;
        }
        catch (Exception e){
            log.error("Error when update test suite with id: "+ id, e);
            throw e;
        }
    }

    public void deleteTestSuite(String id) {
        log.info("Deleting test suite with ID: {}", id); // 3. Log ID
        try {
            if (!testSuiteRepository.existsById(id)) {
                log.warn("Test suite not found with ID: {} for deletion", id); // 4. Log warning
                throw new NoSuchElementException("Test suite not found with id: " + id);
            }
            testSuiteRepository.deleteById(id);
            log.info("Test suite with ID: {} deleted successfully", id); // 5. Log thành công
        }
        catch (NoSuchElementException e){
            log.warn("Test suite not found with ID: {} for deletion", id);
            throw e;
        }
        catch (Exception e) {
            log.error("Error deleting test suite with ID: {}", id, e); // Log ID và stack trace
            throw e; // Re-throw
        }
    }

}