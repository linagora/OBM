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
package org.obm.push.minig.imap.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;

import com.google.common.collect.ImmutableList;

public class AppendCommandTest {
	
	@Test
	public void testBuildCommandWithGoodLineTermination() {
		String literalData = 
			"Date: Fri, 11 Jan 2013 14:36:00 +0100\r\n" +
			"Subject: 4\r\n" +
			"Message-ID: <rh5pqfydtxrupaxxogntvspf.1357911342934@email.android.com>\r\n" +
			"Importance: normal\r\n" +
			"From: usera@antoine.org\r\n" +
			"To: userb@antoine.org\r\n" +
			"MIME-Version: 1.0\r\n" +
			"Content-Type: multipart/mixed; boundary=\"--_com.android.email_2674861553604\"\r\n";
		Reader literalDataReader = new StringReader(literalData);
		AppendCommand appendCommand = new AppendCommand("INBOX", literalDataReader, new FlagsList(ImmutableList.of(Flag.SEEN)));
		CommandArgument buildCommand = appendCommand.buildCommand();
		
		assertThat(buildCommand.getLiteralData()).isEqualTo(literalData.getBytes());
	}
	
	@Test
	public void testBuildCommandWithBadTermination() {
		String literalData = 
			"Date: Fri, 11 Jan 2013 14:36:00 +0100\r\n" +
			"Subject: 4\r" +
			"Message-ID: <rh5pqfydtxrupaxxogntvspf.1357911342934@email.android.com>\n" +
			"Importance: normal\r\n" +
			"From: usera@antoine.org\r" +
			"To: userb@antoine.org\r" +
			"MIME-Version: 1.0\n" +
			"Content-Type: multipart/mixed; boundary=\"--_com.android.email_2674861553604\"\r\n";
		String expectedLiteralData = "Date: Fri, 11 Jan 2013 14:36:00 +0100\r\n" +
				"Subject: 4\r\n" +
				"Message-ID: <rh5pqfydtxrupaxxogntvspf.1357911342934@email.android.com>\r\n" +
				"Importance: normal\r\n" +
				"From: usera@antoine.org\r\n" +
				"To: userb@antoine.org\r\n" +
				"MIME-Version: 1.0\r\n" +
				"Content-Type: multipart/mixed; boundary=\"--_com.android.email_2674861553604\"\r\n";
		Reader literalDataReader = new StringReader(literalData);
		AppendCommand appendCommand = new AppendCommand("INBOX", literalDataReader, new FlagsList(ImmutableList.of(Flag.SEEN)));
		CommandArgument buildCommand = appendCommand.buildCommand();
		
		assertThat(buildCommand.getLiteralData()).isEqualTo(expectedLiteralData.getBytes());
	}
}
