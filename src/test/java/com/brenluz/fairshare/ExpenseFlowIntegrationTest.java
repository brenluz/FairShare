package com.brenluz.fairshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpenseFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper to register a user and return their JWT token
    private String registerAndGetToken(String username, String email, String password) throws Exception {
        String body = """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(username, email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // Helper to create a group and return its ID
    private String createGroupAndGetId(String token, String name, String description) throws Exception {
        String body = """
                {
                    "name": "%s",
                    "description": "%s"
                }
                """.formatted(name, description);

        MvcResult result = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    void shouldCompleteFullExpenseAndSettlementFlow() throws Exception {
        // Step 1 — Register two users
        String anaToken = registerAndGetToken("ana_flow", "ana_flow@integrationtest.com", "password123");
        String bobToken = registerAndGetToken("bob_flow", "bob_flow@integrationtest.com", "password123");

        // Step 2 — Get bob's user ID from his own group list (we'll need it for settling)
        String groupId = createGroupAndGetId(anaToken, "Integration Test Trip", "Full flow test");

        MvcResult inviteResult = mockMvc.perform(get("/api/groups/{id}/invite", groupId)
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andReturn();

        String inviteLink = inviteResult.getResponse().getContentAsString();
        String inviteToken = inviteLink.substring(inviteLink.lastIndexOf("/") + 1);

        // Bob joins via invite link
        mockMvc.perform(post("/api/groups/join/{token}", inviteToken)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));
        // Step 3 — Verify the group was created
        mockMvc.perform(get("/api/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Test Trip"));

        // Step 4 — Ana adds an expense (EQUAL split among group members)
        // At this point only ana is in the group, so the split is just her
        String expenseBody = """
                {
                    "description": "Hotel",
                    "amount": 200.00,
                    "splitType": "EQUAL"
                }
                """;

        MvcResult expenseResult = mockMvc.perform(post("/api/groups/{id}/expenses", groupId)
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Hotel"))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andReturn();

        String expenseId = objectMapper.readTree(expenseResult.getResponse().getContentAsString())
                .get("id").asText();

        // Step 5 — Check balances
        mockMvc.perform(get("/api/groups/{id}/balances", groupId)
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 6 — Check simplified debts
        mockMvc.perform(get("/api/groups/{id}/simplify", groupId)
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 7 — Get ana's user ID from the group detail response
        MvcResult groupDetailResult = mockMvc.perform(get("/api/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andReturn();

        String anaId = objectMapper.readTree(groupDetailResult.getResponse().getContentAsString())
                .get("createdBy").get("id").asText();

        // Step 8 — Bob settles with ana
        String settleBody = """
                {
                    "payeeId": "%s",
                    "amount": 100.00
                }
                """.formatted(anaId);

        mockMvc.perform(post("/api/groups/{id}/settle", groupId)
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(settleBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.payer.username").value("bob_flow"))
                .andExpect(jsonPath("$.payee.username").value("ana_flow"));
    }

    @Test
    void shouldReturn401_When_UnauthenticatedUserAccessesProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/groups")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/groups/some-id/balances")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/groups/some-id/simplify")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200_When_AuthenticatedUserListsTheirGroups() throws Exception {
        String token = registerAndGetToken("listgroups_user", "listgroups@integrationtest.com", "password123");

        createGroupAndGetId(token, "Group One", "First group");
        createGroupAndGetId(token, "Group Two", "Second group");

        mockMvc.perform(get("/api/groups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}