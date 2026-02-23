package com.backend.sh.service;

import com.backend.sh.dto.NotificationDTO;
import com.backend.sh.dto.UserDTO;
import com.backend.sh.entity.Comment;
import com.backend.sh.entity.Notification;
import com.backend.sh.entity.Post;
import com.backend.sh.entity.User;
import com.backend.sh.exception.BadRequestException;
import com.backend.sh.exception.ResourceNotFoundException;
import com.backend.sh.repository.NotificationRepository;
import com.backend.sh.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(int page, int size) {
        Long userId = CurrentUser.getUserId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        Long userId = CurrentUser.getUserId();
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Long userId = CurrentUser.getUserId();
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        
        // Verify ownership
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new BadRequestException("You can only mark your own notifications as read");
        }
        
        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        
        return convertToDTO(notification);
    }
    
    @Transactional
    public void markAllAsRead() {
        Long userId = CurrentUser.getUserId();
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    
    @Transactional
    public void deleteNotification(Long notificationId) {
        Long userId = CurrentUser.getUserId();
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        
        // Verify ownership
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own notifications");
        }
        
        notificationRepository.delete(notification);
    }
    
    @Transactional
    public void deleteAllNotifications() {
        Long userId = CurrentUser.getUserId();
        notificationRepository.deleteByRecipientId(userId);
    }
    
    // Helper methods to create notifications
    
    @Transactional
    public void createLikeNotification(User sender, Post post) {
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.LIKE)
                .message(sender.getUsername() + " liked your post")
                .recipient(post.getAuthor())
                .sender(sender)
                .post(post)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send WebSocket notification
        sendWebSocketNotification(notification);
    }
    
    @Transactional
    public void createCommentNotification(User sender, Post post) {
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.COMMENT)
                .message(sender.getUsername() + " commented on your post")
                .recipient(post.getAuthor())
                .sender(sender)
                .post(post)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send WebSocket notification
        sendWebSocketNotification(notification);
    }
    
    @Transactional
    public void createReplyNotification(User sender, Comment parentComment) {
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.REPLY)
                .message(sender.getUsername() + " replied to your comment")
                .recipient(parentComment.getAuthor())
                .sender(sender)
                .post(parentComment.getPost())
                .comment(parentComment)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send WebSocket notification
        sendWebSocketNotification(notification);
    }
    
    @Transactional
    public void createFollowNotification(User sender, User recipient) {
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.FOLLOW)
                .message(sender.getUsername() + " started following you")
                .recipient(recipient)
                .sender(sender)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send WebSocket notification
        sendWebSocketNotification(notification);
    }
    
    private void sendWebSocketNotification(Notification notification) {
        try {
            NotificationDTO dto = convertToDTO(notification);
            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getId().toString(),
                    "/queue/notifications",
                    dto
            );
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        UserDTO senderDTO = null;
        if (notification.getSender() != null) {
            senderDTO = UserDTO.builder()
                    .id(notification.getSender().getId())
                    .username(notification.getSender().getUsername())
                    .fullName(notification.getSender().getFullName())
                    .profileImageUrl(notification.getSender().getProfileImageUrl())
                    .verified(notification.getSender().getVerified())
                    .build();
        }
        
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .sender(senderDTO)
                .postId(notification.getPost() != null ? notification.getPost().getId() : null)
                .commentId(notification.getComment() != null ? notification.getComment().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}