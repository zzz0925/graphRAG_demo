// graphitiDemo/src/main/repository/ChatSessionRepository.java
package com.chat.repository;

import com.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // 导入 List
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionId(String sessionId);

    // 新增：获取所有会话，按创建时间降序排列
    List<ChatSession> findAllByOrderByCreatedTimeDesc();
}