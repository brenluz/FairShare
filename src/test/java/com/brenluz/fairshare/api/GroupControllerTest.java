package com.brenluz.fairshare.api;

import com.brenluz.fairshare.config.PasswordEncoderConfig;
import com.brenluz.fairshare.config.SecurityConfig;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupService;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.security.JwtUtil;
import com.brenluz.fairshare.security.UserDetailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailServiceImpl userDetailServiceImpl;

    private User ana;
    private Group group;

    @BeforeEach
    void setUp() {
        ana = User.builder()
                .id(UUID.randomUUID())
                .username("ana")
                .email("ana@test.com")
                .password("anahashed")
                .build();

        group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Trip to Bahia");
        group.setDescription("Summer trip");
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(ana);
        group.setMembers(List.of());
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserAccessesGroups() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndGroups_When_AuthenticatedUserRequestsTheirGroups() throws Exception {
        when(groupService.getUserGroups("ana@test.com")).thenReturn(List.of(group));

        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Trip to Bahia"));
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndGroup_When_AuthenticatedUserRequestsGroupById() throws Exception {
        when(groupService.findById(group.getId())).thenReturn(Optional.of(group));

        mockMvc.perform(get("/api/groups/{id}", group.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trip to Bahia"))
                .andExpect(jsonPath("$.description").value("Summer trip"));
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn201AndCreatedGroup_When_AuthenticatedUserCreatesGroup() throws Exception {
        when(groupService.createGroup(any(), eq("ana@test.com"))).thenReturn(group);

        String body = """
                {
                    "name": "Trip to Bahia",
                    "description": "Summer trip"
                }
                """;

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Trip to Bahia"));
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserTriesToCreateGroup() throws Exception {
        String body = """
                {
                    "name": "Trip to Bahia",
                    "description": "Summer trip"
                }
                """;

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}