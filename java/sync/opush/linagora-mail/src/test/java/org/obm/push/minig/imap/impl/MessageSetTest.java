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

package org.obm.push.minig.imap.impl;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.obm.push.minig.imap.impl.MessageSet;

public class MessageSetTest {

	@Test
	public void testParseAsString1() {
		String actual = MessageSet.asString(Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l));
		assertThat(actual).isEqualTo("1:3,8:10,12");
	}

	@Test
	public void testParseAsCollection1() {
		Collection<Long> actual = MessageSet.asLongCollection("1:3,8:10,12", 7);
		assertThat(actual).containsExactly(1l, 2l, 3l, 8l, 9l, 10l, 12l);
	}

	
	@Test
	public void testParseAsString2() {
		String actual = MessageSet.asString(Arrays.asList(8l, 2l, 3l, 4l, 9l, 10l, 12l, 13l));
		assertThat(actual).isEqualTo("2:4,8:10,12:13");
	}
	
	@Test
	public void testParseAsCollection2() {
		Collection<Long> actual = MessageSet.asLongCollection("2:4,8:10,12:13", 7);
		assertThat(actual).containsExactly(2l, 3l, 4l, 8l, 9l, 10l, 12l, 13l);
	}
	
	@Test
	public void testParseAsString3() {
		String actual = MessageSet.asString(Arrays.asList(1l, 2l));
		assertThat(actual).isEqualTo("1:2");
	}
	
	@Test
	public void testParseAsCollection3() {
		Collection<Long> actual = MessageSet.asLongCollection("1:2", 2);
		assertThat(actual).containsExactly(1l, 2l);
	}
	
	@Test
	public void testParseAsString4() {
		String actual = MessageSet.asString(Arrays.asList(1l));
		assertThat(actual).isEqualTo("1");
	}
	
	@Test
	public void testParseAsCollection4() {
		Collection<Long> actual = MessageSet.asLongCollection("1", 1);
		assertThat(actual).containsExactly(1l);
	}
	
	
}
