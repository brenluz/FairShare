package com.brenluz.fairshare.api.dto.response;
import java.math.BigDecimal;

public record SplitResponse(
        UserSummary user,
        BigDecimal owedAmount
) {}