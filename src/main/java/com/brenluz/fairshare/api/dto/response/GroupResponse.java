package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.group.Group;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        UserSummary createdBy
) {
    public record UserSummary(
            UUID id,
            String username,
            String email
    ) {}

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                new UserSummary(
                        group.getCreatedBy().getId(),
                        group.getCreatedBy().getUsername(),
                        group.getCreatedBy().getEmail()
                )
        );
    }
}