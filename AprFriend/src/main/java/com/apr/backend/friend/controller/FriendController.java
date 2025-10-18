
package com.apr.backend.friend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apr.backend.friend.dto.FriendDtos;
import com.apr.backend.friend.serviceImpl.FriendService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "FriendController - /api/friends", description = "APR 백엔드 직무과제_지원자_이민영")
public class FriendController {

    private final FriendService friendService ;

    private Long userIdFromHeader(HttpServletRequest req) {
        String xUserId = req.getHeader("X-user-Id");
        
        if (xUserId == null) throw new IllegalArgumentException("X-user-Id header required");
        
        return Long.parseLong(xUserId);
    }

    /* 4-1 친구 목록 조회
     * return pageable
     * order by approvedAt
     */
    @Operation(
            summary = "친구 목록 조회",
            description = "내가 승인한 친구 목록을 조회합니다.",
            parameters = {
                @Parameter(name = "page", description = "페이지 번호", example = "0", in = ParameterIn.QUERY),
                @Parameter(name = "maxSize", description = "페이지당 항목 수", example = "20", in = ParameterIn.QUERY),
                @Parameter(name = "sort", description = "정렬 기준", example = "approvedAt,desc", in = ParameterIn.QUERY),
                @Parameter(name = "X-user-Id", description = "현재 사용자 ID", example = "100044737", required = true, in = ParameterIn.HEADER)
            }
        )
    @ApiResponse(responseCode = "200", description = "성공적으로 친구 목록을 조회함")
    @GetMapping
    public ResponseEntity<?> getFriends(HttpServletRequest req, @RequestParam(name="page", defaultValue = "0") int page
    		, @RequestParam(name="maxSize", defaultValue = "20") int maxSize, @RequestParam(name="sort", defaultValue = "approvedAt,desc") String sort) {

        Long userId = userIdFromHeader(req);
        String[] sortParts = sort.split(","); // 정렬 방식 split
        Sort.Direction dir = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC; // 정렬 설정
        Pageable pageable = PageRequest.of(page, maxSize, Sort.by(dir, sortParts[0]));
        
        var friendsList = friendService.getFriends(userId, pageable);

        Map<String, Object> payload = Map.of("data", Map.of(
                "totalPages", friendsList.getTotalPages(),
                "totalCount", friendsList.getTotalElements(),
                "items", friendsList.getContent()
        ));
        
        return ResponseEntity.ok(payload);
        
    }

    /* 4-2 받은 친구 신청 목록 조회
     * 나를 기준으로 받은 친구 신청 목록을 조회
     * 슬라이드 윈도우 적용 > 현재 시각 기준으로 과거 1일(또는 7일, 30일 등) 동안 받은 친구 신청 요청들을 requestedAt 기준으로 DESC 정렬하여
     * 해당 범위 내에서 최대 maxSize 개수만큼 데이터를 반환
     * 
     * 예시입니다.
	 *	현재 시간:  2025-08-22T12:00:00Z > UTC
	 *	window =  1d  → 필터링 기준:  requestedAt >= 2025-08-21T12:00:00Z
	 *	정렬 기준:  requestedAt DESC
	 *	결과: 위 조건을 만족하는 요청 중 최신 순으로 maxSize 개수만큼 반환
     */
    @Operation(
            summary = "받은 친구 신청 목록 조회",
            description = "나에게 들어온 친구 요청 목록을 조회합니다.",
            parameters = {
                @Parameter(name = "maxSize", example = "20", in = ParameterIn.QUERY),
                @Parameter(name = "window", example = "1d", in = ParameterIn.QUERY),
                @Parameter(name = "sort", example = "requestedAt,desc", in = ParameterIn.QUERY),
                @Parameter(name = "X-user-Id", description = "현재 사용자 ID", example = "100044737", required = true, in = ParameterIn.HEADER)
            }
        )
    @GetMapping("/requests")
    public ResponseEntity<?> getReceivedRequests(HttpServletRequest req, @RequestParam(name="maxSize", defaultValue = "20") int maxSize
    		, @RequestParam(name="window", defaultValue = "1d") String window, @RequestParam(name="sort", defaultValue = "requestedAt,desc") String sort) {

        Long userId = userIdFromHeader(req);
        List<FriendDtos.RequestItem> items = friendService.getReceivedRequests(userId, window, maxSize);
        
        Map<String, Object> payload = Map.of("data", Map.of(
                "window", window,
                "totalCount", items.size(),
                "items", items
        ));
        
        return ResponseEntity.ok(payload);
        
    }

