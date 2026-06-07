package com.brenluz.fairshare.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SettleDebtRequest(
        @NotNull UUID payeeId,
        @NotNull BigDecimal amount
) {}