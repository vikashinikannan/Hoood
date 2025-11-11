package Backend.socialprompt.repository;

import Backend.socialprompt.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository  extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findById(String id);
    void deleteByUserId(String userId);
}
