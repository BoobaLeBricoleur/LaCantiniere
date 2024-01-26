// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.entity;

/**
 * Enum for status used with order. <br>
 *
 * CREATED(0), DELIVERED(1), CANCELED(2)
 */
public enum OrderStatus {
	/** Order is created. It can now be paid. */
	CREATED(0),
	/** Order is delivered. It has been paid. */
	DELIVERED(1),
	/** Order is canceled. It cannot do anything. */
	CANCELED(2);

	private final Byte value;

	/**
	 * Constructor of the object.
	 *
	 * @param pValue a value
	 */
	OrderStatus(int pValue) {
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
	 * @return the enum. Default is CANCELED
	 */
	public static OrderStatus fromValue(Number pValue) {
		if (pValue != null) {
			if (pValue.byteValue() == OrderStatus.CREATED.getPrimitiveValue()) {
				return CREATED;
			}
			if (pValue.byteValue() == OrderStatus.DELIVERED.getPrimitiveValue()) {
				return DELIVERED;
			}
		}
		return CANCELED;
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
		var all = OrderStatus.values();
		for (OrderStatus elm : all) {
			if (elm.getPrimitiveValue() == pValue.byteValue()) {
				return true;
			}
		}
		return false;
	}
}
