package com.brenluz.fairshare.algorithm;

import com.brenluz.fairshare.domain.user.User;

import java.math.BigDecimal;

public record DebtTransaction(
        User from,
        User to,
        BigDecimal amount
        ) {
}