    /* 4-3 친구 신청
     * 요청 시, 상대방의 친구 신청 목록에 데이터가 추가됩니다.
     * 친구 신청의 취소의 개념은 고려하지 않습니다.
     * 단, 사용자별 초당 10회 요청 제한이 있으며 초과 시 429 Too Many Requests 를 반환합니다.
     * 본인의 설계에 따라 body는 임의로 필요한 내용을 채우면 됩니다.
     * 자기 자신에게 요청, 존재하지 않는 사용자, 이미 보낸 요청 등은 예외 처리해야 하며, 상황에 맞는 HTTP Status Code와 메시지를 반환
     * 해야 합니다.
     * 상대가 거절을할 경우 재요청이 가능함
     */
    @Operation(
    	    summary = "친구 신청",
    	    description = "특정 사용자에게 친구 요청을 보냅니다.",
    	    parameters = {
    	        @Parameter(name = "X-user-Id", description = "요청자 ID", example = "20", required = true, in = ParameterIn.HEADER)
    	    },
    	    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    	        description = "친구 신청 대상 정보",
    	        required = true,
    	        content = @Content(
    	            mediaType = "application/json",
    	            schema = @Schema(example = "{\n  \"toUserId\": \"100044737\"\n}")
    	        )
    	    )
    	)
    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        
    	Long fromUserId = userIdFromHeader(req);
        Object chkToUserId = body.get("toUserId");
        
        // toUserId null check
        if (chkToUserId == null) throw new IllegalArgumentException("toUserId required in body");
        
        Long toUserId = Long.parseLong(chkToUserId.toString());
        UUID id = friendService.sendRequest(fromUserId, toUserId);
        
        return ResponseEntity.ok(Map.of("requestId", id));
        
    }

    /* 4-4  친구 수락
     * 요청 시, 요청자와 수락자의 친구 목록에 관계가 추가됩니다.
     * 본인의 설계에 따라 body는 임의로 필요한 내용을 채우면 됩니다.
     * 수락 불가 상황일 경우 예외를 반환하며, 상황에 맞는 HTTP Status Code와 메시지를 내려야 합니다
     */ 
    @Operation(
            summary = "친구 요청 승인",
            description = "특정 친구 요청을 승인합니다.",
            parameters = {
                @Parameter(name = "X-user-Id", description = "현재 사용자 ID", example = "100044737", required = true, in = ParameterIn.HEADER),
                @Parameter(name = "requestId", description = "친구 요청 UUID", example = "[requestId]", in = ParameterIn.PATH)
            }
        )
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(HttpServletRequest req, @PathVariable("requestId") UUID requestId) {
        Long userId = userIdFromHeader(req);
        friendService.acceptRequest(userId, requestId);
        
        return ResponseEntity.ok(Map.of("message", "accepted"));
    }

    /* 4-5 친구 거절
     * 요청 시, 받은 요청 데이터가 삭제됩니다.
     * 본인의 설계에 따라 body는 임의로 필요한 내용을 채우면 됩니다.
     * 거절 불가 상황일 경우 예외를 반환하며, 상황에 맞는 HTTP Status Code와 메시지를 내려야 합니다. 
     */
    @Operation(
            summary = "친구 요청 거절",
            description = "특정 친구 요청을 거절합니다.",
            parameters = {
                @Parameter(name = "X-user-Id", description = "현재 사용자 ID", example = "100044737", required = true, in = ParameterIn.HEADER),
                @Parameter(name = "requestId", description = "친구 요청 UUID", example = "[requestId]", in = ParameterIn.PATH)
            }
        )
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(HttpServletRequest req, @PathVariable("requestId") UUID requestId) {
        Long userId = userIdFromHeader(req);
        friendService.rejectRequest(userId, requestId);
        
        return ResponseEntity.ok(Map.of("message", "rejected"));
    }
    
}
