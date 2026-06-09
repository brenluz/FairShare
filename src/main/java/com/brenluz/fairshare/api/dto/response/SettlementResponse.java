package com.brenluz.fairshare.api.dto.response;

import com.brenluz.fairshare.domain.settlement.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UserSummary payer,
        UserSummary payee,
        BigDecimal amount,
        LocalDateTime settledAt
) {
    public static SettlementResponse from(Settlement settlement){
        return new SettlementResponse(
                settlement.getId(),
                new UserSummary(
                        settlement.getPayer().getId(),
                        settlement.getPayer().getUsername(),
                        settlement.getPayer().getEmail()
                ),
                new UserSummary(
                        settlement.getPayee().getId(),
                        settlement.getPayee().getUsername(),
                        settlement.getPayee().getEmail()
                ),
                settlement.getAmount(),
                settlement.getSettledAt()
        );
    }
}
