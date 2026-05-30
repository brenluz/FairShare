package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.group.Group;

import java.util.UUID;

public record GroupSummaryResponse(
        UUID id,
        String name,
        int memberCount
) {
    public static GroupSummaryResponse from(Group group) {
        return new GroupSummaryResponse(
                group.getId(),
                group.getName(),
                group.getMembers().size()
        );
    }
}