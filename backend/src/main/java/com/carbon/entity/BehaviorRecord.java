package com.carbon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "behavior_records")
public class BehaviorRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "behavior_type", length = 64, nullable = false)
    private String behaviorType;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "image_hash", length = 64, nullable = false)
    private String imageHash;

    @Column(name = "points", nullable = false)
    private Long points = 0L;

    @Column(name = "ai_label", length = 128)
    private String aiLabel;

    @Column(name = "ai_score", nullable = false)
    private BigDecimal aiScore = BigDecimal.ZERO;

    @Column(name = "decision", length = 32, nullable = false)
    private String decision;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "tx_hash", length = 128)
    private String txHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAiLabel() {
        return aiLabel;
    }

    public void setAiLabel(String aiLabel) {
        this.aiLabel = aiLabel;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public BigDecimal getAiScore() {
        return aiScore;
    }

    public void setAiScore(BigDecimal aiScore) {
        this.aiScore = aiScore;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
