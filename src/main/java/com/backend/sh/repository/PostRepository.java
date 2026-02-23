package com.backend.sh.repository;


import com.backend.sh.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Get all posts ordered by creation date
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Get posts by author
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    // Get posts from followed users (feed)
    @Query("SELECT p FROM Post p WHERE p.author.id IN " +
           "(SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findFeedPosts(@Param("userId") Long userId, Pageable pageable);
    
    // Get trending posts (most liked in last 24 hours)
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since " +
           "ORDER BY p.likesCount DESC, p.createdAt DESC")
    List<Post> findTrendingPosts(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Count posts by author
    Long countByAuthorId(Long authorId);
    
    // Search posts by content
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);
}