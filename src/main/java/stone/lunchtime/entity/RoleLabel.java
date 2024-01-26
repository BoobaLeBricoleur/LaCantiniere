// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity;

/**
 * Role enum for roles.
 */
public enum RoleLabel {
	/** Role User. */
	ROLE_USER,
	/** Role lunch lady. */
	ROLE_LUNCHLADY;

	/**
	 * Transform a value into an enum
	 *
	 * @param pValue a value
	 * @return the enum.
	 */
	public static RoleLabel fromValue(String pValue) {
		if ("ROLE_USER".equals(pValue)) {
			return RoleLabel.ROLE_USER;
		} else if ("ROLE_LUNCHLADY".equals(pValue)) {
			return RoleLabel.ROLE_LUNCHLADY;
		}
		throw new IllegalArgumentException(pValue + " is unkown role !");
	}
}
