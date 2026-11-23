/** Input checks used by controllers. Views stay free of this. */
public final class Input {
    private Input() {}

    public static String text(String value) {
        return value == null ? "" : value.trim();
    }

    public static double amount(String text) {
        String value = text(text).replace(",", "");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Enter an amount.");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be a number.");
        }
    }
}
