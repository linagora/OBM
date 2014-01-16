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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.Test;


public class ImmutableFixedSizeListTest {

	@Test(expected=IllegalStateException.class)
	public void noSize() {
		ImmutableFixedSizeList.<String>builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void elementsButNoSize() {
		ImmutableFixedSizeList.<String>builder().add("a").build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void moreElementsThanSize() {
		ImmutableFixedSizeList.<String>builder().size(1).add("a").add("b").build();
	}
	
	@Test
	public void sizeZero() {
		ImmutableFixedSizeList<String> testee = ImmutableFixedSizeList.<String>builder().size(0).build();
		assertThat(testee).hasSize(0).isEmpty();
	}
	
	@Test
	public void sizeEqualsElements() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(2)
					.add("a")
					.add("b")
					.build();
		assertThat(testee).hasSize(2).containsExactly("a", "b");
	}
	
	@Test
	public void sizeGreaterThenElements() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add("a")
					.add("b")
					.build();
		assertThat(testee).hasSize(5).containsExactly("a", "b", null, null, null);
	}
	
	@Test
	public void addNullElements() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add(null)
					.add("a")
					.add(null)
					.add("b")
					.build();
		assertThat(testee).hasSize(5).containsExactly(null, "a", null, "b", null);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void setIsNotSupported() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add("a")
					.build();
		testee.set(0, "c");
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void addIsNotSupported() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add("a")
					.build();
		testee.add("c");
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void clearIsNotSupported() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add("a")
					.build();
		testee.clear();
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void removeOnIteratorIsNotSupported() {
		ImmutableFixedSizeList<String> testee = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add("a")
					.add("b")
					.build();
		Iterator<String> iterator = testee.iterator();
		iterator.next();
		iterator.remove();
	}
	
	@Test
	public void testEquals() {
		ImmutableFixedSizeList<String> testee1 = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add(null)
					.add("a")
					.add(null)
					.add("b")
					.build();
		ImmutableFixedSizeList<String> testee2 = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add(null)
					.add("a")
					.add(null)
					.add("b")
					.add(null)
					.build();
		assertThat(testee1.equals(testee2)).isTrue();
		assertThat(testee1.hashCode()).isEqualTo(testee2.hashCode());
	}
	
	@Test
	public void testNotEquals() {
		ImmutableFixedSizeList<String> testee1 = 
				ImmutableFixedSizeList.<String>builder().size(5)
					.add(null)
					.add("a")
					.add(null)
					.add("b")
					.build();
		ImmutableFixedSizeList<String> testee2 = 
				ImmutableFixedSizeList.<String>builder().size(6)
					.add(null)
					.add("a")
					.add(null)
					.add("b")
					.add(null)
					.build();
		assertThat(testee1.equals(testee2)).isFalse();
		assertThat(testee1.hashCode()).isNotEqualTo(testee2.hashCode());
	}
}
