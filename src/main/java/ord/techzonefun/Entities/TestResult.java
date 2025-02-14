package ord.techzonefun.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
// Không cần import DocumentReference

@Document(indexName = "test_results")
public class TestResult {

    @Id
    private String id;

    @Field(type = FieldType.Keyword, name = "testId") // Lưu ID của Test
    private String testId;

    @Field(type = FieldType.Keyword, name = "customerId") // Lưu ID của Customer
    private String customerId;

    @Field(type = FieldType.Text, name = "customerName") // Thêm trường này
    private String customerName;

    @Field(type = FieldType.Integer, name = "score")
    private int score;

    @Field(type = FieldType.Integer, name = "totalQuestions")
    private int totalQuestions;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}