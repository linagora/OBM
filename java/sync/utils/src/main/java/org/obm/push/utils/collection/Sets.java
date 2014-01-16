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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

public class Sets {

	/**
	 * This methods use the given comparator to perform a difference between col1 and col2.
	 * It uses {@link com.google.common.collect.Sets.difference} after wrapping col1 and col2 into TreeSets.
	 */
	public static <E> Set<E> difference(Collection<E> col1, Collection<E> col2, Comparator<E> comparator) {
		Preconditions.checkNotNull(col1, "col1");
		Preconditions.checkNotNull(col2, "col2");
		Preconditions.checkNotNull(comparator, "comparator");
		
		TreeSet<E> set1 = com.google.common.collect.Sets.newTreeSet(comparator);
		set1.addAll(col1);
		checkCollectionNoDuplicateEntry(col1, set1, "col1");
		TreeSet<E> set2 = com.google.common.collect.Sets.newTreeSet(comparator);
		set2.addAll(col2);
		checkCollectionNoDuplicateEntry(col2, set2, "col2");
		return com.google.common.collect.Sets.difference(set1, set2);
	}

	private static <E> void checkCollectionNoDuplicateEntry(Collection<E> col1, TreeSet<E> set1, String name) {
		if (col1.size() != set1.size()) {
			throw new IllegalStateException(name + " must not contain duplicate elements as evaluated by comparator");
		}
	}
	
}
