package com.brenluz.fairshare.algorithm;

import com.brenluz.fairshare.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DebtSimplifierTest {

    private User ana;
    private User bob;
    private User maria;
    private User carlos;

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

        maria = User.builder()
                .id(UUID.randomUUID())
                .username("maria")
                .password("mariahashed")
                .email("maria@test.com")
                .build();

        carlos = User.builder()
                .id(UUID.randomUUID())
                .username("carlos")
                .password("carloshashed")
                .email("carlos@test.com")
                .build();
    }

    @Test
    void shouldReturn_TwoTransactions_When_TwoDebtorsOweOneCreditorEqually() {
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(ana, new BigDecimal("200.00"));
        balances.put(bob, new BigDecimal("-100.00"));
        balances.put(maria, new BigDecimal("-100.00"));

        List<DebtTransaction> result = DebtSimplifier.simplify(balances);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(t -> assertThat(t.amount()).isEqualByComparingTo("100.00"));
        assertThat(result).allSatisfy(t -> assertThat(t.to().getUsername()).isEqualTo("ana"));
        assertThat(result).extracting(t -> t.from().getUsername())
                .containsExactlyInAnyOrder("bob", "maria");
    }

    @Test
    void shouldReturnEmptyList_When_AllBalancesAreZero() {
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(ana, BigDecimal.ZERO);
        balances.put(bob, BigDecimal.ZERO);
        balances.put(maria, BigDecimal.ZERO);

        List<DebtTransaction> result = DebtSimplifier.simplify(balances);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturn_OneTransaction_When_TwoPeopleHaveOppositeBalances() {
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(ana, new BigDecimal("100.00"));
        balances.put(bob, new BigDecimal("-100.00"));

        List<DebtTransaction> result = DebtSimplifier.simplify(balances);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().from().getUsername()).isEqualTo("bob");
        assertThat(result.getFirst().to().getUsername()).isEqualTo("ana");
        assertThat(result.getFirst().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldMinimizeTransactions_When_MultipleDebtorsAndCreditors() {
        // Ana paid a lot, Maria paid a bit, Bob and Carlos owe money
        // Without simplification this could be 4 transactions,
        // the algorithm should reduce it to 3
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(ana, new BigDecimal("150.00"));
        balances.put(maria, new BigDecimal("50.00"));
        balances.put(bob, new BigDecimal("-100.00"));
        balances.put(carlos, new BigDecimal("-100.00"));

        List<DebtTransaction> result = DebtSimplifier.simplify(balances);

        assertThat(result).hasSize(3);
        // All transactions must go toward creditors (ana or maria)
        assertThat(result).allSatisfy(t ->
                assertThat(t.to().getUsername()).isIn("ana", "maria"));
        // All transactions must come from debtors (bob or carlos)
        assertThat(result).allSatisfy(t ->
                assertThat(t.from().getUsername()).isIn("bob", "carlos"));
        // Total amount settled must equal total debt
        BigDecimal totalSettled = result.stream()
                .map(DebtTransaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalSettled).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldReturnEmptyList_When_BalancesMapIsEmpty() {
        Map<User, BigDecimal> balances = new HashMap<>();

        List<DebtTransaction> result = DebtSimplifier.simplify(balances);

        assertThat(result).isEmpty();
    }
}