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
package org.obm.push.utils.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Iterables;

public class ClassToInstanceAgregateView<V> implements Iterable<V> {

	private final ArrayList<ClassToInstanceMap<V>> maps;

	public ClassToInstanceAgregateView() {
		maps = new ArrayList<ClassToInstanceMap<V>>();
	}
	
	
	public void addMap(ClassToInstanceMap<V> map) {
		maps.add(map);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> key) {
		T result = null;
		for (ClassToInstanceMap<V> map: maps) {
			V value = map.get(key);
			if (value != null) {
				if (result != null) {
					throw new IllegalStateException("type " + key + " is registered in several maps");
				}
				result = (T) value;
			}
		}
		return result;
	}

	@Override
	public Iterator<V> iterator() {
		List<Iterable<V>> valueIterables = new ArrayList<Iterable<V>>();
		for (ClassToInstanceMap<V> map: maps) {
			valueIterables.add(map.values());
		}
		return Iterables.concat(valueIterables).iterator();
	}
	
}
