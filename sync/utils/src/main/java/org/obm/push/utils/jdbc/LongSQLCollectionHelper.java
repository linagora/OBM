package org.obm.push.utils.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class LongSQLCollectionHelper extends AbstractSQLCollectionHelper<Long> {

	public LongSQLCollectionHelper(Collection<Long> values) {
		super(values);
	}

	@Override
	protected void insertValue(Long value, PreparedStatement statement,
			int parameterCount) throws SQLException {
		statement.setLong(parameterCount, value);
	}
	
	@Override
	protected Long getZeroValue() {
		return 0L;
	}
	
}
