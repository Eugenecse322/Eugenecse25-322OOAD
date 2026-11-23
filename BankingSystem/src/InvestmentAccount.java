/** Investment: BWP500.00 minimum to open, deposits + withdrawals, 5% monthly interest (rule R4). */
public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {
    public static final double MIN_OPENING_DEPOSIT = 500.00;
    private static final double MONTHLY_RATE = 0.05;   // 5%

    public InvestmentAccount(String accountNumber, String branch, Customer owner, double openingDeposit) {
        super(accountNumber, branch, checkOpeningDeposit(owner, openingDeposit));
        deposit(openingDeposit, "Opening deposit");
    }

    // Runs BEFORE the account is created, so a rejected account never reaches the customer's list.
    private static Customer checkOpeningDeposit(Customer owner, double openingDeposit) {
        if (openingDeposit < MIN_OPENING_DEPOSIT) {
            throw new IllegalArgumentException(
                    "Investment account needs an opening deposit of at least BWP500.00");
        }
        return owner;
    }

    @Override public double getMonthlyRate() { return MONTHLY_RATE; }

    @Override public void payInterest() {
        double interest = Math.round(getBalance() * MONTHLY_RATE * 100) / 100.0;
        if (interest > 0) deposit(interest, "Monthly interest");
    }

    @Override public boolean withdraw(double amount) {
        return debit(amount, "Withdrawal");
    }

    @Override public String getAccountType() { return "Investment"; }
}
