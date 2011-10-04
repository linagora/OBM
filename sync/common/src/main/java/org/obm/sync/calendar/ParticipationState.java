package org.obm.sync.calendar;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.obm.sync.base.ObmDbType;

public enum ParticipationState {

	// grr, '-' in java enums not allowed
	NEEDSACTION, ACCEPTED, DECLINED, TENTATIVE, DELEGATED, COMPLETED, INPROGRESS;

	public Object getJdbcObject(ObmDbType type) throws SQLException {
		if (type == ObmDbType.PGSQL) {
			try {
				Object o = Class.forName("org.postgresql.util.PGobject")
						.newInstance();
				Method setType = o.getClass()
						.getMethod("setType", String.class);
				Method setValue = o.getClass().getMethod("setValue",
						String.class);

				setType.invoke(o, "vpartstat");
				setValue.invoke(o, toString());
				return o;
			} catch (Throwable e) {
				throw new SQLException(e.getMessage(), e);
			}
		}
		return toString();
	}

	public static final ParticipationState getValueOf(String s) {
		if ("NEEDS-ACTION".equals(s)) {
			return NEEDSACTION;
		} else if ("IN-PROGRESS".equals(s)) {
			return INPROGRESS;
		} else {
			try {
				return ParticipationState.valueOf(s);
			} catch (IllegalArgumentException iae) {
				return ACCEPTED;
			}
		}
	}

	@Override
	public String toString() {
		if (NEEDSACTION == this) {
			return "NEEDS-ACTION";
		} else if (INPROGRESS == this) {
			return "IN-PROGRESS";
		} else {
			return super.toString();
		}
	}
}
