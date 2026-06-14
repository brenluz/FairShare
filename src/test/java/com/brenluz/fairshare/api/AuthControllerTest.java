package com.brenluz.fairshare.api;

import com.brenluz.fairshare.config.PasswordEncoderConfig;
import com.brenluz.fairshare.config.SecurityConfig;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserService;
import com.brenluz.fairshare.security.JwtUtil;
import com.brenluz.fairshare.security.UserDetailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailServiceImpl userDetailServiceImpl;

    @MockitoBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("ana")
                .email("ana@test.com")
                .password("anahashed")
                .build();
    }

    @Test
    void shouldReturn201AndToken_When_RegisterIsSuccessful() throws Exception {
        User user = buildUser();
        when(userService.register(any())).thenReturn(user);
        when(jwtUtil.generateToken("ana@test.com")).thenReturn("mocked-jwt-token");

        String body = """
                {
                    "username": "ana",
                    "email": "ana@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void shouldReturn200AndToken_When_LoginIsSuccessful() throws Exception {
        User user = buildUser();
        when(userService.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ana@test.com")).thenReturn("mocked-jwt-token");

        String body = """
                {
                    "email": "ana@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void shouldReturn401_When_LoginCredentialsAreInvalid() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        String body = """
                {
                    "email": "ana@test.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400_When_RegisterRequestIsMissingFields() throws Exception {
        String body = """
                {
                    "email": "ana@test.com"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}