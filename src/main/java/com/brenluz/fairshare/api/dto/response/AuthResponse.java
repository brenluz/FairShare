package com.brenluz.fairshare.api.dto.response;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AuthResponse(
        String token,
        String email,
        String username
){}