package com.backend.sh.repository;

import com.backend.sh.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Get comments by post (top-level only, no replies)
    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
            Long postId, Pageable pageable);
    
    // Get replies for a comment
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    // Count comments by post
    Long countByPostId(Long postId);
    
    // Count replies by parent comment
    Long countByParentCommentId(Long parentCommentId);
    
    // Get all comments by author
    Page<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}