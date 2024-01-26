package stone.lunchtime.dto;

import java.io.Serial;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represent a set of week and day for availability.
 *
 */
public class AvailableForWeeksAndDays extends AbstractDto {
	@Serial
	private static final long serialVersionUID = 1L;
	private Set<WeekAndDay> values;

	/**
	 * Constructor of the object.
	 */
	public AvailableForWeeksAndDays() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param aJsonString   the json representation
	 * @param jsonObjMapper the Jackson mapper
	 * @throws JacksonException is string is not proper json
	 */
	public AvailableForWeeksAndDays(String aJsonString, ObjectMapper jsonObjMapper) throws JacksonException {
		super();
		if (aJsonString != null && jsonObjMapper != null) {
			var aad = jsonObjMapper.readValue(aJsonString, AvailableForWeeksAndDays.class);
			this.values = aad.values;
		}
	}

	/**
	 * Gets the property from the object.
	 *
	 * @return the values
	 */
	public Set<WeekAndDay> getValues() {
		return this.values;
	}

	/**
	 * Sets the property into the object.
	 *
	 * @param pValues the values to set
	 */
	public void setValues(Set<WeekAndDay> pValues) {
		this.values = pValues;
	}

	/**
	 * Indicates if available for all weeks
	 *
	 * @return true if available for all weeks.
	 */
	@JsonIgnore
	public boolean allWeeks() {
		return this.values == null || this.values.isEmpty();
	}

	/**
	 * Indicates if available for one week
	 *
	 * @return true if available for one week.
	 */
	@JsonIgnore
	public boolean oneWeek(Integer aWeekNumber) {
		if (this.values == null || this.values.isEmpty()) {
			return true;
		}

		if (aWeekNumber == null) {
			return this.allWeeks();
		}
		for (WeekAndDay weekAndDay : this.values) {
			if (Objects.equals(weekAndDay.getWeek(), aWeekNumber)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if available for one week and all days.
	 *
	 * @return true if available for one week and all days.
	 */
	@JsonIgnore
	public boolean oneWeekAndAllDays(Integer aWeekNumber) {
		if (this.values == null || this.values.isEmpty()) {
			return true;
		}

		if (aWeekNumber == null) {
			return this.allWeeks();
		}
		for (WeekAndDay weekAndDay : this.values) {
			if (Objects.equals(weekAndDay.getWeek(), aWeekNumber) && weekAndDay.getDay() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if available for one week and one day.
	 *
	 * @return true if available for one week and one day.
	 */
	@JsonIgnore
	public boolean oneWeekAndOneDay(Integer aWeekNumber, Integer aDayNumber) {
		if (this.values == null || this.values.isEmpty()) {
			return true;
		}
		if (aWeekNumber == null && aDayNumber == null) {
			return this.allWeeks();
		}
		for (WeekAndDay weekAndDay : this.values) {
			if (Objects.equals(weekAndDay.getWeek(), aWeekNumber)
					&& (Objects.equals(weekAndDay.getDay(), aDayNumber) || weekAndDay.getDay() == null)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a week and a day in the set.
	 *
	 * @param pWeekNb    a week number (1..53 or null)
	 * @param pDayNumber a day number (1..7 or null)
	 * @return true if information was added, false if not
	 */
	@JsonIgnore
	public boolean add(Integer pWeekNb, Integer pDayNumber) {

		if (pWeekNb == null) {
			this.values = null;
			return true;
		}
		if (this.values == null) {
			this.values = new HashSet<>();
		}
		return this.values.add(new WeekAndDay(pWeekNb, pDayNumber));
	}

	/**
	 * Transform this object into Json.
	 *
	 * @param jsonObjMapper json mapper object
	 * @return this object into json
	 * @throws JacksonException if an error occurred in the format
	 */
	@JsonIgnore
	public String toJson(ObjectMapper jsonObjMapper) throws JacksonException {
		if (this.isEmpty()) {
			return null;
		}
		return jsonObjMapper.writeValueAsString(this);
	}

	/**
	 * @see java.util.Set#isEmpty()
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return this.values == null || this.values.isEmpty();
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append("[values=");
		builder.append(this.values);
		builder.append("]");
		return builder.toString();
	}

}
