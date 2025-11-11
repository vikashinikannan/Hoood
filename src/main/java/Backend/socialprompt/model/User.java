package Backend.socialprompt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String passwordHash; // bcrypt
    private boolean enabled = false; // becomes true after email verification
    private String provider; // "local" or "google"
    private Set<String> roles;
    private Instant lastVerifiedAt;

}
