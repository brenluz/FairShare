package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.user.User;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceResponse(
        UserSummary user,
        BigDecimal balance
) {
    public static BalanceResponse from(Map.Entry<User, BigDecimal> entry) {
        return new BalanceResponse(
                new UserSummary(
                        entry.getKey().getId(),
                        entry.getKey().getUsername(),
                        entry.getKey().getEmail()
                ),
                entry.getValue()
        );
    }
}