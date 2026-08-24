package com.orbitet.notificationservice.repository;

import com.orbitet.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadIsFalse(Long userId);

    /**
     * Scoped by user id as well as notification id, so a caller cannot mark
     * someone else's notification read.
     *
     * @return rows updated: 0 when the notification does not exist or is not the caller's
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.userId = :userId")
    int markRead(Long id, Long userId);
}
