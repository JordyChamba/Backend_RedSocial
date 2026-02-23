package com.backend.sh.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.backend.sh.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    private Long id;
    private Notification.NotificationType type;
    private String message;
    private UserDTO sender;
    private Long postId;
    private Long commentId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}