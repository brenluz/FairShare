package com.brenluz.fairshare.domain.settlement;

import com.brenluz.fairshare.algorithm.DebtTransaction;
import com.brenluz.fairshare.api.dto.request.SettleDebtRequest;
import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.ExpenseRepository;
import com.brenluz.fairshare.domain.expense.ExpenseSplit;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupRepository;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;

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
    }

    // -------------------------------------------------------------------------
    // getBalances
    // -------------------------------------------------------------------------

    @Test
    void shouldCalculateBalances_When_OnePersonPaidForEveryone() {
        ExpenseSplit splitAna = new ExpenseSplit();
        splitAna.setUser(ana);
        splitAna.setOwedAmount(new BigDecimal("50.00"));

        ExpenseSplit splitBob = new ExpenseSplit();
        splitBob.setUser(bob);
        splitBob.setOwedAmount(new BigDecimal("50.00"));

        Expense expense = Expense.builder()
                .id(UUID.randomUUID())
                .paidBy(ana)
                .group(group)
                .amount(new BigDecimal("100.00"))
                .splits(List.of(splitAna, splitBob))
                .build();

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(List.of(expense));

        Map<User, BigDecimal> balances = settlementService.getBalances(group.getId());

        // Ana paid 100, owes 50 → net +50
        // Bob paid 0, owes 50 → net -50
        assertThat(balances.get(ana)).isEqualByComparingTo("50.00");
        assertThat(balances.get(bob)).isEqualByComparingTo("-50.00");
    }

    @Test
    void shouldReturnEmptyBalances_When_GroupHasNoExpenses() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(List.of());

        Map<User, BigDecimal> balances = settlementService.getBalances(group.getId());

        assertThat(balances).isEmpty();
    }

    @Test
    void shouldThrowException_When_GroupNotFound_OnGetBalances() {
        when(groupRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settlementService.getBalances(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Group not found");
    }

    // -------------------------------------------------------------------------
    // getDebtTransactions
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnSimplifiedTransactions_When_GroupHasDebts() {
        ExpenseSplit splitAna = new ExpenseSplit();
        splitAna.setUser(ana);
        splitAna.setOwedAmount(new BigDecimal("50.00"));

        ExpenseSplit splitBob = new ExpenseSplit();
        splitBob.setUser(bob);
        splitBob.setOwedAmount(new BigDecimal("50.00"));

        Expense expense = Expense.builder()
                .id(UUID.randomUUID())
                .paidBy(ana)
                .group(group)
                .amount(new BigDecimal("100.00"))
                .splits(List.of(splitAna, splitBob))
                .build();

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(List.of(expense));

        List<DebtTransaction> transactions = settlementService.getDebtTransactions(group.getId());

        assertThat(transactions).hasSize(1);
        assertThat(transactions.getFirst().from().getUsername()).isEqualTo("bob");
        assertThat(transactions.getFirst().to().getUsername()).isEqualTo("ana");
        assertThat(transactions.getFirst().amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldReturnEmptyTransactions_When_GroupIsSettled() {
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(List.of());

        List<DebtTransaction> transactions = settlementService.getDebtTransactions(group.getId());

        assertThat(transactions).isEmpty();
    }

    // -------------------------------------------------------------------------
    // settleDebt
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateSettlement_When_ValidRequestIsProvided() {
        SettleDebtRequest request = new SettleDebtRequest(bob.getId(), new BigDecimal("50.00"));

        Settlement savedSettlement = Settlement.builder()
                .id(UUID.randomUUID())
                .payer(ana)
                .payee(bob)
                .group(group)
                .amount(new BigDecimal("50.00"))
                .build();

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(settlementRepository.save(any())).thenReturn(savedSettlement);

        Settlement result = settlementService.settleDebt(group.getId(), request, "ana@test.com");

        assertThat(result.getPayer().getUsername()).isEqualTo("ana");
        assertThat(result.getPayee().getUsername()).isEqualTo("bob");
        assertThat(result.getAmount()).isEqualByComparingTo("50.00");
        verify(settlementRepository).save(any());
    }

    @Test
    void shouldThrowException_When_PayerNotFound() {
        SettleDebtRequest request = new SettleDebtRequest(bob.getId(), new BigDecimal("50.00"));

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settlementService.settleDebt(group.getId(), request, "unknown@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldThrowException_When_PayeeNotFound() {
        SettleDebtRequest request = new SettleDebtRequest(UUID.randomUUID(), new BigDecimal("50.00"));

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(userRepository.findById(request.payeeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settlementService.settleDebt(group.getId(), request, "ana@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}