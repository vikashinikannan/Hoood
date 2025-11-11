package Backend.socialprompt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    private String userId;        // owner
    private String username;      // convenience
    private String avatar;        // owner avatar url

    private String imageUrl;      // main image
    private String caption;
    private String prompt;        // prompt text if any
    private List<String> tags = new ArrayList<>(); // mentioned userIds or usernames

    private String audience;      // public | followers | private
    private String category;      // prompts | images | etc.

    private Set<String> likes = new HashSet<>();   // userIds who liked
    private int commentsCount;
    private boolean isDraft = false;
    private boolean isSaved = false;
    private boolean isFavorite = false;

    private Instant createdAt;
    private Instant updatedAt;
}
