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

    public @NotBlank(message = "Username cannot be blank") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username cannot be blank") String username) {
        this.username = username;
    }

    @NotBlank(message = "Password cannot be blank")
    @Field(type = FieldType.Keyword, name = "password")
    private String password;

    public @NotBlank(message = "Password cannot be blank") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password cannot be blank") String password) {
        this.password = password;
    }

    @Field(type = FieldType.Keyword, name = "roles")
    private List<String> roles;

}