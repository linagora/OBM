/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.dao;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.annotations.database.DatabaseField;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DatabaseTruncationServiceImpl implements DatabaseTruncationService {

	private final DatabaseMetadataService metadataService;

	@Inject
	private DatabaseTruncationServiceImpl(DatabaseMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public <T> T getTruncatingEntity(T entity) throws SQLException {
		if (entity == null) {
			return null;
		}

		try {
			T newInstance = (T) entity.getClass().newInstance();
			PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(entity.getClass());

			for (PropertyDescriptor descriptor : descriptors) {
				Method writeMethod = descriptor.getWriteMethod(), readMethod = descriptor.getReadMethod();

				if (writeMethod != null) {
					String propertyName = descriptor.getName();
					Object value = PropertyUtils.getProperty(entity, propertyName);

					if (readMethod.isAnnotationPresent(DatabaseEntity.class)) {
						if (value instanceof Collection) {
							value = copyEntityCollection(value);
						} else if (value instanceof Map) {
							value = copyEntityMap(value);
						} else {
							value = copyEntity(value);
						}
					} else {
						DatabaseField dbField = readMethod.getAnnotation(DatabaseField.class);

						if (dbField != null && value instanceof String) {
							value = truncate((String) value, dbField.table(), dbField.column());
						}
					}

					PropertyUtils.setProperty(newInstance, propertyName, value);
				}
			}

			return newInstance;
		}
		catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, SQLException.class);

			throw Throwables.propagate(e);
		}
	}

	@Override
	public String truncate(String value, String table, String column) throws SQLException {
		if (value == null) {
			return null;
		}

		Preconditions.checkArgument(table != null, "database table is mandatory");
		Preconditions.checkArgument(column != null, "database column is mandatory");

		int maxLength = metadataService.getTableDescriptionOf(table).getMaxAllowedBytesOf(column);

		if (value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength);
	}

	private Object copyEntity(Object value) throws Exception {
		return getTruncatingEntity(value);
	}

	private Object copyEntityCollection(Object value) throws SQLException {
		if (value instanceof Set) {
			return truncatingCollection((Collection<?>) value, Sets.newHashSet());
		} else if (value instanceof List) {
			return truncatingCollection((Collection<?>) value, Lists.newArrayList());
		}

		return value;
	}

	private Collection<?> truncatingCollection(Collection<?> oldCollection, Collection<Object> newCollection) throws SQLException {
		for (Object entry : oldCollection) {
			newCollection.add(getTruncatingEntity(entry));
		}

		return newCollection;
	}

	private Object copyEntityMap(Object value) throws SQLException {
		Map<Object, Object> map = Maps.newHashMap();

		for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
			map.put(entry.getKey(), getTruncatingEntity(entry.getValue()));
		}

		return map;
	}

}
