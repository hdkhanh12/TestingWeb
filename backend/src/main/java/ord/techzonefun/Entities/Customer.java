package ord.techzonefun.Entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "customers")
public class Customer {
    @Id
    @Field(type = FieldType.Keyword)
    private String id; // Elasticsearch tự sinh ID

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Keyword, name = "userId")
    private String userId; // Chỉ lưu userId, không dùng làm ID

    @Field(type = FieldType.Text)
    private String phoneNumber; // Thêm số điện thoại

    @Field(type = FieldType.Integer)
    private int testAttempts = 10; // Mặc định 10 lần làm bài

    public Customer(String name, String userId, String phoneNumber) {
        this.name = name;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
    }

    public Customer() {

    }
}


