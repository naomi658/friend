
package com.apr.backend.friend.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apr.backend.friend.entity.FriendRequest;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

	boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, FriendRequest.RequestStatus status);

	@Query("""
			SELECT FR
			FROM FriendRequest FR
			WHERE FR.toUserId = :userId
			AND FR.status = 'APPROVED'
			""")
	Page<FriendRequest> retrieveApprovedFriends(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

	@Query("""
			SELECT FR
			FROM FriendRequest FR
			WHERE FR.toUserId = :toUserId
			AND FR.requestedAt >= :fromTime
			AND FR.status = 'PENDING'
			ORDER BY FR.requestedAt DESC
			""")
	Page<FriendRequest> retrievePendingFriends(@Param("toUserId") Long toUserId,
			@Param("fromTime") OffsetDateTime fromTime, @Param("pageable") Pageable pageable);

	long countByStatusAndFromUserIdOrStatusAndToUserId(FriendRequest.RequestStatus s1, Long fromUserId,
			FriendRequest.RequestStatus s2, Long toUserId);
}
