package ord.techzonefun.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ord.techzonefun.Entities.*;
import ord.techzonefun.Repositories.CustomerRepository;
import ord.techzonefun.Repositories.QuestionRepository;
import ord.techzonefun.Repositories.TestRepository;
import ord.techzonefun.Repositories.TestResultRepository;
import ord.techzonefun.payload.request.SubmitTestRequest;
import ord.techzonefun.payload.request.TestResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class TestService {


    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Transactional
    public Test addQuestionToTest(String testId, String questionId) {
        log.info("Adding question {} to test {}", questionId, testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.warn("Test not found with id: {}", testId);
                    return new NoSuchElementException("Test not found with id: " + testId);
                });

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.warn("Question not found with id: {}", questionId);
                    return new NoSuchElementException("Question not found with id: " + questionId);
                });

        List<Question> questions = test.getQuestions();
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (questions.stream().anyMatch(q -> q.getId().equals(questionId))) {
            // Nếu câu hỏi đã tồn tại, không thêm vào nữa
            log.warn("Question with id {} already exists in test with id {}", questionId, testId);
            return test;
        }
        questions.add(question);
        test.setQuestions(questions);

        try {
            Test savedTest = testRepository.save(test);
            log.info("Successfully added question {} to test {}. Updated test: {}", questionId, testId, savedTest);
            return savedTest;
        } catch (Exception e) {
            log.error("Error adding question {} to test {}", questionId, testId, e);
            throw e; // Re-throw
        }
    }

    public Test deleteQuestionFromTest(String testId, String questionId) {
        log.info("Deleting question {} from test {}", questionId, testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.warn("Test not found with id: {}", testId);
                    return new NoSuchElementException("Test not found with id: " + testId);
                });

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.warn("Question not found with id: {}", questionId);
                    return new NoSuchElementException("Question not found with id: " + questionId);
                });

        List<Question> questions = test.getQuestions();
        if (questions == null || questions.isEmpty()) {
            log.warn("Test {} has no questions to delete", testId);
            throw new NoSuchElementException("Test doesn't have any question to delete");
        }
        log.info("Questions before deletion: {}", questions);
        boolean removed = questions.removeIf(q -> q.getId().equals(question.getId())); // Dùng removeIf, và so sánh ID
        if (!removed) {
            log.warn("Question {} not found in test {}", questionId, testId); // Log nếu không tìm thấy question để xóa
            // Không throw exception ở đây, vì có thể question không nằm trong test này
        }
        log.info("Questions after deletion: {}", questions);
        test.setQuestions(questions);

        try {
            Test updatedTest = testRepository.save(test);
            log.info("Successfully deleted question {} from test {}. Updated test: {}", questionId, testId, updatedTest);
            return updatedTest;
        } catch (Exception e) {
            log.error("Error deleting question {} from test {}", questionId, testId, e);
            throw e; // Re-throw
        }
    }



    public Test createTestWithPercentage(String id, String name, String description, int multichoicePercentage, int oralPercentage, int scalePercentage, String packageId) {
        log.info("Creating test with id: {}, name: {}, description: {}, multichoicePercentage: {}, oralPercentage: {}, scalePercentage: {}, packageId: {}",
                id, name, description, multichoicePercentage, oralPercentage, scalePercentage, packageId);

        Test test;

        if (testRepository.existsById(id)) {
            test = testRepository.findById(id).get();
            log.debug("Found existing test with id: {}", id);
        } else {
            test = new Test();
            test.setId(id);
            log.debug("Creating new test with id: {}", id);
        }

        // Lấy câu hỏi theo packageId nếu có, nếu không thì lấy tất cả
        List<Question> availableQuestions = packageId != null && !packageId.isEmpty()
                ? questionRepository.findByPackageId(packageId)
                : StreamSupport.stream(questionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        if (availableQuestions.isEmpty()) {
            log.warn("No questions found for packageId: {}", packageId);
            throw new IllegalArgumentException("No questions available for the selected package");
        }

        // Lọc câu hỏi theo loại
        List<Question> allMultichoiceQuestions = availableQuestions.stream()
                .filter(q -> "multichoice".equals(q.getType()))
                .collect(Collectors.toList());
        List<Question> allOralQuestions = availableQuestions.stream()
                .filter(q -> "oral".equals(q.getType()))
                .collect(Collectors.toList());
        List<Question> allScaleQuestions = availableQuestions.stream()
                .filter(q -> "scale".equals(q.getType()))
                .collect(Collectors.toList());

        List<Question> selectedQuestions = new ArrayList<>();

        int totalQuestions = 20;
        int multichoiceCount = (totalQuestions * multichoicePercentage) / 100;
        int oralCount = (totalQuestions * oralPercentage) / 100;
        int scaleCount = (totalQuestions * scalePercentage) / 100;

        log.debug("Selecting questions: multichoiceCount={}, oralCount={}, scaleCount={}", multichoiceCount, oralCount, scaleCount);

        // Chọn ngẫu nhiên câu hỏi từ danh sách đã lọc
        if (allMultichoiceQuestions.size() > 0) {
            selectedQuestions.addAll(getRandomQuestions(allMultichoiceQuestions, multichoiceCount));
        }
        if (allOralQuestions.size() > 0) {
            selectedQuestions.addAll(getRandomQuestions(allOralQuestions, oralCount));
        }
        if (allScaleQuestions.size() > 0) {
            selectedQuestions.addAll(getRandomQuestions(allScaleQuestions, scaleCount));
        }

        test.setName(name);
        //test.setDescription(description);
        test.setQuestions(selectedQuestions);
        //test.setMultichoicePercentage(multichoicePercentage);
        //test.setOralPercentage(oralPercentage);
        //test.setScalePercentage(scalePercentage);

        Test savedTest = testRepository.save(test);
        log.info("Successfully created/updated test with id: {}", savedTest.getId());
        return savedTest;
    }
    public Test getTest(String id) {
        log.info("Getting test with id: {}", id);
        return testRepository.findById(id).orElseThrow(() -> {
            log.warn("Test not found with id: {}", id);
            return new NoSuchElementException("Test not found with id: " + id);
        });
    }


    public List<Test> getPublicTest() {
        log.info("Getting public test for customer");
        // Giả sử bạn có một trường "isPublic" trong TestSuite để lọc các đề thi công khai
        return testRepository.findByIsPublicTrue();
    }

    private List<Question> getRandomQuestions(List<Question> list, int count) {
        int actualCount = Math.min(count, list.size());
        List<Question> randomQuestions = new ArrayList<>();

        for (int i = 0; i < actualCount; i++) {
            int randomIndex = (int) (Math.random() * list.size());
            randomQuestions.add(list.remove(randomIndex));
        }
        return randomQuestions;
    }



    public Test updateTest(String id, TestDto testDto) {
        log.info("Updating test with id: {}, data: {}", id, testDto);
        Test test = testRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Test not found with id: {}", id);
                    return new NoSuchElementException("Test not found with id: " + id);
                });

        test.setName(testDto.getName());
        test.setTime(testDto.getTime());
        test.setQuestions(testDto.getQuestions()); // Set questions từ DTO

        try {
            Test updatedTest = testRepository.save(test);
            log.info("Successfully updated test: {}", updatedTest);
            return updatedTest;
        }
        catch (Exception e){
            log.error("Error when update test", e);
            throw e;
        }
    }

    public Customer getCustomer (String id){
        log.info("find customer with id: {}", id);
        try{
            return customerRepository.findById(id).orElseThrow(() -> {
                log.warn("Customer not found with ID: {}" ,id);
                return new NoSuchElementException("customer not found with id: " + id);
            });
        }
        catch (NoSuchElementException e){
            log.warn("Customer not found with ID: {}", id);
            throw e; // Re-throw the exception
        }
        catch (Exception e){
            log.error("Error when get customer with id: " + id, e);
            throw e;
        }
    }

    @Transactional
    public TestResultResponse submitTest(SubmitTestRequest submitRequest, String customerId) {
        log.info("Submitting test for customerId: {}, testId: {}", customerId, submitRequest.getTestId());

        Test test = testRepository.findById(submitRequest.getTestId())
                .orElseThrow(() -> {
                    log.warn("Test not found with id: {}", submitRequest.getTestId());
                    return new NoSuchElementException("Test not found");
                });

        List<Question> questions = test.getQuestions();
        if (questions == null) {
            log.warn("Test with id {} has no questions", submitRequest.getTestId());
            questions = new ArrayList<>(); // Để tránh NullPointerException
        }

        Map<String, String> userAnswers = submitRequest.getAnswers().stream()
                .collect(Collectors.toMap(
                        SubmitTestRequest.Answer::getQuestionId,
                        SubmitTestRequest.Answer::getSelectedOption,
                        (existing, replacement) -> {
                            log.warn("Duplicate answer for questionId: {}. Using first answer.", existing);
                            return existing;
                        }
                ));

        int correctCount = 0;
        for (Question question : questions) {
            String correctAnswer = question.getAnswer();
            String userAnswer = userAnswers.get(question.getId());
            if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                correctCount++;
            }
        }

        TestResult testResult = new TestResult();
        testResult.setId(UUID.randomUUID().toString());
        testResult.setTestId(test.getId());
        testResult.setCustomerId(customerId);
        testResult.setScore(correctCount);
        testResult.setTotalQuestions(questions.size());

        try {
            testResultRepository.save(testResult);
            log.info("Test result saved successfully for customerId: {}, testId: {}, score: {}", customerId, test.getId(), correctCount);
            return new TestResultResponse(correctCount, questions.size());
        } catch (Exception e) {
            log.error("Error when save test result", e);
            throw e;
        }
    }
    public List<TestResult> getAllTestResults() {
        log.info("Retrieving all test results");
        try {
            List<TestResult> testResults = testResultRepository.findAll();
            List<TestResult> results = new ArrayList<>();
            for (TestResult testResult : testResults) {
                Customer customer = customerRepository.findById(testResult.getCustomerId()).orElse(null);
                if (customer != null) {
                    testResult.setCustomerName(customer.getName());
                }
                results.add(testResult);

            }
            log.info("Returning {} test results", results.size());
            return results;
        }
        catch (Exception e){
            log.error("Error when get all test result", e);
            throw e;
        }
    }

    public List<TestResult> getResultsByTestId(String testId) {
        log.info("Retrieving test results for testId: {}", testId);
        try {
            List<TestResult> testResults =  testResultRepository.findByTestId(testId);
            List<TestResult> results = new ArrayList<>(); // Tạo danh sách trung gian
            for (TestResult testResult : testResults) {
                Customer customer = customerRepository.findById(testResult.getCustomerId()).orElse(null);
                if (customer != null) {
                    testResult.setCustomerName(customer.getName());
                }
                results.add(testResult); // Thêm vào danh sách trung gian
            }
            log.info("Returning {} test results for testId: {}", results.size(), testId);
            return results; // Trả về danh sách trung gian
        }
        catch (Exception e){
            log.error("Error when get test result by test id", e);
            throw e;
        }

    }
    public void deleteTestResult(String resultId) {
        log.info("Deleting test result with ID: {}", resultId);
        try {
            // Kiểm tra xem document có tồn tại không
            if (!testResultRepository.existsById(resultId)) {
                log.warn("Test result with ID: {} not found", resultId);
                throw new IllegalArgumentException("Test result not found");
            }
            // Xóa document từ Elasticsearch
            testResultRepository.deleteById(resultId);
            log.info("Successfully deleted test result with ID: {}", resultId);
        } catch (Exception e) {
            log.error("Error deleting test result with ID: {}", resultId, e);
            throw e;
        }
    }

    public List<Test> getTestsForCustomer(String customerId) {
        log.info("Fetching tests for customer: {}", customerId);

        // Lấy bài thi gốc từ TESTDEV (customerId = null)
        List<Test> originalTests = testRepository.findByCustomerIdIsNull();

        if (originalTests.isEmpty()) {
            log.warn("No original tests found in database");
            return Collections.emptyList();
        }

        log.info("Returning {} original tests for customer: {}", originalTests.size(), customerId);
        return originalTests;
    }

    public List<TestResult> getResultsByCustomerId(String customerId) {
        log.info("Fetching test results for customer: {}", customerId);
        return testResultRepository.findByCustomerId(customerId);
    }
}