package Backend.socialprompt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "verification_tokens")
public class VerificationToken {
    @Id
    private String id; // token string
    private String userId;
    private Instant expiryDate; // set to now + 45 seconds
}
