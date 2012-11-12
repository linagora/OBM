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

package org.obm.push.minig.imap;

import java.util.Arrays;
import java.util.Collection;

import org.obm.push.minig.imap.impl.MessageSet;

public class MessageSetTests extends IMAPTestCase {

	private void testParse(Collection<Long> data, String expectedSet, Collection<Long> expectedCollection) {
		String set = MessageSet.asString(data);
		assertEquals(expectedSet, set);
		assertEquals(expectedCollection, MessageSet.asLongCollection(set, data.size()));
	}
	
	public void testParse1() {
		testParse(Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l), "1:3,8:10,12", 
				Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l));
	}

	public void testParse2() {
		testParse(Arrays.asList(8l, 2l, 3l, 4l, 9l, 10l, 12l, 13l), "2:4,8:10,12:13",
				Arrays.asList(2l, 3l, 4l, 8l, 9l, 10l, 12l, 13l));
	}
	
	public void testParse3() {
		testParse(Arrays.asList(1l, 2l), "1:2", Arrays.asList(1l, 2l));
	}
	
	public void testParse4() {
		testParse(Arrays.asList(1l), "1", Arrays.asList(1l));
	}
	
}
