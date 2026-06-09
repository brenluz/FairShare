package com.brenluz.fairshare.api;

import com.brenluz.fairshare.api.dto.request.SettleDebtRequest;
import com.brenluz.fairshare.api.dto.response.BalanceResponse;
import com.brenluz.fairshare.api.dto.response.DebtTransactionResponse;
import com.brenluz.fairshare.api.dto.response.SettlementResponse;
import com.brenluz.fairshare.domain.settlement.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{id}")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/balances")
    public List<BalanceResponse> getBalances(@PathVariable UUID id){
        return settlementService.getBalances(id)
                .entrySet()
                .stream()
                .map(BalanceResponse::from)
                .toList();
    }

    @GetMapping("/simplify")
    public List<DebtTransactionResponse> getDebtTransactions(@PathVariable UUID id){
        return settlementService.getDebtTransactions(id)
                .stream()
                .map(DebtTransactionResponse::from)
                .toList();
    }

    @PostMapping("/settle")
    public SettlementResponse settleDebt(@RequestBody SettleDebtRequest request, @PathVariable UUID id)  {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return SettlementResponse.from(settlementService.settleDebt(id, request, email));
    }
}
