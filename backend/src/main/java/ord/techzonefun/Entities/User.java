package ord.techzonefun.Entities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "Username cannot be blank")
    @Field(type = FieldType.Keyword, name = "username")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Field(type = FieldType.Keyword, name = "password")
    private String password; // Sẽ lưu mật khẩu đã mã hóa

    @Field(type = FieldType.Keyword, name = "roles")
    private List<String> roles;

    @Field(type = FieldType.Text)
    private String phoneNumber; // Thêm số điện thoại

    public User(String username, String password, List<String> roles, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.phoneNumber = phoneNumber;
    }
/*
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @NotBlank(message = "Password cannot be blank") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password cannot be blank") String password) {
        this.password = password;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public @NotBlank(message = "Username cannot be blank") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username cannot be blank") String username) {
        this.username = username;
    }

 */
}
