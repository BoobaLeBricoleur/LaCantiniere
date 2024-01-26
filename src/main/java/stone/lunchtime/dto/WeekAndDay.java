package stone.lunchtime.dto;

import java.io.Serial;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A week and a day in the week.
 */
@JsonPropertyOrder({ "week", "day" })
public class WeekAndDay extends AbstractDto {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(WeekAndDay.class);

	/** Week number **/
	private Integer week;
	/** Day in the week. 1 is Monday and 7 is Sunday. **/
	private Integer day;

	/**
	 * Constructor for this object.
	 */
	public WeekAndDay() {
		super();
	}

	/**
	 * Constructor for this object.
	 *
	 * @param pWeek a week value (1 to 53)
	 * @param pDay  a day value (1 is Monday, 7 is Sunday)
	 */
	public WeekAndDay(Integer pWeek, Integer pDay) {
		super();
		this.setWeek(pWeek);
		this.setDay(pDay);
	}

	/**
	 * Gets the property from the object.
	 *
	 * @return the week (1 to 53, null is for every week)
	 */
	public Integer getWeek() {
		return this.week;
	}

	/**
	 * Sets the property into the object.
	 *
	 * @param pWeek the week to set (null or 1 to 53)
	 */
	public void setWeek(Integer pWeek) {
		if (pWeek != null && (pWeek < 1 || pWeek > 53)) {
			WeekAndDay.LOG.atWarn().log("Week number is between 1 and 53, found {}", pWeek);
		}
		this.week = pWeek;
	}

	/**
	 * Gets the property from the object.
	 *
	 * @return the day (1 is Monday, 7 is Sunday, null for everyday)
	 */
	public Integer getDay() {
		return this.day;
	}

	/**
	 * Sets the property into the object.
	 *
	 * @param pDay the day to set (null or 1 is Monday, 7 is Sunday)
	 */
	public void setDay(Integer pDay) {
		if (pDay != null && (pDay < 1 || pDay > 7)) {
			WeekAndDay.LOG.atWarn().log("Day number is between 1 and 7, found {}", pDay);
		}
		this.day = pDay;
	}

	@Override
	@JsonIgnore
	public int hashCode() {
		return Objects.hash(this.day, this.week);
	}

	@Override
	@JsonIgnore
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		var other = (WeekAndDay) obj;
		return Objects.equals(this.day, other.day) && Objects.equals(this.week, other.week);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append("[w=");
		builder.append(this.week);
		builder.append(",d=");
		builder.append(this.day);
		builder.append("]");
		return builder.toString();
	}

}
