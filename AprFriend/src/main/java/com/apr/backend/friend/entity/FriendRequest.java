
package com.apr.backend.friend.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "FRIEND_REQUESTS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long fromUserId;

    @Column(nullable = false)
    private Long toUserId;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    private OffsetDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Long getFromUserId() {
		return this.fromUserId;
	}

	public void setFromUserId(Long fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Long getToUserId() {
		return this.toUserId;
	}

	public void setToUserId(Long toUserId) {
		this.toUserId = toUserId;
	}

	public OffsetDateTime getRequestedAt() {
		return this.requestedAt;
	}

	public void setRequestedAt(OffsetDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public OffsetDateTime getApprovedAt() {
		return this.approvedAt;
	}

	public void setApprovedAt(OffsetDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}

	public RequestStatus getStatus() {
		return this.status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

}
