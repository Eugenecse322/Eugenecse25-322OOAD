import javafx.scene.Scene;

/**
 * Controller: OpenAccountView talks to this class only.
 * This class validates input and constructs Savings / Investment / Cheque accounts.
 */
public class OpenAccountController {
    private final BankSession session;
    private final Navigator navigator;
    private OpenAccountView view;

    public OpenAccountController(BankSession session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    public Scene show() {
        view = new OpenAccountView(this);
        Scene scene = view.createScene();
        view.suggestNumber(session.nextNumber("Savings"));
        return scene;
    }

    public void accountTypeChanged(String type) {
        view.suggestNumber(session.nextNumber(type));
    }

    public void handleSubmit(String type, String accountNumber, String branch,
                             String openingDeposit, String companyName, String companyAddress) {
        try {
            openAccount(type, accountNumber, branch, openingDeposit, companyName, companyAddress);
            navigator.showDashboard();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void goBack() {
        navigator.showDashboard();
    }

    private void openAccount(String type, String accountNumber, String branch,
                             String openingDeposit, String companyName, String companyAddress) {
        String chosenType = type == null ? "" : type;
        String number = Input.text(accountNumber);
        String chosenBranch = Input.text(branch);
        Customer owner = session.getCurrentCustomer();

        if (number.isEmpty() || chosenBranch.isEmpty()) {
            throw new IllegalArgumentException("Account number and branch are required.");
        }
        if (session.findAccount(number) != null) {
            throw new IllegalArgumentException("That account number is already in use.");
        }

        switch (chosenType) {
            case "Savings" -> new SavingsAccount(number, chosenBranch, owner);
            case "Investment" -> new InvestmentAccount(number, chosenBranch, owner, Input.amount(openingDeposit));
            case "Cheque" -> {
                String company = Input.text(companyName);
                String companyAddr = Input.text(companyAddress);
                if (company.isEmpty() || companyAddr.isEmpty()) {
                    throw new IllegalArgumentException("Cheque accounts need employer name and address.");
                }
                new ChequeAccount(number, chosenBranch, owner, new EmploymentDetails(company, companyAddr));
            }
            default -> throw new IllegalArgumentException("Choose an account type.");
        }
        session.rememberOpened(chosenType);
        session.persist();
    }
}
