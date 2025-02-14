package ord.techzonefun.payload.request;

public class TestResultResponse {
    private int score; // Điểm số
    private int totalQuestions; // Tổng số câu hỏi

    // Constructor
    public TestResultResponse(int score, int totalQuestions) {
        this.score = score;
        this.totalQuestions = totalQuestions;
    }

    // Getters (setters không cần thiết cho response)
    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }
}