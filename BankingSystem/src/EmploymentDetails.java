/** Company details. A Cheque account cannot exist without one (rule R5). */
public class EmploymentDetails {
    private final String companyName;
    private final String companyAddress;

    public EmploymentDetails(String companyName, String companyAddress) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    public String getCompanyName()    { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
}
