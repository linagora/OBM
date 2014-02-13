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
package org.obm.push.minig.imap;

import org.junit.Assert;
import org.junit.Test;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class FlagListTest {

	@Test
	public void testFlagDeleteForCommand() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.DELETED);
		
		Assert.assertEquals("(\\Deleted)", fl.asCommandValue());
	}
	
	@Test
	public void testFlagAnsweredForCommand() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.ANSWERED);
		
		Assert.assertEquals("(\\Answered)", fl.asCommandValue());
	}
	
	@Test
	public void testFlagDraftForCommand() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.DRAFT);
		
		Assert.assertEquals("(\\Draft)", fl.asCommandValue());
	}
	
	@Test
	public void testFlagFlaggedForCommand() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.FLAGGED);
		
		Assert.assertEquals("(\\Flagged)", fl.asCommandValue());
	}
	
	@Test
	public void testFlagSeenForCommand() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		
		Assert.assertEquals("(\\Seen)", fl.asCommandValue());
	}
	
	@Test
	public void testFlagAllForCommand() {
		for (int i = 0 ; i <100000;i++){
		FlagsList fl = new FlagsList();
		fl.add(Flag.ANSWERED);
		fl.add(Flag.DELETED);
		fl.add(Flag.DRAFT);
		fl.add(Flag.FLAGGED);
		fl.add(Flag.SEEN);
		
		String asCommandValue = fl.asCommandValue();

		int antiSlashCount = charCount(asCommandValue, '\\');
		int spaceCount =  charCount(asCommandValue, ' ');
		
		Assert.assertEquals(antiSlashCount, 5);
		Assert.assertEquals(spaceCount, 4);
		}
	}

	private int charCount(String asCommandValue, char charToCount) {
		Iterable<String> splitted = Splitter.on(charToCount).split(asCommandValue);
		return Iterables.size(splitted) -1;
	}
}
