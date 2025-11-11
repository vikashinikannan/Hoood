package Backend.socialprompt.service;

import Backend.socialprompt.dto.CreatePostRequest;
import Backend.socialprompt.model.Post;
import Backend.socialprompt.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Post createPost(String userId, String username, CreatePostRequest req) {
        Post p = Post.builder()
                .userId(userId)
                .username(username)
                .imageUrl(req.getImageUrl())
                .caption(req.getCaption())
                .prompt(req.getPrompt())
                .tags(req.getTags() == null ? new ArrayList<>() : req.getTags())
                .audience(req.getAudience() == null ? "public" : req.getAudience())
                .category(req.getCategory() == null ? "general" : req.getCategory())
                .isDraft(false)
                .likes(new HashSet<>())
                .commentsCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return postRepository.save(p);
    }

    public Post saveDraft(String userId, CreatePostRequest req) {
        Post p = Post.builder()
                .userId(userId)
                .imageUrl(req.getImageUrl())
                .caption(req.getCaption())
                .prompt(req.getPrompt())
                .tags(req.getTags() == null ? new ArrayList<>() : req.getTags())
                .isDraft(true)
                .likes(new HashSet<>())
                .commentsCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return postRepository.save(p);
    }

    public List<Post> getFeed(String userId, int page, int size) {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(p -> "public".equals(p.getAudience()) || p.getUserId().equals(userId))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<Post> getUserPosts(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Post> getPostById(String postId) {
        return postRepository.findById(postId);
    }

    public void deletePost(String userId, String postId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!p.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        postRepository.deleteById(postId);
    }

    public Post toggleLike(String userId, String postId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Set<String> likes = p.getLikes() == null ? new HashSet<>() : p.getLikes();
        if (likes.contains(userId)) likes.remove(userId); else likes.add(userId);
        p.setLikes(likes);
        p.setUpdatedAt(Instant.now());
        return postRepository.save(p);
    }

    public List<Post> getCollections(String userId, String category, String sort) {
        List<Post> list = postRepository.findAll();
        List<Post> filtered = list.stream()
                .filter(p -> {
                    if ("draft".equals(category)) return p.isDraft() && userId.equals(p.getUserId());
                    if ("saved".equals(category)) return p.isSaved();
                    if ("favorite".equals(category)) return p.isFavorite();
                    return userId.equals(p.getUserId());
                })
                .collect(Collectors.toList());

        if ("newest".equals(sort) || sort == null) {
            filtered.sort(Comparator.comparing(Post::getCreatedAt).reversed());
        } else {
            filtered.sort(Comparator.comparing(Post::getCreatedAt));
        }
        return filtered;
    }

    public List<Post> searchCollections(String userId, String query) {
        return postRepository.findByCaptionContainingIgnoreCaseOrPromptContainingIgnoreCase(
                query, query, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
