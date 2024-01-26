// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import stone.lunchtime.entity.AbstractEntity;

/**
 * Mother of simple entity class.
 */
@MappedSuperclass
public abstract class AbstractJpaEntity extends AbstractEntity<Integer> {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractJpaEntity() {
		super();
	}

	/**
	 * Constructor of the object.<br>
	 *
	 * @param pId a value for PK
	 */
	protected AbstractJpaEntity(Integer pId) {
		super();
		this.setId(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the id value.
	 */
	@Override
	public Integer getId() {
		return this.id;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pId the new value for id attribute
	 */
	@Override
	public void setId(Integer pId) {
		this.id = pId;
	}

}
