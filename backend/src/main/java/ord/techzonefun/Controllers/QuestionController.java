package ord.techzonefun.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ord.techzonefun.Entities.Question;
import ord.techzonefun.Services.QuestionService;
import lombok.extern.slf4j.Slf4j; // 1. Import Slf4j
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/import")
    public ResponseEntity<List<Question>> importQuestions(@RequestParam("file") MultipartFile file) {
        log.info("Received import request for file: {}", file.getOriginalFilename()); //  Log thông tin file
        try {
            List<Question> createdQuestions = questionService.importQuestions(file);
            log.info("Imported {} questions successfully.", createdQuestions.size()); // Log số lượng import thành công
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestions);
        } catch (Exception e) {
            log.error("Error importing questions from file: {}", file.getOriginalFilename(), e); // Log tên file và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody Question question) {
        log.info("Received request to create question: {}", question); // Log thông tin question
        try {
            Question createdQuestion = questionService.createQuestion(question);
            log.info("Question created successfully. Question ID: {}", createdQuestion.getId()); // Log ID của question mới
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
        } catch (Exception e) {
            log.error("Error creating question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        log.info("Received request to get all questions");
        try {
            List<Question> questions = questionService.getAllQuestions();
            log.info("Returning {} questions", questions.size()); // Log số lượng question
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting all questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
        log.info("Received request to get question with ID: {}", id);
        try {
            Question question = questionService.getQuestionById(id);
            log.info("Returning question with ID: {}", id); // Log ID (đã có trong request, nhưng log lại cho chắc)
            return ResponseEntity.ok(question);
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {}", id); // Log warning
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting question with ID: {}", id, e); // Log ID và stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        log.info("Received request to delete question with ID: {}", id);
        try {
            questionService.deleteQuestion(id); // Thêm phương thức này trong QuestionService
            log.info("Question with ID: {} deleted successfully", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NoSuchElementException e) {
            log.warn("Question not found with ID: {} for deletion", id);
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception e) {
            log.error("Error deleting question with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

}