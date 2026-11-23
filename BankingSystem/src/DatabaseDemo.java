/** Demo: save the seeded customer to the H2 database, wipe memory, load it back. */
public class DatabaseDemo {
    public static void main(String[] args) throws Exception {
        BankSession session = new BankSession();
        session.seedDemoCustomer();

        System.out.println("=== Saving to database ===");
        session.saveToDatabase();

        System.out.println("\n=== Clearing memory and loading from database ===");
        session.loadFromDatabase();

        for (Customer customer : session.getCustomers()) {
            System.out.println(customer);
            for (Account account : customer.getAccounts()) {
                System.out.println("  " + account);
                account.printHistory();
            }
        }
        Database.close();
    }
}
