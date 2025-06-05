// ChatMessageRepository.java
package com.chat.repository;

import com.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE m.session.sessionId = :sessionId ORDER BY m.createdTime")
    List<ChatMessage> findBySessionIdOrderByCreatedTime(@Param("sessionId") String sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.graphitiSaved = false")
    List<ChatMessage> findUnsavedMessages();
}
