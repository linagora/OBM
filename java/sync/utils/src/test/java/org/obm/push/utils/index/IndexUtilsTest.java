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
package org.obm.push.utils.index;

import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class IndexUtilsTest {

	private static class IntIndexed implements Indexed<Integer> {
		
		private final int index;

		public IntIndexed(int index) {
			this.index = index;
		}
		
		@Override
		public Integer getIndex() {
			return index;
		}
	}
	
	@Test
	public void testEmptyList() {
		ImmutableList<Indexed<Integer>> emptyList = ImmutableList.of();
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes(emptyList);
		Assertions.assertThat(listIndexes).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullList() {
		@SuppressWarnings("unused")
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes((List<Indexed<Integer>>)null);
	}

	@Test
	public void testSimpleList() {
		ImmutableList<IntIndexed> emptyList = 
				ImmutableList.of(new IntIndexed(0), new IntIndexed(1), new IntIndexed(2));
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes(emptyList);
		Assertions.assertThat(listIndexes).containsOnly(0, 1, 2);
	}
	
	
}
