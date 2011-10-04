package org.obm.sync.calendar;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.obm.sync.base.ObmDbType;

public enum ParticipationRole {

	CHAIR, REQ, OPT, NON;
	public Object getJdbcObject(ObmDbType type) throws SQLException {
		if (type == ObmDbType.PGSQL) {
			try {
				Object o = Class.forName("org.postgresql.util.PGobject")
						.newInstance();
				Method setType = o.getClass()
						.getMethod("setType", String.class);
				Method setValue = o.getClass().getMethod("setValue",
						String.class);

				setType.invoke(o, "vrole");
				setValue.invoke(o, toString());
				return o;
			} catch (Throwable e) {
				throw new SQLException(e.getMessage(), e);
			}
		}
		return toString();
	}
}
