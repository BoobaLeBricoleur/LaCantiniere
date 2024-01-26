// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.entity;

/**
 * Enum for status for user, meal, menu, ingredient.
 */
public enum EntityStatus {
	/** Entity is enabled. */
	ENABLED(0),
	/** Entity is disabled. It cannot do anything until it gets back to enable. */
	DISABLED(1),
	/** Entity is deleted. It cannot do anything. */
	DELETED(2);

	private final Byte value;

	/**
	 * Constructor of the object.
	 *
	 * @param pValue a value
	 */
	EntityStatus(int pValue) {
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
	 * @return the enum. Default is DISABLED
	 */
	public static EntityStatus fromValue(Number pValue) {
		if (pValue != null) {
			if (pValue.byteValue() == EntityStatus.ENABLED.getPrimitiveValue()) {
				return ENABLED;
			}
			if (pValue.byteValue() == EntityStatus.DELETED.getPrimitiveValue()) {
				return DELETED;
			}
		}
		return DISABLED;
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
		var all = EntityStatus.values();
		for (EntityStatus elm : all) {
			if (elm.getPrimitiveValue() == pValue.byteValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if user has the status EntityStatus.ENABLED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.ENABLED
	 */
	public static boolean isEnabled(Byte aValue) {
		return EntityStatus.ENABLED.getValue().equals(aValue);
	}

	/**
	 * Indicates if user has the status EntityStatus.DELETED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.DELETED
	 */
	public static boolean isDeleted(Byte aValue) {
		return EntityStatus.DELETED.getValue().equals(aValue);
	}

	/**
	 * Indicates if user has the status EntityStatus.DISABLED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.DISABLED
	 */
	public static boolean isDisabled(Byte aValue) {
		return EntityStatus.DISABLED.getValue().equals(aValue);
	}

	/**
	 * Indicates if user has the status EntityStatus.ENABLED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.ENABLED
	 */
	public static boolean isEnabled(EntityStatus aValue) {
		return EntityStatus.ENABLED == aValue;
	}

	/**
	 * Indicates if user has the status EntityStatus.DELETED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.DELETED
	 */
	public static boolean isDeleted(EntityStatus aValue) {
		return EntityStatus.DELETED == aValue;
	}

	/**
	 * Indicates if user has the status EntityStatus.DISABLED
	 *
	 * @param aValue a value
	 * @return true if user has status EntityStatus.DISABLED
	 */
	public static boolean isDisabled(EntityStatus aValue) {
		return EntityStatus.DISABLED == aValue;
	}
}
