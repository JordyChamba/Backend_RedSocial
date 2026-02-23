package com.backend.sh.service;

import com.backend.sh.dto.CommentDTO;
import com.backend.sh.dto.UserDTO;
import com.backend.sh.entity.Comment;
import com.backend.sh.entity.Post;
import com.backend.sh.entity.User;
import com.backend.sh.exception.BadRequestException;
import com.backend.sh.exception.ResourceNotFoundException;
import com.backend.sh.repository.CommentRepository;
import com.backend.sh.repository.PostRepository;
import com.backend.sh.repository.UserRepository;
import com.backend.sh.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public CommentDTO createComment(Long postId, CommentDTO.CreateCommentRequest request) {
        Long userId = CurrentUser.getUserId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent comment not found with id: " + request.getParentCommentId()));
            
            // Verify parent comment belongs to the same post
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new BadRequestException("Parent comment does not belong to this post");
            }
        }
        
        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(user)
                .parentComment(parentComment)
                .likesCount(0)
                .build();
        
        comment = commentRepository.save(comment);
        
        // Update post comments count
        post.incrementCommentsCount();
        postRepository.save(post);
        
        // Create notification
        if (parentComment != null) {
            // Reply to a comment
            if (!parentComment.getAuthor().getId().equals(userId)) {
                notificationService.createReplyNotification(user, parentComment);
            }
        } else {
            // Comment on a post
            if (!post.getAuthor().getId().equals(userId)) {
                notificationService.createCommentNotification(user, post);
            }
        }
        
        return convertToDTO(comment);
    }
    
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByPostId(Long postId, int page, int size) {
        // Verify post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository
                .findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId, pageable);
        
        return comments.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CommentDTO> getRepliesByCommentId(Long commentId) {
        // Verify comment exists
        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        
        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentDTO updateComment(Long commentId, CommentDTO.UpdateCommentRequest request) {
        Long userId = CurrentUser.getUserId();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        // Verify ownership
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own comments");
        }
        
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        
        return convertToDTO(comment);
    }
    
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = CurrentUser.getUserId();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        // Verify ownership
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own comments");
        }
        
        Post post = comment.getPost();
        
        // Delete comment (cascade will delete replies)
        commentRepository.delete(comment);
        
        // Update post comments count
        post.decrementCommentsCount();
        postRepository.save(post);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        // Convert author to UserDTO
        UserDTO authorDTO = UserDTO.builder()
                .id(comment.getAuthor().getId())
                .username(comment.getAuthor().getUsername())
                .fullName(comment.getAuthor().getFullName())
                .profileImageUrl(comment.getAuthor().getProfileImageUrl())
                .verified(comment.getAuthor().getVerified())
                .build();
        
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(authorDTO)
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? 
                        comment.getParentComment().getId() : null)
                .likesCount(comment.getLikesCount())
                .repliesCount(comment.getReplies().size())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
        
        return dto;
    }
}