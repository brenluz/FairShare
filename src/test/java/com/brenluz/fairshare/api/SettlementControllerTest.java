package com.brenluz.fairshare.api;

import com.brenluz.fairshare.algorithm.DebtTransaction;
import com.brenluz.fairshare.config.PasswordEncoderConfig;
import com.brenluz.fairshare.config.SecurityConfig;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.settlement.Settlement;
import com.brenluz.fairshare.domain.settlement.SettlementService;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.security.JwtUtil;
import com.brenluz.fairshare.security.UserDetailServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementService settlementService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailServiceImpl userDetailServiceImpl;

    private User ana;
    private User bob;
    private Group group;

    @BeforeEach
    void setUp() {
        ana = User.builder()
                .id(UUID.randomUUID())
                .username("ana")
                .email("ana@test.com")
                .password("anahashed")
                .build();

        bob = User.builder()
                .id(UUID.randomUUID())
                .username("bob")
                .email("bob@test.com")
                .password("bobhashed")
                .build();

        group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Trip to Bahia");
        group.setMembers(List.of());
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserAccessesBalances() throws Exception {
        mockMvc.perform(get("/api/groups/{id}/balances", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndBalances_When_AuthenticatedUserRequestsBalances() throws Exception {
        when(settlementService.getBalances(group.getId()))
                .thenReturn(Map.of(ana, new BigDecimal("50.00"), bob, new BigDecimal("-50.00")));

        mockMvc.perform(get("/api/groups/{id}/balances", group.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndTransactions_When_AuthenticatedUserRequestsSimplifiedDebts() throws Exception {
        DebtTransaction transaction = new DebtTransaction(bob, ana, new BigDecimal("50.00"));
        when(settlementService.getDebtTransactions(group.getId())).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/groups/{id}/simplify", group.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].from.username").value("bob"))
                .andExpect(jsonPath("$[0].to.username").value("ana"));
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndSettlement_When_AuthenticatedUserSettlesDebt() throws Exception {
        Settlement settlement = Settlement.builder()
                .id(UUID.randomUUID())
                .payer(ana)
                .payee(bob)
                .group(group)
                .amount(new BigDecimal("50.00"))
                .settledAt(LocalDateTime.now())
                .build();

        when(settlementService.settleDebt(any(), any(), eq("ana@test.com"))).thenReturn(settlement);

        String body = """
                {
                    "payeeId": "%s",
                    "amount": 50.00
                }
                """.formatted(bob.getId());

        mockMvc.perform(post("/api/groups/{id}/settle", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payer.username").value("ana"))
                .andExpect(jsonPath("$.payee.username").value("bob"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserTriesToSettle() throws Exception {
        String body = """
                {
                    "payeeId": "%s",
                    "amount": 50.00
                }
                """.formatted(bob.getId());

        mockMvc.perform(post("/api/groups/{id}/settle", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}