// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils methods for entities
 */
public final class EntityUtils {
	private static final Logger LOG = LoggerFactory.getLogger(EntityUtils.class);

	/**
	 * Constructor of the object. <br>
	 */
	private EntityUtils() {
		super();
	}

	/**
	 * Converts a String like "1, 2, 45" into a set of integer.
	 *
	 * @param pValue the string respecting format 1,2,53
	 * @return a set of integer, null if none
	 */
	public static Set<Integer> stringToSet(String pValue) {
		if (pValue == null) {
			return null; // NOSONAR We want to return null
		}
		// Just in case ...
		pValue = pValue.replace('[', ' ');
		pValue = pValue.replace(']', ' ');
		pValue = pValue.replace('}', ' ');
		pValue = pValue.replace('}', ' ');
		pValue = pValue.replace(';', ',');
		pValue = pValue.replace('S', ' ');
		pValue = pValue.replace('s', ' ');
		pValue = pValue.trim();
		if (pValue.isEmpty()) {
			return Collections.emptySet();
		}
		Set<Integer> result = new HashSet<>();
		try {
			var dec = pValue.split(",");
			for (String elm : dec) {
				result.add(Integer.valueOf(elm.trim()));
			}
		} catch (Exception lExp) {
			EntityUtils.LOG.atError().log("stringToSet - Error with values {}", pValue, lExp);
		}
		return result;
	}

	/**
	 * Checks if value is empty or null.
	 *
	 * @param pValue a value
	 * @return this value trimmed or null
	 */
	public static String checkAndClean(String pValue) {
		if (pValue == null || pValue.trim().isEmpty()) {
			return null;
		}
		return pValue.trim();
	}

}
