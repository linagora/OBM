package fr.aliacom.obm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.obm.push.utils.jdbc.AbstractSQLCollectionHelper;
import org.obm.sync.calendar.EventObmId;

public class EventObmIdSQLCollectionHelper extends AbstractSQLCollectionHelper<EventObmId> {

	public EventObmIdSQLCollectionHelper(Collection<EventObmId> values) {
		super(values);
	}

	@Override
	protected EventObmId getZeroValue() {
		return new EventObmId(0);
	}

	@Override
	protected void insertValue(EventObmId value, PreparedStatement statement, int parameterCount)
			throws SQLException {
		statement.setInt(parameterCount, value.getObmId());
	}

}
