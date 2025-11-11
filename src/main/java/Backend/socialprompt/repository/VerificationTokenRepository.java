package Backend.socialprompt.repository;

import Backend.socialprompt.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    Optional<VerificationToken> findById(String id);
    void deleteByUserId(String userId);
}
