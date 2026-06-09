package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.algorithm.DebtTransaction;

import java.math.BigDecimal;

public record DebtTransactionResponse(
        UserSummary from,
        UserSummary to,
        BigDecimal amount
) {
    public static DebtTransactionResponse from(DebtTransaction debtTransaction){
        return new DebtTransactionResponse(
                new UserSummary(
                        debtTransaction.from().getId(),
                        debtTransaction.from().getUsername(),
                        debtTransaction.from().getEmail()
                ),
                new UserSummary(
                        debtTransaction.to().getId(),
                        debtTransaction.to().getUsername(),
                        debtTransaction.to().getEmail()
                ),
                debtTransaction.amount()
        );
    }
}
