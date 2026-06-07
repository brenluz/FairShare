package com.brenluz.fairshare.domain.settlement;

import com.brenluz.fairshare.algorithm.DebtSimplifier;
import com.brenluz.fairshare.algorithm.DebtTransaction;
import com.brenluz.fairshare.api.dto.request.SettleDebtRequest;
import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.ExpenseRepository;
import com.brenluz.fairshare.domain.expense.ExpenseSplit;
import com.brenluz.fairshare.domain.group.Group;
import com.brenluz.fairshare.domain.group.GroupRepository;
import com.brenluz.fairshare.domain.user.User;
import com.brenluz.fairshare.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;

    public Map<User, BigDecimal> getBalances(UUID groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        List<Expense> expenses = expenseRepository.findByGroup(group);
        Map<User, BigDecimal> balances = new HashMap<>();


        for(Expense expense : expenses) {
            User payer = expense.getPaidBy();

            balances.merge(payer, expense.getAmount(), BigDecimal::add);

            for (ExpenseSplit split : expense.getSplits()) {
                balances.merge(split.getUser(), split.getOwedAmount().negate(), BigDecimal::add);
            }
        }

        return balances;
    }

    public List<DebtTransaction> getDebtTransactions(UUID groupId) {
        Map<User, BigDecimal> balances = getBalances(groupId);
        return DebtSimplifier.simplify(balances);
    }

    public Settlement settleDebt(UUID groupId, SettleDebtRequest request, String payerEmail) {
        User payer = userRepository.findByEmail(payerEmail).orElseThrow(() -> new RuntimeException("User not found"));
        User payee = userRepository.findById(request.payeeId()).orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .payee(payee)
                .group(group)
                .amount(request.amount())
                .build();

        return settlementRepository.save(settlement);
    }
}
