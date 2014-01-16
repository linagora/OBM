/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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

import java.util.AbstractList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ImmutableFixedSizeList<T>  extends AbstractList<T> implements List<T>{

	public static class Builder<T> {
		
		private final List<T> elements;
		private Integer size;
		
		private Builder() {
			this.elements = Lists.newArrayList();
		}
		
		public Builder<T> size(int size) {
			this.size = size;
			return this;
		}
		
		public Builder<T> add(T element) {
			this.elements.add(element);
			return this;
		}
		
		public Builder<T> addAll(Iterable<T> elements) {
			for (T element: elements) {
				add(element);
			}
			return this;
		}
		
		public ImmutableFixedSizeList<T> build() {
			Preconditions.checkState(size != null);
			Preconditions.checkState(elements.size() <= size);
			for (int i = elements.size(); i < size; ++i) {
				add(null);
			}
			return new ImmutableFixedSizeList<T>(elements);
		}
	}
	
	private List<T> unmodifiableList;
	
	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}
	
	private ImmutableFixedSizeList(List<T> unmodifiableList) {
		this.unmodifiableList = unmodifiableList;
	}

	@Override
	public T get(int index) {
		return unmodifiableList.get(index);
	}

	@Override
	public int size() {
		return unmodifiableList.size();
	}

	@Override
	public String toString() {
		return unmodifiableList.toString();
	}
	
	@Override
	public int hashCode() {
		return unmodifiableList.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return unmodifiableList.equals(o);
	}
}
