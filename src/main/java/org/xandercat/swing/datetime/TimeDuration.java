package org.xandercat.swing.datetime;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Class to represent a rough time duration in days, weeks, months, or years.
 * 
 * @author Scott Arnold
 */
public class TimeDuration implements Serializable {

	private static final long serialVersionUID = 2010061001;
	
	public enum Unit {
		DAY(Calendar.DAY_OF_YEAR, "Days"), 
		WEEK(Calendar.WEEK_OF_YEAR, "Weeks"), 
		MONTH(Calendar.MONTH, "Months"), 
		YEAR(Calendar.YEAR, "Years");
		
		private int calendarField;
		private String displayName;
		private Unit(int calendarField, String displayName) {
			this.calendarField = calendarField;
			this.displayName = displayName;
		}
		public int getCalendarField() {
			return calendarField;
		}
		public String toString() {
			return displayName;
		}
	}
	
	private Unit unit;
	private int value;
	
	/**
	 * Default constructor sets up a TimeDuration set to 0 Days.
	 * 
	 * Default constructor is needed to support XCSerialization.
	 */
	public TimeDuration() {
		unit = Unit.DAY;
	}
	
	public TimeDuration(int value, Unit unit) {
		this.value = value;
		this.unit = unit;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public Calendar getFutureCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(unit.getCalendarField(), value);
		return calendar;
	}
	
	public Calendar getPastCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(unit.getCalendarField(), -value);
		return calendar;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value).append(" ");
		switch (unit) {
		case DAY:
			sb.append((value == 1)? "day" : "days");
			break;
		case WEEK:
			sb.append((value == 1)? "week" : "weeks");
			break;
		case MONTH:
			sb.append((value == 1)? "month" : "months");
			break;
		case YEAR:
			sb.append((value == 1)? "year" : "years");
			break;
		default:
			sb.append(unit.toString());
		}
		return sb.toString();
	}
}
