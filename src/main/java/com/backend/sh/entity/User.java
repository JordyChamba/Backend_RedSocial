package com.backend.sh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 100)
    private String fullName;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 100)
    private String location;
    
    @Column(length = 255)
    private String profileImageUrl;
    
    @Column(length = 255)
    private String coverImageUrl;
    
    @Column(length = 255)
    private String website;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Like> likes = new HashSet<>();
    
    // Followers: users who follow this user
    @ManyToMany
    @JoinTable(
        name = "user_followers",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    @Builder.Default
    private Set<User> followers = new HashSet<>();
    
    // Following: users this user follows
    @ManyToMany(mappedBy = "followers")
    @Builder.Default
    private Set<User> following = new HashSet<>();
    
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Notification> notifications = new HashSet<>();
    
    // Helper methods
    public void follow(User user) {
        this.following.add(user);
        user.getFollowers().add(this);
    }
    
    public void unfollow(User user) {
        this.following.remove(user);
        user.getFollowers().remove(this);
    }
    
    public boolean isFollowing(User user) {
        return this.following.contains(user);
    }
}