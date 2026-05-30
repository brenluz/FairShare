package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.group.Group;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GroupDetailResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        UserSummary createdBy,
        List<UserSummary> members
) {
    public static GroupDetailResponse from(Group group) {
        return new GroupDetailResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                new UserSummary(
                        group.getCreatedBy().getId(),
                        group.getCreatedBy().getUsername(),
                        group.getCreatedBy().getEmail()
                ),
                group.getMembers().stream()
                        .map(m -> new UserSummary(
                                m.getUser().getId(),
                                m.getUser().getUsername(),
                                m.getUser().getEmail()
                        ))
                        .toList()
        );
    }
}