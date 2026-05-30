package com.brenluz.fairshare.domain.expense;

import com.brenluz.fairshare.api.dto.request.AddExpenseRequest;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupMember;
import com.brenluz.fairshare.domain.group.GroupRepository;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public Expense addExpense(UUID groupId, String email, AddExpenseRequest request){
        if (request.splitType() != SplitType.EQUAL) {
            throw new RuntimeException("Only EQUAL split is supported currently");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Group not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        Expense expense = Expense.builder()
                .paidBy(user)
                .group(group)
                .description(request.description())
                .amount(request.amount())
                .splitType(request.splitType())
                .build();

        List<GroupMember> members = group.getMembers();
        BigDecimal splitAmount = request.amount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        List<ExpenseSplit> splits = members.stream()
                .map(member -> ExpenseSplit.builder()
                        .expense(expense)
                        .user(member.getUser())
                        .owedAmount(splitAmount)
                        .build())
                .toList();

        expenseSplitRepository.saveAll(splits);

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> getGroupExpenses(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));;

        return expenseRepository.findByGroup(group);
    }
}
