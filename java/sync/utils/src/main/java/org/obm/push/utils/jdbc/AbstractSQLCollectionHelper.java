/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.utils.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * Abstract helper. It helps with inserting collections as parameters in a
 * {@link PreparedStatement}. Concrete subclasses should implement
 * {@link #insertValue(Object, PreparedStatement, int)}.
 * 
 * @param <V>
 *            the type of the collection elements to insert.
 */
public abstract class AbstractSQLCollectionHelper<V> {
	private Collection<V> values;

	public AbstractSQLCollectionHelper(Collection<V> values) {
		this.values = values;
	}

	/**
	 * Returns a string of placeholders for inserting into an SQL query.
	 * @return a {@link String} with the format "?, ?, ...".
	 */
	public String asPlaceHolders() {
		if (values.isEmpty()) {
			return "?";
		} else {
			List<String> questionMarks = Collections.nCopies(values.size(), "?");
			String placeHolders = Joiner.on(", ").join(questionMarks);
			return placeHolders;
		}
	}

	/**
	 * Inserts each value into a {@link PreparedStatement}.
	 */
	public int insertValues(PreparedStatement st, int parameterCount) throws SQLException {
		if (values.isEmpty()) {
			insertValue(getZeroValue(), st, parameterCount);
			parameterCount++;
		} else {
			for (V value : values) {
				insertValue(value, st, parameterCount);
				parameterCount++;
			}
		}
		return parameterCount;
	}

	protected abstract V getZeroValue();

	protected abstract void insertValue(V value, PreparedStatement statement,
			int parameterCount) throws SQLException;

	public Collection<V> getValues() {
		return values;
	}
}
