package ru.android.ainege.shoppinglist.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
    public static boolean isValid(String str, boolean isPrice) {
        boolean isValid = false;
        String expression;
        if(isPrice) {
            expression = "^[0-9]*[,|.]?[0-9]{0,2}$";
        } else {
            expression = "^[0-9]*[,|.]?[0-9]*$";
        }
        Matcher matcher = Pattern.compile(expression).matcher(str);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
