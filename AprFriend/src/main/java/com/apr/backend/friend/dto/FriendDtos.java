
package com.apr.backend.friend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FriendDtos {

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class FriendItem {
        private UUID requestId;
        private Long fromUserId;
        private Long toUserId;
        private OffsetDateTime approvedAt;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class RequestItem {
        private UUID requestId;
        private Long requestUserId;
        private OffsetDateTime requestedAt;
    }
    
}
