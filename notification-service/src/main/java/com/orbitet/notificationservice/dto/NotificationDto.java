package com.orbitet.notificationservice.dto;

import com.orbitet.notificationservice.entity.Notification;

import java.time.LocalDateTime;

public record NotificationDto(Long id, String message, boolean read, LocalDateTime sentAt) {

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getSentAt());
    }
}
