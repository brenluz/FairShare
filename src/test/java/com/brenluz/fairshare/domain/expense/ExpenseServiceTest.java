package com.brenluz.fairshare.domain.expense;

import com.brenluz.fairshare.api.dto.request.AddExpenseRequest;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupMember;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseSplitRepository expenseSplitRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

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

        GroupMember memberAna = new GroupMember();
        memberAna.setUser(ana);
        GroupMember memberBob = new GroupMember();
        memberBob.setUser(bob);

        group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Trip to Bahia");
        group.setMembers(List.of(memberAna, memberBob));
    }

    @Test
    void shouldSplitEqually_When_SplitTypeIsEqual() {
        AddExpenseRequest request = new AddExpenseRequest(
                "Dinner", new BigDecimal("100.00"), SplitType.EQUAL, null
        );

        Expense savedExpense = Expense.builder()
                .id(UUID.randomUUID())
                .paidBy(ana)
                .group(group)
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .splitType(SplitType.EQUAL)
                .build();

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.save(any())).thenReturn(savedExpense);
        when(expenseRepository.findById(any())).thenReturn(Optional.of(savedExpense));

        Expense result = expenseService.addExpense(group.getId(), "ana@test.com", request);

        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getSplitType()).isEqualTo(SplitType.EQUAL);
        assertThat(result.getPaidBy().getUsername()).isEqualTo("ana");

        verify(expenseSplitRepository).saveAll(any());
    }

    @Test
    void shouldSplitByPercentage_When_SplitTypeIsPercentage(){
        when(userRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));

        List<AddExpenseRequest.SplitDetail> splits = List.of(
                new AddExpenseRequest.SplitDetail(ana.getId(), new BigDecimal("60.00")),
                new AddExpenseRequest.SplitDetail(bob.getId(), new BigDecimal("40.00"))
        );

        AddExpenseRequest request = new AddExpenseRequest(
                "Dinner", new BigDecimal("100.00"), SplitType.PERCENTAGE, splits
        );

        Expense savedExpense = Expense.builder()
                .id(UUID.randomUUID())
                .paidBy(ana)
                .group(group)
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .splitType(SplitType.PERCENTAGE)
                .build();

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.save(any())).thenReturn(savedExpense);
        when(expenseRepository.findById(any())).thenReturn(Optional.of(savedExpense));

        Expense result = expenseService.addExpense(group.getId(), "ana@test.com", request);

        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getSplitType()).isEqualTo(SplitType.PERCENTAGE);
        assertThat(result.getPaidBy().getUsername()).isEqualTo("ana");

        verify(expenseSplitRepository).saveAll(any());
    }

    @Test
    void shouldSplitByExactAmounts_When_SplitTypeIsExact(){
        when(userRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));

        List<AddExpenseRequest.SplitDetail> splits = List.of(
                new AddExpenseRequest.SplitDetail(ana.getId(), new BigDecimal("60")),
                new AddExpenseRequest.SplitDetail(bob.getId(), new BigDecimal("40"))
        );


        AddExpenseRequest request = new AddExpenseRequest(
                "Dinner", new BigDecimal("100.00"), SplitType.EXACT, splits
        );

        Expense savedExpense = Expense.builder()
                .id(UUID.randomUUID())
                .paidBy(ana)
                .group(group)
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .splitType(SplitType.EXACT)
                .build();

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.save(any())).thenReturn(savedExpense);
        when(expenseRepository.findById(any())).thenReturn(Optional.of(savedExpense));

        Expense result = expenseService.addExpense(group.getId(), "ana@test.com", request);

        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getSplitType()).isEqualTo(SplitType.EXACT);
        assertThat(result.getPaidBy().getUsername()).isEqualTo("ana");

        verify(expenseSplitRepository).saveAll(any());
    }

    @Test
    void shouldThrowException_When_ExactSplitsDoNotSumToTotal() {
        List<AddExpenseRequest.SplitDetail> splits = List.of(
                new AddExpenseRequest.SplitDetail(ana.getId(), new BigDecimal("60.00")),
                new AddExpenseRequest.SplitDetail(bob.getId(), new BigDecimal("30.00"))
        );

        AddExpenseRequest request = new AddExpenseRequest(
                "Dinner", new BigDecimal("100.00"), SplitType.EXACT, splits
        );

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(expenseRepository.save(any())).thenReturn(Expense.builder()
                .id(UUID.randomUUID()).paidBy(ana).group(group)
                .description("Dinner").amount(new BigDecimal("100.00"))
                .splitType(SplitType.EXACT).build());

        assertThatThrownBy(() -> expenseService.addExpense(group.getId(), "ana@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Exact splits must sum");
    }
}