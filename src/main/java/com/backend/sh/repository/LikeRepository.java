package com.backend.sh.repository;

import com.backend.sh.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    
    Boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    Long countByPostId(Long postId);
    
    void deleteByUserIdAndPostId(Long userId, Long postId);
}