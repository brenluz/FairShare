package com.brenluz.fairshare.api.dto.response;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String email
) {}