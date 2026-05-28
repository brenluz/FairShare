package com.brenluz.fairshare.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotEmpty String username,
        @Email @NotEmpty String email,
        @NotEmpty @Size(min = 8) String password
) {}