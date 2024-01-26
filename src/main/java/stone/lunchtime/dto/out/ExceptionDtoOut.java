// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the exception
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Represents an exception.")
public class ExceptionDtoOut implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionDtoOut.class);

	@Schema(description = "The exception classe name.")
	private final String error;
	@Schema(description = "The exception package name.")
	private final String exceptionPackageName;
	@Schema(description = "The exception message.")
	private String exceptionMessage;
	@Schema(description = "The exception cause.")
	private String exceptionCause;
	@Schema(description = "The parameter name responsible of the exception.")
	private String targetedParameter;
	@Schema(description = "The element id responsible of the exception.")
	private String targetedEntityPk;
	@Schema(description = "The HTTP status, if any. Will use HttpServletResponse.SC_XXX")
	private Integer status;
	@Schema(description = "Date and time. Will format using DateTimeFormatter.ISO_LOCAL_DATE_TIME.")
	private final String timestamp;

	/**
	 * Constructor of the object.
	 */
	public ExceptionDtoOut() {
		super();
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		this.error = this.getClass().getSimpleName();
		this.exceptionPackageName = this.getClass().getPackageName();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pException where to find information for building the DTO
	 */
	public ExceptionDtoOut(Throwable pException) {
		super();
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		this.error = pException.getClass().getSimpleName();
		this.exceptionPackageName = pException.getClass().getPackageName();
		this.setExceptionMessage(pException.getMessage());
		if (pException.getCause() != null) {
			try (var sw = new StringWriter(); var pw = new PrintWriter(sw)) {
				pException.getCause().printStackTrace(pw);
				this.setExceptionCause(sw.toString());
			} catch (Exception e) {
				ExceptionDtoOut.LOG.atError().log("Erreur lors de la recuperation de la cause", e);
			}
		}
		if (pException instanceof ParameterException p) {
			this.targetedParameter = p.getParameterName();
		}
		if (pException instanceof EntityNotFoundException p) {
			this.targetedEntityPk = String.valueOf(p.getEntityId());
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the httpStatus value. Will use HttpServletResponse.SC_XXX
	 */
	public Integer getStatus() {
		return this.status;
	}

	/**
	 * The HTTP status if any.
	 *
	 * @param pHttpStatus Will use HttpServletResponse.SC_XXX
	 */
	public void setStatus(Integer pHttpStatus) {
		this.status = pHttpStatus;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the dateAndTime value.
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the exceptionCause value.
	 */
	public String getExceptionCause() {
		return this.exceptionCause;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pExceptionCause the new value for exceptionCause attribute
	 */
	public void setExceptionCause(String pExceptionCause) {
		this.exceptionCause = pExceptionCause;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the exceptionName value.
	 */
	public String getError() {
		return this.error;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the exceptionPackageName value.
	 */
	public String getExceptionPackageName() {
		return this.exceptionPackageName;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the exceptionMessage value.
	 */
	public String getExceptionMessage() {
		return this.exceptionMessage;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pExceptionMessage the new value for exceptionMessage attribute
	 */
	public void setExceptionMessage(String pExceptionMessage) {
		this.exceptionMessage = pExceptionMessage;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the targetedParameter value.
	 */
	public String getTargetedParameter() {
		return this.targetedParameter;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pTargetedParameter the new value for targetedParameter attribute
	 */
	public void setTargetedParameter(String pTargetedParameter) {
		this.targetedParameter = pTargetedParameter;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the targetedEntityPk value.
	 */
	public String getTargetedEntityPk() {
		return this.targetedEntityPk;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pTargetedEntityPk the new value for targetedEntityPk attribute
	 */
	public void setTargetedEntityPk(String pTargetedEntityPk) {
		this.targetedEntityPk = pTargetedEntityPk;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" {exceptionName=");
		sb.append(this.error);
		sb.append(",exceptionPackageName=");
		sb.append(this.exceptionPackageName);
		sb.append(",exceptionMessage=");
		sb.append(this.exceptionMessage);
		if (this.exceptionCause != null) {
			sb.append(",exceptionCause=");
			sb.append(this.exceptionCause);
		}
		if (this.targetedParameter != null) {
			sb.append(",targetedParameter=");
			sb.append(this.targetedParameter);
		}
		if (this.targetedEntityPk != null) {
			sb.append(",targetedEntityPk=");
			sb.append(this.targetedEntityPk);
		}
		sb.append(",httpStatus=");
		sb.append(this.status);
		sb.append(",dateAndTime=");
		sb.append(this.timestamp);
		sb.append("}");
		return sb.toString();
	}

}
