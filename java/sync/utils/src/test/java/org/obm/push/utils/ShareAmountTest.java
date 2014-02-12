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
package org.obm.push.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;


public class ShareAmountTest {

	@Test(expected=IllegalArgumentException.class)
	public void noEntries() {
		ShareAmount.forEntries(Arrays.<String>asList()).amount(12);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullEntries() {
		ShareAmount.forEntries(null).amount(12);
	}
	
	@Test
	public void share2Entries() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b")).amount(100);
		assertThat(actual).containsEntry("a", 50).containsEntry("b", 50).hasSize(2);
	}
	
	@Test
	public void share2EntriesOddAmount() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b")).amount(101);
		assertThat(actual).containsEntry("a", 51).containsEntry("b", 50).hasSize(2);
	}
	
	@Test
	public void share2EntriesAmountZero() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b")).amount(0);
		assertThat(actual).containsEntry("a", 0).containsEntry("b", 0).hasSize(2);
	}
	
	@Test
	public void share2EntriesAmountOne() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b")).amount(1);
		assertThat(actual).containsEntry("a", 1).containsEntry("b", 0).hasSize(2);
	}
	
	@Test
	public void share3EntriesAmountFour() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b", "c")).amount(4);
		assertThat(actual).containsEntry("a", 2).containsEntry("b", 1).containsEntry("c", 1).hasSize(3);
	}
	
	@Test
	public void share3EntriesAmountTwo() {
		Map<String, Integer> actual = ShareAmount.forEntries(Arrays.asList("a", "b", "c")).amount(2);
		assertThat(actual).containsEntry("a", 1).containsEntry("b", 1).containsEntry("c", 0).hasSize(3);
	}
}
