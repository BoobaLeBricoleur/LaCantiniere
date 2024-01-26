// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.exception;

import java.io.Serial;
import java.io.Serializable;

/**
 * Exception when entity was not found
 */
public class EntityNotFoundException extends AbstractFunctionalException {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Serializable entityId;

	/**
	 * Constructs a new exception with {@code null} as its detail message. The cause
	 * is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 */
	public EntityNotFoundException() {
		this(null, null);
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is
	 * not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 *
	 * @param pMessage  the detail message. The detail message is saved for later
	 *                  retrieval by the {@link #getMessage()} method.
	 * @param pEntityId id used for searching entity
	 */
	public EntityNotFoundException(String pMessage, Serializable pEntityId) {
		super(pMessage);
		this.entityId = pEntityId;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the entityId value.
	 */
	public Object getEntityId() {
		return this.entityId;
	}

}
