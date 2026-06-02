package com.brenluz.fairshare.algorithm;

import com.brenluz.fairshare.domain.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DebtSimplifier {
    public static List<DebtTransaction> simplify(Map<User, BigDecimal> balances){
        List<DebtTransaction> result = new ArrayList<>();

        List<Map.Entry<User, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<User, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<User, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }

        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort(Comparator.comparing(Map.Entry::getValue)); // debtors are negative so ascending = biggest debt first

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<User, BigDecimal> debtor = debtors.getFirst();
            Map.Entry<User, BigDecimal> creditor = creditors.getFirst();

            BigDecimal debtAmount = debtor.getValue().abs();
            BigDecimal creditAmount = creditor.getValue();
            BigDecimal settled = debtAmount.min(creditAmount);

            result.add(new DebtTransaction(debtor.getKey(), creditor.getKey(), settled));

            // Reduce balances by settled amount
            debtor.setValue(debtor.getValue().add(settled));
            creditor.setValue(creditor.getValue().subtract(settled));

            // Remove if fully settled
            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) debtors.removeFirst();
            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) creditors.removeFirst();
        }

        return result;
    }
}
