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

package org.obm.push.minig.imap.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import org.obm.push.mail.bean.MessageSet;

public class ImapMessageSetTest {

	@Test
	public void testParseAsString1() {
		ImapMessageSet imapMessageSet = buildImapMessageSet(Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l));
		String actual = imapMessageSet.asString();
		assertThat(actual).isEqualTo("1:3,8:10,12");
	}

	@Test
	public void testParseAsCollection1() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("1:3,8:10,12");
		Collection<Long> actual = imapMessageSet.asLongCollection();
		assertThat(actual).containsExactly(1l, 2l, 3l, 8l, 9l, 10l, 12l);
	}
	
	@Test
	public void testParseAsString2() {
		ImapMessageSet imapMessageSet = buildImapMessageSet(Arrays.asList(8l, 2l, 3l, 4l, 9l, 10l, 12l, 13l));
		String actual = imapMessageSet.asString();
		assertThat(actual).isEqualTo("2:4,8:10,12:13");
	}
	
	@Test
	public void testParseAsCollection2() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("2:4,8:10,12:13");
		Collection<Long> actual = imapMessageSet.asLongCollection();
		assertThat(actual).containsExactly(2l, 3l, 4l, 8l, 9l, 10l, 12l, 13l);
	}
	
	@Test
	public void testParseAsString3() {
		ImapMessageSet imapMessageSet = buildImapMessageSet(Arrays.asList(1l, 2l));
		String actual = imapMessageSet.asString();
		assertThat(actual).isEqualTo("1:2");
	}
	
	@Test
	public void testParseAsCollection3() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("1:2");
		Collection<Long> actual = imapMessageSet.asLongCollection();
		assertThat(actual).containsExactly(1l, 2l);
	}
	
	@Test
	public void testParseAsString4() {
		ImapMessageSet imapMessageSet = buildImapMessageSet(Arrays.asList(1l));
		String actual = imapMessageSet.asString();
		assertThat(actual).isEqualTo("1");
	}
	
	@Test
	public void testParseAsCollection4() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("1");
		Collection<Long> actual = imapMessageSet.asLongCollection();
		assertThat(actual).containsExactly(1l);
	}

	private ImapMessageSet buildImapMessageSet(List<Long> uids) {
		MessageSet set = MessageSet.builder()
				.addAll(uids)
				.build();
		return ImapMessageSet.wrap(set);
	}
	
	@Test
	public void testSizeZero() {
		ImapMessageSet imapMessageSet = ImapMessageSet.wrap(MessageSet.builder().build());
		assertThat(imapMessageSet.size()).isEqualTo(0);
	}
	
	@Test
	public void testSize() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("2:4,8:10,12:13");
		assertThat(imapMessageSet.size()).isEqualTo(8);
	}
	
	@Test
	public void testIsEmpty() {
		ImapMessageSet imapMessageSet = ImapMessageSet.wrap(MessageSet.builder().build());
		assertThat(imapMessageSet.isEmpty()).isTrue();
	}
	
	@Test
	public void testIsNotEmpty() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("2:4,8:10,12:13");
		assertThat(imapMessageSet.isEmpty()).isFalse();
	}
	
	@Test
	public void testNotContainsEmpty() {
		ImapMessageSet imapMessageSet = ImapMessageSet.wrap(MessageSet.builder().build());
		assertThat(imapMessageSet.contains(0)).isFalse();
	}
	
	@Test
	public void testNotContains() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("2:4,8:10,12:13");
		assertThat(imapMessageSet.contains(0)).isFalse();
	}
	
	@Test
	public void testContains() {
		ImapMessageSet imapMessageSet = ImapMessageSet.parseMessageSet("2:4,8:10,12:13");
		assertThat(imapMessageSet.contains(3)).isTrue();
	}
	
	@Test(expected=NullPointerException.class)
	public void testParseMessageSetNull() {
		ImapMessageSet.parseMessageSet(null);
	}
	
	@Test(expected=NumberFormatException.class)
	public void testParseMessageSetNumber() {
		ImapMessageSet.parseMessageSet("");
	}
	
	@Test
	public void testParseMessageSet() {
		ImapMessageSet messageSet = ImapMessageSet.parseMessageSet("1");
		assertThat(messageSet.asLongCollection()).containsOnly(1l);
	}
	
	@Test
	public void testParseMessageSetRange() {
		ImapMessageSet messageSet = ImapMessageSet.parseMessageSet("1:3");
		assertThat(messageSet.asLongCollection()).containsOnly(1l, 2l, 3l);
	}
	
	@Test
	public void testParseMessageSetMultipleRanges() {
		ImapMessageSet messageSet = ImapMessageSet.parseMessageSet("1:3,8:10");
		assertThat(messageSet.asLongCollection()).containsOnly(1l, 2l, 3l, 8l, 9l, 10l);
	}
	

}
