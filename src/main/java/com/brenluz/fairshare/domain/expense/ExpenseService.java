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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Group not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        SplitType splitType = request.splitType();
        List<GroupMember> members = group.getMembers();

        Expense expense = Expense.builder()
                .paidBy(user)
                .group(group)
                .description(request.description())
                .amount(request.amount())
                .splitType(splitType)
                .build();
        expenseRepository.save(expense);

        List<ExpenseSplit> splits;

        if (splitType == SplitType.EQUAL) {
            BigDecimal splitAmount = request.amount()
                    .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
            splits = members.stream()
                    .map(member -> ExpenseSplit.builder()
                            .expense(expense)
                            .user(member.getUser())
                            .owedAmount(splitAmount)
                            .build())
                    .toList();
        }
        else if(splitType == SplitType.PERCENTAGE){
            splits = request.splits().stream().map(detail -> ExpenseSplit.builder()
                    .expense(expense)
                    .user(userRepository.findById(detail.userId())
                            .orElseThrow(() -> new RuntimeException("User not found")))
                    .owedAmount(request.amount()
                            .multiply(detail.value())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .build())
                    .toList();
        }
        else if (splitType == SplitType.EXACT) {
            BigDecimal sum = request.splits().stream()
                    .map(AddExpenseRequest.SplitDetail::value)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(request.amount()) != 0) {
                throw new RuntimeException("Exact splits must sum to total amount: " + request.amount());
            }
            splits = request.splits().stream()
                    .map(detail -> ExpenseSplit.builder()
                            .expense(expense)
                            .user(userRepository.findById(detail.userId())
                                    .orElseThrow(() -> new RuntimeException("User not found")))
                            .owedAmount(detail.value())
                            .build())
                    .toList();

        } else {
            throw new RuntimeException("Unsupported split type");
        }

        expenseSplitRepository.saveAll(splits);

        return expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Transactional(readOnly = true)
    public List<Expense> getGroupExpenses(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));;

        return expenseRepository.findByGroup(group);
    }
}
