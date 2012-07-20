package org.obm.push.utils.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class WildcardStringSQLCollectionHelper extends StringSQLCollectionHelper {

	public WildcardStringSQLCollectionHelper(Collection<String> values) {
		super(values);
	}

	@Override
	protected void insertValue(String value, PreparedStatement statement, int parameterCount) throws SQLException {
		super.insertValue('%' + value + '%', statement, parameterCount);
	}
 
}
