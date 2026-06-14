package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.LoginRequest;
import com.brenluz.fairshare.api.dto.request.RegisterRequest;
import com.brenluz.fairshare.api.dto.response.AuthResponse;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserService;
import com.brenluz.fairshare.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        User registered = userService.register(request);
        String token = jwtUtil.generateToken(registered.getEmail());
        return new AuthResponse(token, registered.getEmail(), registered.getUsername());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtUtil.generateToken(request.email());
        User user = userService.findByEmail(request.email()).orElseThrow();
        return new AuthResponse(token, user.getEmail(), user.getUsername());
    }
}
