package ord.techzonefun.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ord.techzonefun.Entities.Question;
import ord.techzonefun.Services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Nhập file câu hỏi với gói
    @PostMapping("/import/package")
    public ResponseEntity<Map<String, Object>> importQuestionsWithPackage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("packageName") String packageName) {
        log.info("Received import request for file: {} with package: {}", file.getOriginalFilename(), packageName);
        try {
            List<Question> createdQuestions = questionService.importQuestionsWithPackage(file, packageName);
            log.info("Imported {} questions successfully with package: {}", createdQuestions.size(), packageName);

            Map<String, Object> response = new HashMap<>();
            response.put("questions", createdQuestions);
            response.put("packageName", packageName); // Trả về packageName
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error importing questions from file: {} with package: {}", file.getOriginalFilename(), packageName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/package/name/{packageName}")
    public ResponseEntity<List<Question>> getQuestionsByPackageName(@PathVariable String packageName) {
        log.info("Received request to get questions with packageName: {}", packageName);
        try {
            List<Question> questions = questionService.getQuestionsByPackageName(packageName);
            log.info("Returning {} questions for packageName: {}", questions.size(), packageName);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting questions with packageName: {}", packageName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /*
    // Nhập file câu hỏi không gói (giữ nguyên)
    @PostMapping("/import")
    public ResponseEntity<List<Question>> importQuestions(@RequestParam("file") MultipartFile file) {
        log.info("Received import request for file: {}", file.getOriginalFilename());
        try {
            List<Question> createdQuestions = questionService.importQuestions(file);
            log.info("Imported {} questions successfully.", createdQuestions.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestions);
        } catch (Exception e) {
            log.error("Error importing questions from file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
     */

    // Tạo câu hỏi đơn lẻ (giữ nguyên)
    @PostMapping("/create")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody Question question) {
        log.info("Received request to create question: {}", question);
        try {
            Question createdQuestion = questionService.createQuestion(question);
            log.info("Question created successfully. Question ID: {}", createdQuestion.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
        } catch (Exception e) {
            log.error("Error creating question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy tất cả câu hỏi (giữ nguyên)
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        log.info("Received request to get all questions");
        try {
            List<Question> questions = questionService.getAllQuestions();
            log.info("Returning {} questions", questions.size());
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting all questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy câu hỏi theo gói
    @GetMapping("/package/{packageId}")
    public ResponseEntity<List<Question>> getQuestionsByPackage(@PathVariable String packageId) {
        log.info("Received request to get questions with packageId: {}", packageId);
        try {
            List<Question> questions = questionService.getQuestionsByPackage(packageId);
            log.info("Returning {} questions for packageId: {}", questions.size(), packageId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting questions with packageId: {}", packageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy câu hỏi theo ID (giữ nguyên)
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
        log.info("Received request to get question with ID: {}", id);
        try {
            Question question = questionService.getQuestionById(id);
            log.info("Returning question with ID: {}", id);
            return ResponseEntity.ok(question);
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting question with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa câu hỏi đơn lẻ (giữ nguyên)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        log.info("Received request to delete question with ID: {}", id);
        try {
            questionService.deleteQuestion(id);
            log.info("Question with ID: {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {} for deletion", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting question with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa tất cả câu hỏi trong một gói
    @DeleteMapping("/package/{packageId}")
    public ResponseEntity<Void> deleteQuestionsByPackage(@PathVariable String packageId) {
        log.info("Received request to delete questions with packageId: {}", packageId);
        try {
            questionService.deleteQuestionsByPackage(packageId);
            log.info("All questions with packageId: {} deleted successfully", packageId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("No questions found with packageId: {} for deletion", packageId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting questions with packageId: {}", packageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Trong QuestionController.java
    @DeleteMapping("/no-package")
    public ResponseEntity<Void> deleteQuestionsWithoutPackage(HttpServletRequest request, Authentication authentication) {
        log.info("Received request to delete questions without package");
        try {
            questionService.deleteQuestionsWithoutPackage();
            log.info("All questions without package deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("No questions found without package for deletion");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting questions without package", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}