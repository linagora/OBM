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
package org.obm.sync.calendar;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class RecurrenceDays implements Set<RecurrenceDay>, Serializable {
	
	public static final RecurrenceDays ALL_DAYS = new RecurrenceDays(RecurrenceDay.values());
	
	private final Set<RecurrenceDay> value;

	public RecurrenceDays() {
		value = ImmutableSet.of();
	}

	public RecurrenceDays(RecurrenceDay... recurrenceDays) {
		if (recurrenceDays == null) {
			value = ImmutableSet.of();
		}
		else {
			value = Sets.immutableEnumSet(Arrays.asList(recurrenceDays));
		}
	}

	public RecurrenceDays(Collection<RecurrenceDay> recurrenceDays) {
		if (recurrenceDays == null) {
			value = ImmutableSet.of();
		}
		else {
			value = Sets.immutableEnumSet(recurrenceDays);
		}
	}

	@Override
	public int size() {
		return value.size();
	}

	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return value.contains(o);
	}

	@Override
	public Iterator<RecurrenceDay> iterator() {
		return value.iterator();
	}

	@Override
	public Object[] toArray() {
		return value.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return value.toArray(a);
	}

	@Override
	public boolean add(RecurrenceDay e) {
		return value.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return value.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return value.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends RecurrenceDay> c) {
		return value.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return value.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return value.removeAll(c);
	}

	@Override
	public void clear() {
		value.clear();
	}

	@Override
	public final int hashCode() {
		return value.hashCode();
	}

	@Override
	public final boolean equals(Object object) {
		if (object instanceof RecurrenceDays) {
			RecurrenceDays other = (RecurrenceDays) object;
			return Objects.equal(value, other.value);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
