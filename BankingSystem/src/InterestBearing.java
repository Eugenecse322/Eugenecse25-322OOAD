/**
 * INTERFACE: a "contract" for accounts that earn interest.
 * Savings and Investment accounts implement this. Cheque does NOT.
 */
public interface InterestBearing {
    double getMonthlyRate();   // e.g. 0.05 means 5%
    void payInterest();        // adds this month's interest to the balance
}
