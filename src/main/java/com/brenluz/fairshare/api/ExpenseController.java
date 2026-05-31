package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.AddExpenseRequest;
import com.brenluz.fairshare.api.dto.response.ExpenseResponse;
import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{id}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping()
    public ExpenseResponse addExpense(@RequestBody AddExpenseRequest request, @PathVariable UUID id) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();
        Expense expense = expenseService.addExpense(id,email,request);
        return ExpenseResponse.from(expense);
    }

    @GetMapping()
    public List<ExpenseResponse> getGroupExpenses(@PathVariable UUID id){
        return expenseService.getGroupExpenses(id)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }
}
