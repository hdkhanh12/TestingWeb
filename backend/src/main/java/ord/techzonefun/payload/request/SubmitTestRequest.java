package ord.techzonefun.payload.request;

import java.util.List;

public class SubmitTestRequest {
    private String testId; // ID của bài test
    private String customerId;
    private List<Answer> answers; // Danh sách câu trả lời

    // Getters and setters
    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
    public String getCustomerId(){
        return this.customerId;
    }
    public void setCustomerId(String customerId){
        this.customerId = customerId;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    // Inner class Answer (giữ nguyên như bạn đã định nghĩa ở frontend)
    public static class Answer {
        private String questionId;
        private String selectedOption;

        // Getters and setters
        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getSelectedOption() {
            return selectedOption;
        }

        public void setSelectedOption(String selectedOption) {
            this.selectedOption = selectedOption;
        }
    }
}