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
package org.obm.sync.utils;


import org.junit.Assert;
import org.junit.Test;

import org.obm.sync.utils.DisplayNameUtils;



public class DisplayNameUtilsTest {
	
	@Test
	public void getDisplayNameNotNullParam() {
		String disp = DisplayNameUtils.getDisplayName("common", "first", "last");
		Assert.assertEquals("common", disp);
	}
	
	@Test
	public void getDisplayNameNullFirstAndEmptyName() {
		String disp = DisplayNameUtils.getDisplayName("common", null, "");
		Assert.assertEquals("common", disp);
	}
	
	@Test
	public void getDisplayNameEmptyFirstAndNullName() {
		String disp = DisplayNameUtils.getDisplayName("common", "", null);
		Assert.assertEquals("common", disp);
	}

	@Test
	public void getDisplayNameNullCommon() {
		String disp = DisplayNameUtils.getDisplayName(null, "first", "last");
		Assert.assertEquals("first last", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommon() {
		String disp = DisplayNameUtils.getDisplayName("", "first", "last");
		Assert.assertEquals("first last", disp);
	}
	
	@Test
	public void getDisplayNameNullCommonAndFirst() {
		String disp = DisplayNameUtils.getDisplayName(null, null, "last");
		Assert.assertEquals("last", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommonAndName() {
		String disp = DisplayNameUtils.getDisplayName("", "", "last");
		Assert.assertEquals("last", disp);
	}
	
	@Test
	public void getDisplayNameNullCommonAndLast() {
		String disp = DisplayNameUtils.getDisplayName(null, "first", "");
		Assert.assertEquals("first", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommonAndLast() {
		String disp = DisplayNameUtils.getDisplayName("", "first", "");
		Assert.assertEquals("first", disp);
	}
	
	@Test
	public void getDisplayNameAllEmpty() {
		String disp = DisplayNameUtils.getDisplayName("", "", "");
		Assert.assertEquals("", disp);
	}
	
	@Test
	public void getDisplayNameAllNull() {
		String disp = DisplayNameUtils.getDisplayName(null, null, null);
		Assert.assertEquals("", disp);
	}
	
}
