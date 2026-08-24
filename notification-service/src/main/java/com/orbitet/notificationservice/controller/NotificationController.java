package com.orbitet.notificationservice.controller;

import com.orbitet.notificationservice.dto.NotificationDto;
import com.orbitet.notificationservice.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Notifications are always read as "mine" — the id comes from the {@code X-User-Id}
 * header the gateway sets from a validated token, never from the request body or path.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(50) int pageSize) {
        return ResponseEntity.ok(notificationService.getNotificationsOfUser(userId, page, pageSize));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(Map.of("unread", notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long notificationId) {
        if (!notificationService.markRead(notificationId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Notification not found with id " + notificationId);
        }
        return ResponseEntity.noContent().build();
    }
}
