package ord.techzonefun.Entities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "roles")
public class Role {
    @Id
    private String id;

    @NotBlank(message = "Role name cannot be blank")
    @Field(type = FieldType.Keyword, name = "name")
    private String name;
}