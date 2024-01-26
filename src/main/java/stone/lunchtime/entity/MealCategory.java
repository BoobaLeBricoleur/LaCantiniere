// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity;

/**
 * Enum for category for meal.
 */
public enum MealCategory {
	UNKNOWN(0),
	/** HORS-D’OEUVRE ET BOUCHÉES. */
	APPETIZERS(1),
	/** ENTRÉES. */
	STARTERS(2),
	/** PLATS PRINCIPAUX. */
	MAIN_DISHES(3),
	/** A-CÔTÉS. */
	OTHERS(4),
	/** DESSERTS. */
	DESSERTS(5),
	/** BRUNCHS ET DÉJEUNERS. */
	BRUNCHS_AND_LUNCHES(6),
	/** SOUPES ET POTAGES. */
	SOUPS(7),
	/** SAUCES ET VINAIGRETTES. */
	SAUCES(8),
	/** BOISSONS ET COCKTAILS. */
	DRINKS(9),
	/** SANDWICHS. */
	SANDWICHES(10),
	/** COLLATIONS. */
	SNACKS(11);

	private final Byte value;

	/**
	 * Constructor of the object.
	 *
	 * @param pValue a value
	 */
	MealCategory(int pValue) {
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
	 * @return the enum. Default is UNKNOWN
	 */
	public static MealCategory fromValue(Number pValue) {
		if (pValue != null) {
			if (pValue.byteValue() == MealCategory.STARTERS.getPrimitiveValue()) {
				return STARTERS;
			}
			if (pValue.byteValue() == MealCategory.APPETIZERS.getPrimitiveValue()) {
				return APPETIZERS;
			}
			if (pValue.byteValue() == MealCategory.MAIN_DISHES.getPrimitiveValue()) {
				return MAIN_DISHES;
			}
			if (pValue.byteValue() == MealCategory.OTHERS.getPrimitiveValue()) {
				return OTHERS;
			}
			if (pValue.byteValue() == MealCategory.DESSERTS.getPrimitiveValue()) {
				return DESSERTS;
			}
			if (pValue.byteValue() == MealCategory.BRUNCHS_AND_LUNCHES.getPrimitiveValue()) {
				return BRUNCHS_AND_LUNCHES;
			}
			if (pValue.byteValue() == MealCategory.SOUPS.getPrimitiveValue()) {
				return SOUPS;
			}
			if (pValue.byteValue() == MealCategory.SAUCES.getPrimitiveValue()) {
				return SAUCES;
			}
			if (pValue.byteValue() == MealCategory.DRINKS.getPrimitiveValue()) {
				return DRINKS;
			}
			if (pValue.byteValue() == MealCategory.SANDWICHES.getPrimitiveValue()) {
				return SANDWICHES;
			}
			if (pValue.byteValue() == MealCategory.SNACKS.getPrimitiveValue()) {
				return SNACKS;
			}
		}
		return UNKNOWN;
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
		var all = MealCategory.values();
		for (MealCategory elm : all) {
			if (elm.getPrimitiveValue() == pValue.byteValue()) {
				return true;
			}
		}
		return false;
	}
}
