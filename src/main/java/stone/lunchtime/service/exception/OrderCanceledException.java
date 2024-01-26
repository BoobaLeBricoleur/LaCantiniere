// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.exception;

import java.io.Serial;

/**
 * Status exception
 */
public class OrderCanceledException extends AbstractFunctionalException {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception with {@code null} as its detail message. The cause
	 * is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 */
	public OrderCanceledException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is
	 * not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 *
	 * @param pMessage the detail message. The detail message is saved for later
	 *                 retrieval by the {@link #getMessage()} method.
	 */
	public OrderCanceledException(String pMessage) {
		super(pMessage);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains
	 * the class and detail message of <tt>cause</tt>). This constructor is useful
	 * for exceptions that are little more than wrappers for other throwables (for
	 * example, {@link java.security.PrivilegedActionException}).
	 *
	 * @param pCause the cause (which is saved for later retrieval by the
	 *               {@link #getCause()} method). (A <tt>null</tt> value is
	 *               permitted, and indicates that the cause is nonexistent or
	 *               unknown.)
	 * @since 1.4
	 */
	public OrderCanceledException(Throwable pCause) {
		super(pCause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 *
	 * @param pMessage the detail message (which is saved for later retrieval by the
	 *                 {@link #getMessage()} method).
	 * @param pCause   the cause (which is saved for later retrieval by the
	 *                 {@link #getCause()} method). (A <tt>null</tt> value is
	 *                 permitted, and indicates that the cause is nonexistent or
	 *                 unknown.)
	 * @since 1.4
	 */
	public OrderCanceledException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

}
