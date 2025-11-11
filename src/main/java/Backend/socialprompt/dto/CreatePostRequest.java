package Backend.socialprompt.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
    private String imageUrl;
    private String caption;
    private String prompt;
    private List<String> tags; // usernames or userIds
    private String audience; // public|followers|private
    private String category;
    private boolean shareExternal; // optional

}
