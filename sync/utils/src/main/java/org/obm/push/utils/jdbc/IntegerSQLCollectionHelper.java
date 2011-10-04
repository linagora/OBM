package org.obm.push.utils.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class IntegerSQLCollectionHelper extends AbstractSQLCollectionHelper<Integer> {

	public IntegerSQLCollectionHelper(Collection<Integer> values) {
		super(values);
	}

	@Override
	protected void insertValue(Integer value, PreparedStatement statement,
			int parameterCount) throws SQLException {
		statement.setInt(parameterCount, value);
	}
	
	@Override
	protected Integer getZeroValue() {
		return 0;
	}
	
}
