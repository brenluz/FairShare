package com.brenluz.fairshare.api;

import com.brenluz.fairshare.config.PasswordEncoderConfig;
import com.brenluz.fairshare.config.SecurityConfig;
import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.ExpenseService;
import com.brenluz.fairshare.domain.expense.SplitType;
import com.brenluz.fairshare.domain.group.Group;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WebMvcTest(ExpenseController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailServiceImpl userDetailServiceImpl;

    private User ana;
    private Group group;
    private Expense expense;

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
        group.setMembers(List.of());

        expense = Expense.builder()
                .id(UUID.randomUUID())
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .splitType(SplitType.EQUAL)
                .paidBy(ana)
                .group(group)
                .createdAt(LocalDateTime.now())
                .splits(List.of())
                .build();
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserAccessesExpenses() throws Exception {
        mockMvc.perform(get("/api/groups/{id}/expenses", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndExpenses_When_AuthenticatedUserRequestsGroupExpenses() throws Exception {
        when(expenseService.getGroupExpenses(group.getId())).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/groups/{id}/expenses", group.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Dinner"))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    @WithMockUser(username = "ana@test.com")
    void shouldReturn200AndExpense_When_AuthenticatedUserAddsExpense() throws Exception {
        when(expenseService.addExpense(any(), eq("ana@test.com"), any())).thenReturn(expense);

        String body = """
                {
                    "description": "Dinner",
                    "amount": 100.00,
                    "splitType": "EQUAL"
                }
                """;

        mockMvc.perform(post("/api/groups/{id}/expenses", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Dinner"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserTriesToAddExpense() throws Exception {
        String body = """
                {
                    "description": "Dinner",
                    "amount": 100.00,
                    "splitType": "EQUAL"
                }
                """;

        mockMvc.perform(post("/api/groups/{id}/expenses", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}