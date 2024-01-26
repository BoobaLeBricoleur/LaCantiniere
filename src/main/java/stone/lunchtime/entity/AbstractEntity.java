// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity;

import java.io.Serial;
import java.io.Serializable;

/**
 * Mother of simple entity class.
 */
public abstract class AbstractEntity<K> implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractEntity() {
		super();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the id value.
	 */
	public abstract K getId();

	/**
	 * Sets the attribute value.
	 *
	 * @param pId the new value for id attribute
	 */
	public abstract void setId(K pId);

	@Override
	public int hashCode() {
		if (this.getId() != null) {
			return (this.getClass().getName() + "-" + this.getId()).hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof AbstractEntity<?> o && this.getClass() == obj.getClass()) {
			return o.getId() == this.getId() || o.getId().equals(this.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("{");
		sb.append(this.getClass().getSimpleName());
		sb.append(",id=");
		sb.append(this.getId());
		sb.append("}");
		return sb.toString();
	}

}
