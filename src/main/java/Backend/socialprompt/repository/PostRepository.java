package Backend.socialprompt.repository;

import Backend.socialprompt.model.Post;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Post> findByIsDraftTrueAndUserIdOrderByCreatedAtDesc(String userId);
    List<Post> findByCategoryAndUserId(String category, String userId);
    List<Post> findByCaptionContainingIgnoreCaseOrPromptContainingIgnoreCase(String caption, String prompt, Sort sort);

}
