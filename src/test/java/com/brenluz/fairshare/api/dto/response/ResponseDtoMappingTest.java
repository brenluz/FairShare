package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.algorithm.DebtTransaction;
import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.ExpenseSplit;
import com.brenluz.fairshare.domain.expense.SplitType;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupMember;
import com.brenluz.fairshare.domain.settlement.Settlement;
import com.brenluz.fairshare.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoMappingTest {

    private User ana;
    private User bob;

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
    }

    // -------------------------------------------------------------------------
    // BalanceResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_UserAndBalance_When_BuildingBalanceResponse() {
        Map.Entry<User, BigDecimal> entry =
                new AbstractMap.SimpleEntry<>(ana, new BigDecimal("42.50"));

        BalanceResponse response = BalanceResponse.from(entry);

        assertThat(response.balance()).isEqualByComparingTo("42.50");
        assertThat(response.user().id()).isEqualTo(ana.getId());
        assertThat(response.user().username()).isEqualTo("ana");
        assertThat(response.user().email()).isEqualTo("ana@test.com");
    }

    @Test
    void shouldMap_NegativeBalance_When_BuildingBalanceResponse() {
        Map.Entry<User, BigDecimal> entry =
                new AbstractMap.SimpleEntry<>(ana, new BigDecimal("-15.00"));

        BalanceResponse response = BalanceResponse.from(entry);

        assertThat(response.balance()).isEqualByComparingTo("-15.00");
    }

    @Test
    void shouldMap_ZeroBalance_When_BuildingBalanceResponse() {
        Map.Entry<User, BigDecimal> entry =
                new AbstractMap.SimpleEntry<>(ana, BigDecimal.ZERO);

        BalanceResponse response = BalanceResponse.from(entry);

        assertThat(response.balance()).isEqualByComparingTo("0");
    }

    // -------------------------------------------------------------------------
    // DebtTransactionResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_BothUsersAndAmount_When_BuildingDebtTransactionResponse() {
        DebtTransaction transaction = new DebtTransaction(ana, bob, new BigDecimal("30.00"));

        DebtTransactionResponse response = DebtTransactionResponse.from(transaction);

        assertThat(response.amount()).isEqualByComparingTo("30.00");
        assertThat(response.from().id()).isEqualTo(ana.getId());
        assertThat(response.from().username()).isEqualTo("ana");
        assertThat(response.to().id()).isEqualTo(bob.getId());
        assertThat(response.to().username()).isEqualTo("bob");
    }

    @Test
    void shouldNotSwap_FromAndTo_When_BuildingDebtTransactionResponse() {
        DebtTransaction transaction = new DebtTransaction(ana, bob, new BigDecimal("10.00"));

        DebtTransactionResponse response = DebtTransactionResponse.from(transaction);

        assertThat(response.from().username()).isEqualTo("ana");
        assertThat(response.to().username()).isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // SettlementResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_AllFields_When_BuildingSettlementResponse() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Settlement settlement = new Settlement();
        settlement.setId(id);
        settlement.setPayer(ana);
        settlement.setPayee(bob);
        settlement.setAmount(new BigDecimal("99.99"));
        settlement.setSettledAt(now);

        SettlementResponse response = SettlementResponse.from(settlement);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.amount()).isEqualByComparingTo("99.99");
        assertThat(response.settledAt()).isEqualTo(now);
        assertThat(response.payer().username()).isEqualTo("ana");
        assertThat(response.payee().username()).isEqualTo("bob");
    }

    @Test
    void shouldNotSwap_PayerAndPayee_When_BuildingSettlementResponse() {
        Settlement settlement = new Settlement();
        settlement.setId(UUID.randomUUID());
        settlement.setPayer(ana);
        settlement.setPayee(bob);
        settlement.setAmount(new BigDecimal("50.00"));
        settlement.setSettledAt(LocalDateTime.now());

        SettlementResponse response = SettlementResponse.from(settlement);

        assertThat(response.payer().username()).isEqualTo("ana");
        assertThat(response.payee().username()).isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // GroupSummaryResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_IdNameAndMemberCount_When_BuildingGroupSummaryResponse() {
        GroupMember member1 = new GroupMember();
        member1.setUser(ana);
        GroupMember member2 = new GroupMember();
        member2.setUser(bob);

        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Trip to Bahia");
        group.setMembers(List.of(member1, member2));

        GroupSummaryResponse response = GroupSummaryResponse.from(group);

        assertThat(response.id()).isEqualTo(group.getId());
        assertThat(response.name()).isEqualTo("Trip to Bahia");
        assertThat(response.memberCount()).isEqualTo(2);
    }

    @Test
    void shouldReturn_ZeroMemberCount_When_GroupHasNoMembers() {
        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Empty Group");
        group.setMembers(List.of());

        GroupSummaryResponse response = GroupSummaryResponse.from(group);

        assertThat(response.memberCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // GroupDetailResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_AllFieldsAndMembers_When_BuildingGroupDetailResponse() {
        LocalDateTime now = LocalDateTime.now();

        GroupMember member1 = new GroupMember();
        member1.setUser(ana);
        GroupMember member2 = new GroupMember();
        member2.setUser(bob);

        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Trip to Bahia");
        group.setDescription("Summer trip");
        group.setCreatedAt(now);
        group.setCreatedBy(ana);
        group.setMembers(List.of(member1, member2));

        GroupDetailResponse response = GroupDetailResponse.from(group);

        assertThat(response.id()).isEqualTo(group.getId());
        assertThat(response.name()).isEqualTo("Trip to Bahia");
        assertThat(response.description()).isEqualTo("Summer trip");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.createdBy().username()).isEqualTo("ana");
        assertThat(response.members()).hasSize(2);
        assertThat(response.members())
                .extracting(UserSummary::username)
                .containsExactlyInAnyOrder("ana", "bob");
    }

    // -------------------------------------------------------------------------
    // ExpenseResponse
    // -------------------------------------------------------------------------

    @Test
    void shouldMap_AllFieldsAndSplits_When_BuildingExpenseResponse() {
        LocalDateTime now = LocalDateTime.now();

        ExpenseSplit split1 = new ExpenseSplit();
        split1.setUser(ana);
        split1.setOwedAmount(new BigDecimal("50.00"));

        ExpenseSplit split2 = new ExpenseSplit();
        split2.setUser(bob);
        split2.setOwedAmount(new BigDecimal("50.00"));

        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setDescription("Dinner");
        expense.setAmount(new BigDecimal("100.00"));
        expense.setSplitType(SplitType.EQUAL);
        expense.setPaidBy(ana);
        expense.setCreatedAt(now);
        expense.setSplits(List.of(split1, split2));

        ExpenseResponse response = ExpenseResponse.from(expense);

        assertThat(response.id()).isEqualTo(expense.getId());
        assertThat(response.description()).isEqualTo("Dinner");
        assertThat(response.amount()).isEqualByComparingTo("100.00");
        assertThat(response.splitType()).isEqualTo(SplitType.EQUAL);
        assertThat(response.paidBy().username()).isEqualTo("ana");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.splits()).hasSize(2);
        assertThat(response.splits())
                .extracting(s -> s.user().username())
                .containsExactlyInAnyOrder("ana", "bob");
        assertThat(response.splits())
                .extracting(SplitResponse::owedAmount)
                .allMatch(amount -> amount.compareTo(new BigDecimal("50.00")) == 0);
    }

    @Test
    void shouldReturn_EmptySplitsList_When_ExpenseHasNoSplits() {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setDescription("Solo expense");
        expense.setAmount(new BigDecimal("100.00"));
        expense.setSplitType(SplitType.EQUAL);
        expense.setPaidBy(ana);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setSplits(List.of());

        ExpenseResponse response = ExpenseResponse.from(expense);

        assertThat(response.splits()).isEmpty();
    }
}