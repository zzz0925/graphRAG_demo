package com.chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false, length = 10)
    private String role; // "user" or "assistant"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "graphiti_saved")
    private Boolean graphitiSaved = false;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
