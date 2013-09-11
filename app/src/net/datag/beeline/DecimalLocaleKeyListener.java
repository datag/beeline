package net.datag.beeline;

import java.text.DecimalFormatSymbols;

import android.text.InputType;
import android.text.method.DigitsKeyListener;

/**
 * This class is a workaround for Android issue #2626 <https://code.google.com/p/android/issues/detail?id=2626>
 *
 */
public class DecimalLocaleKeyListener extends DigitsKeyListener {
	private final char[] acceptedCharacters = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-',
			new DecimalFormatSymbols().getDecimalSeparator()
	};

	@Override
	protected char[] getAcceptedChars() {
		return acceptedCharacters;
	}

	public int getInputType() {
		return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT;
	}
}
