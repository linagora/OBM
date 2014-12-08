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
package org.obm.push.minig.imap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.EmailReader;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

public class ImapTestUtils {
	
	private static long retrieveLastEmailUid(StoreClient client, String mailbox)
			throws MailException, MailboxNotFoundException, ImapTimeoutException {
		
		client.select(mailbox);
		SearchQuery query = SearchQuery.builder().afterInclusive(DateUtils.getEpochCalendar().getTime()).build();
		MessageSet messages = client.uidSearch(query);
		return Iterables.getLast(messages.asDiscreteValues());
	}

	public static long storeEmailToInbox(StoreClient client, InputStream email)
			throws MailException, MailboxNotFoundException, ImapTimeoutException {
		
		storeInFolder(client, emailReader(email), "INBOX");
		return retrieveLastEmailUid(client, "INBOX");
	}

	public static EmailReader emailReader(InputStream email) {
		return new EmailReader(email);
	}
	
	private static void storeInFolder(StoreClient client, Reader mailContent, String folderName) 
			throws MailException, MailboxNotFoundException, ImapTimeoutException {

		try {
			client.select(folderName);
			client.append(folderName, mailContent, new FlagsList());
		} catch (CommandIOException e) {
			throw new MailException(e);
		}
	}
	
	public static InputStream loadEmail(String name) throws IOException {
		return new ByteArrayInputStream(ByteStreams.toByteArray(ClassLoader.getSystemResourceAsStream("eml/" + name)));
	}
}
