package ru.android.ainege.shoppinglist.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
	public static boolean isPriceValid(String str) {
		String expression = "^[0-9]+[,|.]?[0-9]{0,2}$";
		return regexpValidator(str, expression);
	}

	public static boolean isAmountValid(String str) {
		String expression = "^[0-9]+[,|.]?[0-9]*$";
		return regexpValidator(str, expression);
	}

	private static boolean regexpValidator(String str, String expression) {
		Matcher matcher = Pattern.compile(expression).matcher(str);
		return matcher.matches();
	}
}
