package ord.techzonefun.Entities;

import lombok.Data;

import java.util.List;

@Data
public class TestDto {
    private String id;
    private String name;
    private String description;
    private Integer time;
    private List<Question> questions;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}