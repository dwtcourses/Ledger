package ledger.user_interface.utils;

/**
 * Created by Tayler How on 11/1/2016.
 */
public class AmountCreditStringConverter extends AAmountStringConverter {

    public String toString(Integer amount) {
        String amountString = "";
        if (amount >= 0) {
            boolean positiveValue = amount >= 0 ? true : false;
            Integer absoluteAmount = Math.abs(amount);
            String absoluteAmountInCents = String.valueOf(absoluteAmount);

            String dollars;
            String cents;
            if (absoluteAmountInCents.length() > 2) {
                dollars = absoluteAmountInCents.substring(0, absoluteAmountInCents.length() - 2);
                cents = absoluteAmountInCents.substring(absoluteAmountInCents.length() - 2, absoluteAmountInCents.length());
            } else if (absoluteAmountInCents.length() == 2) {
                dollars = "0";
                cents = absoluteAmountInCents;
            } else {
                dollars = "0";
                cents = "0" + absoluteAmountInCents;
            }

            amountString = (positiveValue ? "" : "-") + dollars + "." + cents;
        }

        return amountString;
    }
}
