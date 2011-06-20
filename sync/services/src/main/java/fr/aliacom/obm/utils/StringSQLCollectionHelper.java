package fr.aliacom.obm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class StringSQLCollectionHelper extends AbstractSQLCollectionHelper<String> {

	public StringSQLCollectionHelper(Collection<String> values) {
		super(values);
	}

	@Override
	protected void insertValue(String value, PreparedStatement statement,
			int parameterCount) throws SQLException {
		statement.setString(parameterCount, value);
	}
	
}
