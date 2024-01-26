// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto;

import java.text.DecimalFormat;

/**
 * Utils for Dto.
 */
public interface DtoUtils {

	/**
	 * Checks if value is empty or null.
	 *
	 * @param pValue a value
	 * @return this value trimmed or null
	 */
	static String checkAndClean(String pValue) {
		if (pValue == null || pValue.trim().isEmpty()) {
			return null;
		}
		return pValue.trim();
	}

	/**
	 * Formats a number with like x.xx.
	 *
	 * @param pNumber the number to format
	 * @return the formated number
	 */
	static String formatNumber(Number pNumber) {
		if (pNumber == null || pNumber.doubleValue() == 0) {
			return "0";
		}
		var df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
		return df.format(pNumber.doubleValue());
	}
}
