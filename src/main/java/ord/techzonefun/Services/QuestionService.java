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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ObjectMapper objectMapper;


    public List<Question> importQuestions(MultipartFile file) throws IOException {
        log.info("Importing questions from file: {}", file.getOriginalFilename()); // Log tên file
        try {
            InputStream inputStream = file.getInputStream();
            List<Question> questions = objectMapper.readValue(inputStream, new TypeReference<List<Question>>() {});
            log.info("Read {} questions from file", questions.size()); // Log số lượng question đọc được
            Iterable<Question> savedQuestions = questionRepository.saveAll(questions);
            List<Question> savedQuestionsList =  StreamSupport.stream(savedQuestions.spliterator(), false).collect(Collectors.toList());
            log.info("Saved {} questions to database", savedQuestionsList.size()); // Log số lượng question lưu được
            return savedQuestionsList;
        } catch (Exception e) {
            log.error("Error importing questions from file: {}", file.getOriginalFilename(), e); // Log tên file và stack trace
            throw e; // Re-throw
        }
    }

    public Question createQuestion(Question question) {
        log.info("Creating question: {}", question); //  Log question data
        try{
            Question createdQuestion = questionRepository.save(question);
            log.info("Question created successfully. Question ID: {}", createdQuestion.getId()); //  Log ID
            return createdQuestion;
        } catch (Exception e){
            log.error("Error when create question", e);
            throw e;
        }
    }

    public List<Question> getAllQuestions() {
        log.info("Retrieving all questions"); //  Log
        try {
            Iterable<Question> iterable = questionRepository.findAll();
            List<Question> questions = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
            log.info("Returning {} questions", questions.size()); //  Log số lượng
            return questions;
        }
        catch (Exception e){
            log.error("Error when get all question", e);
            throw e;
        }
    }

    public Question getQuestionById(String id) {
        log.info("Retrieving question with ID: {}", id); //  Log ID
        try {
            return questionRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Question not found with ID: {}", id); //  Log warning
                        return new NoSuchElementException("Question not found with id: " + id);
                    });
        }
        catch (NoSuchElementException e){
            log.warn("Question not found with ID: {}", id); //  Log warning
            throw e;
        }
        catch (Exception e) {
            log.error("Error retrieving question with ID: {}" ,id, e);
            throw e; // Re-throw
        }

    }

    public void deleteQuestion(String id) {
        log.info("Deleting question with ID: {}", id);
        try {
            questionRepository.deleteById(id);
        }
        catch (NoSuchElementException e){
            log.warn("Question not found with ID: {} for deletion", id); // 4. Log warning
            throw e;
        }
        catch (Exception e){
            log.error("Error when delete question with id: " + id, e);
            throw e;
        }
    }

}