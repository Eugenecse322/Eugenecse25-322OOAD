/** Cheque: any deposit/withdrawal, working people only, no interest (rule R5). */
public class ChequeAccount extends Account implements Withdrawable {
    private final EmploymentDetails employment;   // mandatory

    public ChequeAccount(String accountNumber, String branch, Customer owner, EmploymentDetails employment) {
        super(accountNumber, branch, checkEmployment(owner, employment));
        this.employment = employment;
    }

    // Runs BEFORE the account is created (same idea as InvestmentAccount).
    private static Customer checkEmployment(Customer owner, EmploymentDetails employment) {
        if (employment == null) {
            throw new IllegalArgumentException(
                    "A cheque account can only be opened for a person who is working");
        }
        return owner;
    }

    public EmploymentDetails getEmployment() { return employment; }

    @Override public boolean withdraw(double amount) {
        return debit(amount, "Withdrawal");
    }

    @Override public String getAccountType() { return "Cheque"; }
}
