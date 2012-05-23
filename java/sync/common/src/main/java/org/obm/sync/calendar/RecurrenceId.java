package org.obm.sync.calendar;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class RecurrenceId implements Serializable{
	private final String recurrenceId;
	
	public RecurrenceId(String recurrenceId) {
		this.recurrenceId = Strings.emptyToNull(recurrenceId);
	}
	
	public String getRecurrenceId() {
		return recurrenceId;
	}
	
	public String serializeToString() {
		return recurrenceId;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof RecurrenceId) {
			RecurrenceId other = (RecurrenceId) obj;
			return Objects.equal(recurrenceId, other.recurrenceId);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(recurrenceId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("recurrenceId", recurrenceId)
			.toString();
	}
}
