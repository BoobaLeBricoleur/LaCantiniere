package stone.lunchtime.utils;

import java.util.Collection;
import java.util.Map;

import stone.lunchtime.service.exception.ParameterException;

/**
 * Class used for rapid test of parameters.
 */
public final class ValidationUtils {

	private ValidationUtils() {
		throw new IllegalAccessError();
	}

	/**
	 * Indicates if the parameter is not null and throws an exception if it is.
	 *
	 * @param parameter the parameter to test
	 * @param pMessage  the message for the exception
	 * @return true if parameter is not null
	 * @throws ParameterException if parameter is null
	 */
	public static boolean isNotNull(Object parameter, String pMessage) {
		if (parameter != null) {
			return true;
		}
		throw new ParameterException(pMessage, "parameter");
	}

	/**
	 * Indicates if the parameter is not empty and throws an exception if it is.
	 *
	 * @param parameter the parameter to test. Can be a String, Collection, Map or
	 *                  Array
	 * @param pMessage  the message for the exception
	 * @return true if parameter is not empty
	 * @throws ParameterException if parameter is empty
	 */
	public static boolean isNotEmpty(Object parameter, String pMessage) {
		if (ValidationUtils.isNotNull(parameter, pMessage)) {
			if (parameter instanceof String s && !s.trim().isEmpty()) {
				return true;
			}
			if (parameter instanceof Collection<?> c && !c.isEmpty()) {
				return true;
			}
			if (parameter instanceof Map<?, ?> m && !m.isEmpty()) {
				return true;
			}
			if (parameter instanceof Object[] t && t.length != 0) {
				return true;
			}
		}
		throw new ParameterException(pMessage, "parameter");
	}

	/**
	 * Indicates if the parameter is strictly positive and throws an exception if it
	 * is not.
	 *
	 * @param parameter the parameter to test.
	 * @param pMessage  the message for the exception
	 * @return true if parameter is strictly positive
	 * @throws ParameterException if parameter not strictly positive
	 */
	public static boolean isStrictlyPositive(Number parameter, String pMessage) {
		if (ValidationUtils.isNotNull(parameter, pMessage) && parameter.doubleValue() > 0D) {
			return true;
		}
		throw new ParameterException(pMessage, "parameter");
	}

	/**
	 * Indicates if the parameter is positive or zero and throws an exception if it
	 * is not.
	 *
	 * @param parameter the parameter to test.
	 * @param pMessage  the message for the exception
	 * @return true if parameter is positive or zero
	 * @throws ParameterException if parameter is not positive or zero
	 */
	public static boolean isPositiveOrZero(Number parameter, String pMessage) {
		if (ValidationUtils.isNotNull(parameter, pMessage) && parameter.doubleValue() >= 0D) {
			return true;
		}
		throw new ParameterException(pMessage, "parameter");
	}

	/**
	 * Indicates if the parameter is strictly between ]min, max[ and throws an
	 * exception if it is not.
	 *
	 * @param parameter the parameter to test.
	 * @param pMessage  the message for the exception
	 * @return true if parameter is strictly between min and max
	 * @throws ParameterException if parameter not strictly between min and max
	 */
	public static boolean isStrictlyBetween(Number parameter, Number pMin, Number pMax, String pMessage) {
		if (ValidationUtils.isNotNull(parameter, pMessage) && parameter.doubleValue() < pMax.doubleValue()
				&& parameter.doubleValue() > pMin.doubleValue()) {
			return true;
		}
		throw new ParameterException(pMessage, "parameter");
	}

	/**
	 * Indicates if the parameter is strictly between [min, max] and throws an
	 * exception if it is not.
	 *
	 * @param parameter the parameter to test.
	 * @param pMessage  the message for the exception
	 * @return true if parameter is strictly between min and max
	 * @throws ParameterException if parameter not strictly between min and max
	 */
	public static boolean isBetween(Number parameter, Number pMin, Number pMax, String pMessage) {
		if (ValidationUtils.isNotNull(parameter, pMessage) && parameter.doubleValue() <= pMax.doubleValue()
				&& parameter.doubleValue() >= pMin.doubleValue()) {
			return true;
		}
		throw new ParameterException(pMessage, "parameter");
	}
}
