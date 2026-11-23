/** Savings: deposits only (NO withdrawals), 0.05% interest per month (rule R3). */
public class SavingsAccount extends Account implements InterestBearing {
    private static final double MONTHLY_RATE = 0.0005;   // 0.05%

    public SavingsAccount(String accountNumber, String branch, Customer owner) {
        super(accountNumber, branch, owner);
    }

    @Override public double getMonthlyRate() { return MONTHLY_RATE; }

    @Override public void payInterest() {
        double interest = Math.round(getBalance() * MONTHLY_RATE * 100) / 100.0; // 2 decimals
        if (interest > 0) deposit(interest, "Monthly interest");
    }

    @Override public String getAccountType() { return "Savings"; }
    // Withdrawable is NOT implemented => withdrawals are impossible by design.
}
