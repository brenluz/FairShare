package com.brenluz.fairshare.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
        @NotBlank String name,
        String description
){}