package Backend.socialprompt.controllers;

import Backend.socialprompt.dto.CreatePostRequest;
import Backend.socialprompt.dto.LikeRequest;
import Backend.socialprompt.jwt.JwtProvider;
import Backend.socialprompt.model.Post;
import Backend.socialprompt.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final JwtProvider jwtProvider;

    private String extractUserId(String authHeader, String fallbackUserId) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                return jwtProvider.getUserId(token);
            }
        }
        return fallbackUserId;
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<?> feed(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        List<Post> feed = postService.getFeed(userId, page, size);
        return ResponseEntity.ok(feed);
    }

    @PostMapping("/posts/create")
    public ResponseEntity<?> createPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestBody CreatePostRequest req
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        String username = "unknown";
        Post created = postService.createPost(userId, username, req);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/posts/draft")
    public ResponseEntity<?> saveDraft(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestBody CreatePostRequest req
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        Post draft = postService.saveDraft(userId, req);
        return ResponseEntity.ok(draft);
    }

    @PostMapping("/posts/like")
    public ResponseEntity<?> likePost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LikeRequest req,
            @RequestParam(value = "userId", required = false) String userIdParam
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        Post p = postService.toggleLike(userId, req.getPostId());
        return ResponseEntity.ok(Map.of("success", true, "newLikeCount", p.getLikes().size()));
    }

    @GetMapping({"/user/{userId}/posts", "/user/posts"})
    public ResponseEntity<?> myPosts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "userId", required = false) String pathUserId,
            @RequestParam(value = "userId", required = false) String reqUserId
    ) {
        String userId = extractUserId(authHeader, pathUserId == null ? reqUserId : pathUserId);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> viewPost(@PathVariable String postId) {
        Optional<Post> p = postService.getPostById(postId);
        if (p.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Post not found"));
        return ResponseEntity.ok(p.get());
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<?> deletePost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String postId,
            @RequestParam(value = "userId", required = false) String userIdParam
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        postService.deletePost(userId, postId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }

    @GetMapping("/user/collections")
    public ResponseEntity<?> collections(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        return ResponseEntity.ok(postService.getCollections(userId, category, sort));
    }

    @GetMapping("/user/collections/search")
    public ResponseEntity<?> searchCollections(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String userIdParam,
            @RequestParam(value = "q") String q
    ) {
        String userId = extractUserId(authHeader, userIdParam);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        return ResponseEntity.ok(postService.searchCollections(userId, q));
    }
}
