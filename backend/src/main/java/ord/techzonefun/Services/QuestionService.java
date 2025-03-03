package ord.techzonefun.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ord.techzonefun.Entities.Question;
import ord.techzonefun.Repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Nhập file câu hỏi và gán vào một gói
    public List<Question> importQuestionsWithPackage(MultipartFile file, String packageName) throws IOException {
        log.info("Importing questions from file: {} with package: {}", file.getOriginalFilename(), packageName);
        try {
            String packageId = UUID.randomUUID().toString();
            InputStream inputStream = file.getInputStream();
            List<Question> questions = objectMapper.readValue(inputStream, new TypeReference<List<Question>>() {});
            log.info("Read {} questions from file", questions.size());

            questions.forEach(question -> {
                question.setPackageId(packageId);
                question.setPackageName(packageName); // Lưu packageName
            });
            Iterable<Question> savedQuestions = questionRepository.saveAll(questions);
            List<Question> savedQuestionsList = StreamSupport.stream(savedQuestions.spliterator(), false)
                    .collect(Collectors.toList());
            log.info("Saved {} questions to database with packageId: {} and packageName: {}", savedQuestionsList.size(), packageId, packageName);
            return savedQuestionsList;
        } catch (Exception e) {
            log.error("Error importing questions from file: {} with package: {}", file.getOriginalFilename(), packageName, e);
            throw e;
        }
    }

    public List<Question> getQuestionsByPackageName(String packageName) {
        log.info("Retrieving questions for packageName: {}", packageName);
        try {
            List<Question> questions = questionRepository.findByPackageName(packageName);
            log.info("Returning {} questions for packageName: {}", questions.size(), packageName);
            return questions;
        } catch (Exception e) {
            log.error("Error retrieving questions for packageName: {}", packageName, e);
            throw e;
        }
    }

    // Tạo câu hỏi đơn lẻ (giữ nguyên)
    public Question createQuestion(Question question) {
        log.info("Creating question: {}", question);
        try {
            Question createdQuestion = questionRepository.save(question);
            log.info("Question created successfully. Question ID: {}", createdQuestion.getId());
            return createdQuestion;
        } catch (Exception e) {
            log.error("Error when create question", e);
            throw e;
        }
    }

    // Lấy tất cả câu hỏi (giữ nguyên)
    public List<Question> getAllQuestions() {
        log.info("Retrieving all questions");
        try {
            Iterable<Question> iterable = questionRepository.findAll();
            List<Question> questions = StreamSupport.stream(iterable.spliterator(), false)
                    .collect(Collectors.toList());
            log.info("Returning {} questions", questions.size());
            return questions;
        } catch (Exception e) {
            log.error("Error when get all question", e);
            throw e;
        }
    }

    // Lấy câu hỏi theo gói
    public List<Question> getQuestionsByPackage(String packageId) {
        log.info("Retrieving questions for packageId: {}", packageId);
        try {
            List<Question> questions = questionRepository.findByPackageId(packageId);
            log.info("Returning {} questions for packageId: {}", questions.size(), packageId);
            return questions;
        } catch (Exception e) {
            log.error("Error retrieving questions for packageId: {}", packageId, e);
            throw e;
        }
    }

    // Lấy câu hỏi theo ID (giữ nguyên)
    public Question getQuestionById(String id) {
        log.info("Retrieving question with ID: {}", id);
        try {
            return questionRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Question not found with ID: {}", id);
                        return new NoSuchElementException("Question not found with id: " + id);
                    });
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving question with ID: {}", id, e);
            throw e;
        }
    }

    // Xóa câu hỏi đơn lẻ (giữ nguyên)
    public void deleteQuestion(String id) {
        log.info("Deleting question with ID: {}", id);
        try {
            questionRepository.deleteById(id);
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {} for deletion", id);
            throw e;
        } catch (Exception e) {
            log.error("Error when delete question with id: {}", id, e);
            throw e;
        }
    }

    // Xóa tất cả câu hỏi trong một gói
    public void deleteQuestionsByPackage(String packageId) {
        log.info("Deleting all questions with packageId: {}", packageId);
        try {
            List<Question> questions = questionRepository.findByPackageId(packageId);
            if (questions.isEmpty()) {
                log.warn("No questions found for packageId: {}", packageId);
                throw new NoSuchElementException("No questions found for packageId: " + packageId);
            }
            questionRepository.deleteAll(questions);
            log.info("Successfully deleted {} questions with packageId: {}", questions.size(), packageId);
        } catch (Exception e) {
            log.error("Error deleting questions with packageId: {}", packageId, e);
            throw e;
        }
    }

    public void deleteQuestionsWithoutPackage() {
        log.info("Deleting all questions without packageId");
        try {
            // Ép kiểu Iterable thành Stream
            List<Question> questions = StreamSupport.stream(questionRepository.findAll().spliterator(), false)
                    .filter(q -> q.getPackageId() == null || q.getPackageId().isEmpty())
                    .collect(Collectors.toList());
            if (questions.isEmpty()) {
                log.warn("No questions found without packageId");
                throw new NoSuchElementException("No questions found without packageId");
            }
            questionRepository.deleteAll(questions);
            log.info("Successfully deleted {} questions without packageId", questions.size());
        } catch (Exception e) {
            log.error("Error deleting questions without packageId", e);
            throw e;
        }
    }
}