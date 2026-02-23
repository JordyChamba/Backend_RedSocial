package com.backend.sh.service;

import com.backend.sh.dto.PostDTO;
import com.backend.sh.dto.UserDTO;
import com.backend.sh.entity.Like;
import com.backend.sh.entity.Post;
import com.backend.sh.entity.User;
import com.backend.sh.exception.BadRequestException;
import com.backend.sh.exception.ResourceNotFoundException;
import com.backend.sh.repository.LikeRepository;
import com.backend.sh.repository.PostRepository;
import com.backend.sh.repository.UserRepository;
import com.backend.sh.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public PostDTO createPost(PostDTO.CreatePostRequest request) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Post post = Post.builder()
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .author(user)
                .likesCount(0)
                .commentsCount(0)
                .sharesCount(0)
                .build();
        
        post = postRepository.save(post);
        
        return convertToDTO(post, userId);
    }
    
    @Transactional(readOnly = true)
    public PostDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        return convertToDTO(post, currentUserId);
    }
    
    @Transactional(readOnly = true)
    public Page<PostDTO> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return posts.map(post -> convertToDTO(post, finalCurrentUserId));
    }
    
    @Transactional(readOnly = true)
    public Page<PostDTO> getPostsByUserId(Long userId, int page, int size) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return posts.map(post -> convertToDTO(post, finalCurrentUserId));
    }
    
    @Transactional(readOnly = true)
    public Page<PostDTO> getFeedPosts(int page, int size) {
        Long userId = CurrentUser.getUserId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findFeedPosts(userId, pageable);
        
        return posts.map(post -> convertToDTO(post, userId));
    }
    
    @Transactional(readOnly = true)
    public List<PostDTO> getTrendingPosts(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Pageable pageable = PageRequest.of(0, limit);
        List<Post> posts = postRepository.findTrendingPosts(since, pageable);
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return posts.stream()
                .map(post -> convertToDTO(post, finalCurrentUserId))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<PostDTO> searchPosts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.searchPosts(query, pageable);
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return posts.map(post -> convertToDTO(post, finalCurrentUserId));
    }
    
    @Transactional
    public PostDTO updatePost(Long postId, PostDTO.UpdatePostRequest request) {
        Long userId = CurrentUser.getUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        // Verify ownership
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own posts");
        }
        
        post.setContent(request.getContent());
        post = postRepository.save(post);
        
        return convertToDTO(post, userId);
    }
    
    @Transactional
    public void deletePost(Long postId) {
        Long userId = CurrentUser.getUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        // Verify ownership
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own posts");
        }
        
        postRepository.delete(post);
    }
    
    @Transactional
    public PostDTO likePost(Long postId) {
        Long userId = CurrentUser.getUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if already liked
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("You have already liked this post");
        }
        
        // Create like
        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();
        
        likeRepository.save(like);
        
        // Update post likes count
        post.incrementLikesCount();
        post = postRepository.save(post);
        
        // Create notification (don't notify if user likes their own post)
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.createLikeNotification(user, post);
        }
        
        return convertToDTO(post, userId);
    }
    
    @Transactional
    public PostDTO unlikePost(Long postId) {
        Long userId = CurrentUser.getUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        // Check if liked
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("You have not liked this post");
        }
        
        // Delete like
        likeRepository.deleteByUserIdAndPostId(userId, postId);
        
        // Update post likes count
        post.decrementLikesCount();
        post = postRepository.save(post);
        
        return convertToDTO(post, userId);
    }
    
    private PostDTO convertToDTO(Post post, Long currentUserId) {
        // Convert author to UserDTO
        UserDTO authorDTO = UserDTO.builder()
                .id(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .fullName(post.getAuthor().getFullName())
                .profileImageUrl(post.getAuthor().getProfileImageUrl())
                .verified(post.getAuthor().getVerified())
                .build();
        
        PostDTO dto = PostDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .author(authorDTO)
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .sharesCount(post.getSharesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
        
        // Check if current user liked this post
        if (currentUserId != null) {
            boolean isLiked = likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            dto.setIsLiked(isLiked);
        }
        
        return dto;
    }
}