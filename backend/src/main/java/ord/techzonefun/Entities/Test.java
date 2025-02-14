package ord.techzonefun.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "tests")
public class Test {
    @Id
    private String id;

    @NotBlank(message = "Test name cannot be blank")
    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Date, name = "startDate")
    private LocalDateTime endDate;

    @Field(type = FieldType.Keyword, name = "status")
    private String status;

    @Field(type = FieldType.Integer, name = "time")
    private Integer time;

    @Field(type = FieldType.Object, name="questions")
    private List<Question> questions;

    // Constructor không tham số
    public Test() {}

    // Constructor đầy đủ tham số
    public Test(String id, String name, LocalDateTime endDate, String status, Integer time, List<Question> questions) {
        this.id = id;
        this.name = name;
        this.endDate = endDate;
        this.status = status;
        this.time = time;
        this.questions = questions;
    }

    // Getter và Setter cho từng thuộc tính
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
