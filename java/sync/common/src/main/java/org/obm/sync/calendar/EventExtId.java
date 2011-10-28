package org.obm.sync.calendar;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class EventExtId implements Serializable {

	private final String extId;
	
	public EventExtId(String extId) {
		this.extId = extId;
	}
	
	public String getExtId() {
		return extId;
	}

	public String serializeToString() {
		return extId;
	}

	public boolean isDefined() {
		return !Strings.isNullOrEmpty(extId);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EventExtId) {
			EventExtId other = (EventExtId) obj;
			return Objects.equal(extId, other.extId);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(extId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("extId", extId)
			.toString();
	}
	
}
