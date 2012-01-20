/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import java.util.Comparator;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class SetsTest {

	private final class ComparatorUsingAMember implements Comparator<A> {
		@Override
		public int compare(A o1, A o2) {
			return o1.a - o2.a;
		}
	}

	private final class ComparatorUsingAllMembers implements Comparator<A> {
		@Override
		public int compare(A o1, A o2) {
			return Double.valueOf(Math.pow(o1.a - o2.a, 2) + Math.pow(o1.b - o2.b, 2)).intValue();
		}
	}

	
	private static class A {
		public int a;
		public int b;
		
		public A(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof A) {
				A other = (A) obj;
				return Objects.equal(a, other.a)
					&& Objects.equal(b, other.b);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(a, b);
		}
	}
	
	private A a(int a, int b) {
		return new A(a, b);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCol1IsNull() {
		ImmutableList<A> col1 = null;
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test(expected=NullPointerException.class)
	public void testCol2IsNull() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = null;
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test(expected=NullPointerException.class)
	public void testComparatorIsNull() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, null);
	}
	
	@Test
	public void testUsingAMemberCol1Empty() {
		ImmutableList<A> col1 = ImmutableList.of();
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test
	public void testUsingAMemberCol2Empty() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = ImmutableList.of();
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).contains(a(2, 4));
	}
	
	@Test
	public void testUsingAMember() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).containsOnly(a(1, 2));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testUsingAMemberCol1ContainsDuplicates() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(1, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void testUsingAMemberCol2ContainsDuplicates() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2));
		ImmutableList<A> col2 = ImmutableList.of(a(1, 2), a(1, 3));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test
	public void testUsingAMemberEmptyResult() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(1, 3), a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test
	public void testUsingAllMembers() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAllMembers());
		Assertions.assertThat(result).containsOnly(a(1, 2), a(2, 3));
	}

}
