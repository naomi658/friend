
package com.apr.backend.friend.serviceImpl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.apr.backend.friend.dto.FriendDtos;
import com.apr.backend.friend.entity.FriendRequest;
import com.apr.backend.friend.repository.FriendRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository repo;
    private static final int FRIEND_LIMIT = 10_000;

    private final Map<Long, Deque<Long>> rateLimit = new ConcurrentHashMap<>();

    // 초당 10회 요청 제한
    private void checkRateLimit(Long userId) {
        long now = System.currentTimeMillis();
        Deque<Long> dq = rateLimit.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && dq.peekFirst() < now - 1000L) dq.pollFirst();
            if (dq.size() >= 10) {
                throw new ResponseStatusException(TOO_MANY_REQUESTS, "초당 10회 요청 제한(429)");
            }
            dq.addLast(now);
        }
    }

    // 친구 신청
    @Transactional
    public UUID sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId == null) throw new ResponseStatusException(BAD_REQUEST, "X-user-Id header required");
        if (fromUserId.equals(toUserId)) throw new ResponseStatusException(BAD_REQUEST, "자기 자신에게는 요청 불가");

        // 친구 신청 보낸 상태인지 check
        if (repo.existsByFromUserIdAndToUserIdAndStatus(fromUserId, toUserId, FriendRequest.RequestStatus.PENDING)) {
            throw new ResponseStatusException(CONFLICT, "이미 요청 중입니다");
        }
        
        // 초당 10회 요청 제한
        checkRateLimit(fromUserId);
        
        FriendRequest fr = FriendRequest.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .requestedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .status(FriendRequest.RequestStatus.PENDING)
                .build();
        
        fr = repo.save(fr);
        
        return fr.getId();
    }

    // 친구 목록 조회
    public Page<FriendDtos.FriendItem> getFriends(Long userId, Pageable pageable) {
        Page<FriendRequest> page = repo.retrieveApprovedFriends(userId, pageable);
        
        List<FriendDtos.FriendItem> items = page.getContent().stream().map(fr ->
                new FriendDtos.FriendItem(fr.getId(), fr.getFromUserId(), fr.getToUserId(), fr.getApprovedAt())
        ).collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    // 받은 친구 신청 목록 조회(상태값 PENDING인 건 조회)
    public List<FriendDtos.RequestItem> getReceivedRequests(Long userId, String window, int maxSize) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime from = switch (window) {
            case "1d" -> now.minusDays(1); 		// 최근 1일
            case "7d" -> now.minusDays(7); 		// 최근 7일
            case "30d" -> now.minusDays(30);	// 최근 30일
            case "90d" -> now.minusDays(90);	// 최근 90일
            case "over" -> OffsetDateTime.MIN;	// 90일 초과 (장기 누적)
            default -> now.minusDays(1);
        };
        
        Page<FriendRequest> page = repo.retrievePendingFriends(userId, from, PageRequest.of(0, Math.max(1, maxSize)));
        
        return page.stream().map(fr -> new FriendDtos.RequestItem(fr.getId(), fr.getFromUserId(), fr.getRequestedAt())).collect(Collectors.toList());
    }

    // 승인
    @Transactional
    public void acceptRequest(Long userId, UUID requestId) {
        FriendRequest fr = repo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "request not found"));

        if (!fr.getToUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "다른 사용자의 요청을 수락할 수 없습니다");
        }

        long approvedCount = repo.retrieveApprovedFriends(userId, PageRequest.of(0, 1)).getTotalElements();
        
        if (approvedCount >= FRIEND_LIMIT) {
            throw new ResponseStatusException(BAD_REQUEST, "친구 수 한도 초과");
        }

        fr.setStatus(FriendRequest.RequestStatus.APPROVED);
        fr.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
        repo.save(fr);
    }

    // 거절
    @Transactional
    public void rejectRequest(Long userId, UUID requestId) {
        FriendRequest fr = repo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "request not found"));

        if (!fr.getToUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "다른 사용자의 요청을 거절할 수 없습니다");
        }

        repo.delete(fr);
    }
}
