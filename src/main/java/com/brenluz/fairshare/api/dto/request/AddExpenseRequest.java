package com.brenluz.fairshare.api.dto.request;

import com.brenluz.fairshare.domain.expense.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AddExpenseRequest(
        @NotBlank String description,
        @NotNull BigDecimal amount,
        @NotNull SplitType splitType,
        List<SplitDetail> splits
)
{
    public record SplitDetail(
            @NotNull UUID userId,
            @NotNull BigDecimal value  // percentage OR exact amount depending on splitType
    ) {}
}
