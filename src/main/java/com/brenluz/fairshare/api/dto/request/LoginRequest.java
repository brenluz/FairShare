package com.brenluz.fairshare.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record LoginRequest(
        @Email @NotEmpty String email,
        @NotEmpty @Size(min = 8) String password
){}