package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.expense.Expense;
import com.brenluz.fairshare.domain.expense.SplitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        SplitType splitType,
        UserSummary paidBy,
        LocalDateTime createdAt,
        List<SplitResponse> splits
) {
    public static ExpenseResponse from(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getSplitType(),
                new UserSummary(
                        expense.getPaidBy().getId(),
                        expense.getPaidBy().getUsername(),
                        expense.getPaidBy().getEmail()
                ),
                expense.getCreatedAt(),
                expense.getSplits().stream()
                        .map(split -> new SplitResponse(
                                new UserSummary(
                                        split.getUser().getId(),
                                        split.getUser().getUsername(),
                                        split.getUser().getEmail()
                                ),
                                split.getOwedAmount()
                        ))
                        .toList()
        );
    }

}