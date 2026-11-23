/**
 * INTERFACE: a "contract" for accounts that allow withdrawals.
 * Investment and Cheque accounts implement this. Savings does NOT,
 * so withdrawing from a Savings account will not even compile.
 */
public interface Withdrawable {
    boolean withdraw(double amount);   // true = success, false = refused
}
