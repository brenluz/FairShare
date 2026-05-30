package com.brenluz.fairshare.api.dto.request;

import com.brenluz.fairshare.domain.expense.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddExpenseRequest(
        @NotBlank String description,
        @NotNull BigDecimal amount,
        @NotNull SplitType splitType
)
{}
