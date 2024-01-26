// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.entity;

/**
 * User enum for sex.
 */
public enum Sex {
	/** User is a Man. */
	MAN(0),
	/** User is a Woman. */
	WOMAN(1),
	/** User is not a Man nor a Woman. */
	OTHER(2);

	private final Byte value;

	/**
	 * Constructor of the object.
	 *
	 * @param pValue a value
	 */
	Sex(int pValue) {
		this.value = (byte) pValue;
	}

	/**
	 * Gets the value for this enum
	 *
	 * @return the value for this enum
	 */
	public final Byte getValue() {
		return this.value;
	}

	/**
	 * Gets the primitive value for this enum
	 *
	 * @return the primitive value for this enum
	 */
	public final byte getPrimitiveValue() {
		return this.value.byteValue();
	}

	/**
	 * Transform a value into an enum
	 *
	 * @param pValue a value
	 * @return the enum. Default is OTHER
	 */
	public static Sex fromValue(Number pValue) {
		if (pValue != null) {
			if (pValue.byteValue() == MAN.getPrimitiveValue()) {
				return MAN;
			}
			if (pValue.byteValue() == WOMAN.getPrimitiveValue()) {
				return WOMAN;
			}
		}
		return OTHER;
	}

	/**
	 * Checks if value is in supported enum values
	 *
	 * @param pValue a value
	 * @return true this value is in supported enum value
	 */
	public static boolean inRange(Number pValue) {
		if (pValue == null) {
			return false;
		}
		var all = Sex.values();
		for (Sex elm : all) {
			if (elm.getPrimitiveValue() == pValue.byteValue()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMan(Number aValue) {
		return aValue != null && aValue.byteValue() == Sex.MAN.getPrimitiveValue();
	}

	public static boolean isWoman(Number aValue) {
		return aValue != null && aValue.byteValue() == Sex.WOMAN.getPrimitiveValue();
	}

	public static boolean isOther(Number aValue) {
		return aValue != null && aValue.byteValue() == Sex.OTHER.getPrimitiveValue();
	}

	public static boolean isMan(Sex aValue) {
		return Sex.MAN == aValue;
	}

	public static boolean isWoman(Sex aValue) {
		return Sex.WOMAN == aValue;
	}

	public static boolean isOther(Sex aValue) {
		return Sex.OTHER == aValue;
	}

}
