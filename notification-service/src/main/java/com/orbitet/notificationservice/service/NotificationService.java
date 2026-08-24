package com.orbitet.notificationservice.service;

import com.orbitet.notificationservice.dto.NotificationDto;
import com.orbitet.notificationservice.entity.Notification;
import com.orbitet.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepo;

    @Transactional
    public void notifyUser(Long userId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notificationRepo.save(notification);
        log.info("Stored notification for user id {}: {}", userId, message);
    }

    /**
     * @param page 1-indexed page number, as exposed by the API
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsOfUser(Long userId, int page, int pageSize) {
        log.info("Getting notifications page {} (size {}) for user id {}", page, pageSize, userId);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Notification> notifications = notificationRepo.findByUserIdOrderBySentAtDesc(userId, pageable);
        return notifications.map(NotificationDto::from).getContent();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndReadIsFalse(userId);
    }

    /**
     * @return false when the notification does not exist or belongs to another user
     */
    @Transactional
    public boolean markRead(Long notificationId, Long userId) {
        return notificationRepo.markRead(notificationId, userId) > 0;
    }
}
