package org.obm.push.backend;

import java.util.Date;
import java.util.Set;

import org.obm.push.data.calendarenum.RecurrenceDayOfWeek;
import org.obm.push.data.calendarenum.RecurrenceType;

public class Recurrence {
	
	private Date until;
	private RecurrenceType type;
	private Integer weekOfMonth;
	private Integer monthOfYear;
	private Integer dayOfMonth;
	private Integer occurrences;
	private Integer interval;
	private Set<RecurrenceDayOfWeek> dayOfWeek;
	
	/*Task Attribut*/
	private Date start;
	private Boolean regenerate;
	private Boolean deadOccur;
	
	
	public RecurrenceType getType() {
		return type;
	}
	public void setType(RecurrenceType type) {
		this.type = type;
	}
	public Integer getWeekOfMonth() {
		return weekOfMonth;
	}
	public void setWeekOfMonth(Integer weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}
	public Integer getMonthOfYear() {
		return monthOfYear;
	}
	public void setMonthOfYear(Integer monthOfYear) {
		this.monthOfYear = monthOfYear;
	}
	public Integer getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	public Integer getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(Integer occurrences) {
		this.occurrences = occurrences;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	public Set<RecurrenceDayOfWeek> getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(Set<RecurrenceDayOfWeek> dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public Date getUntil() {
		return until;
	}
	public void setUntil(Date until) {
		this.until = until;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Boolean getRegenerate() {
		return regenerate;
	}
	public void setRegenerate(Boolean regenerate) {
		this.regenerate = regenerate;
	}
	public Boolean getDeadOccur() {
		return deadOccur;
	}
	public void setDeadOccur(Boolean deadOccur) {
		this.deadOccur = deadOccur;
	}
	
	@Override
	public String toString() {
		return "Recurrence [dayOfMonth=" + dayOfMonth + ", dayOfWeek="
				+ dayOfWeek + ", deadOccur=" + deadOccur + ", interval="
				+ interval + ", monthOfYear=" + monthOfYear + ", occurrences="
				+ occurrences + ", regenerate=" + regenerate + ", start="
				+ start + ", type=" + type + ", until=" + until
				+ ", weekOfMonth=" + weekOfMonth + "]";
	}
	
	
}
