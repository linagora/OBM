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
package org.obm.mail.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import org.minig.imap.command.parser.HeadersParser;
import org.obm.mail.MailboxConnection;
import org.obm.mail.conversation.MailMessage;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.mime.IMimePart;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;


public class MessageFetcherImpl implements MessageFetcher {
	
	@Singleton
	public static class Factory implements MessageFetcher.Factory {

		@Override
		public MessageFetcher create(MailboxConnection mailboxConnection) {
			return new MessageFetcherImpl(mailboxConnection);
		}
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MessageFetcherImpl.class);
	private MailboxConnection mailboxConnection;

	public MessageFetcherImpl(MailboxConnection mailboxConnection) {
		super();
		this.mailboxConnection = mailboxConnection;
	}
	
	private InputStream uidFetchPart(MimeMessage message, String part) {
		return mailboxConnection.uidFetchPart(message.getUid(), part);
	}

	private InputStream uidFetchPart(MimeMessage message, IMimePart mimePart) {
		return mailboxConnection.uidFetchPart(message.getUid(), mimePart.getAddress().getAddress());
	}

	private InputStream uidFetchPart(MailMessage message, IMimePart mimePart) {
		return mailboxConnection.uidFetchPart(message.getUid(), mimePart.getAddress().getAddress());
	}

	@Override
	public IMAPHeaders fetchPartHeaders(MimeMessage message, IMimePart mimePart) throws IOException {
		MimeAddress messageAddress = mimePart.getAddress();
		String part = null;
		if (messageAddress == null) {
			part = "HEADER";
		} else {
			part = messageAddress.getAddress() + ".HEADER";
		}
		InputStream is = uidFetchPart(message, part);
		InputStreamReader reader = new InputStreamReader(is, getHeaderCharsetDecoder(mimePart));
		Map<String, String> rawHeaders = new HeadersParser().parseRawHeaders(reader);
		IMAPHeaders h = new IMAPHeaders();
		h.setRawHeaders(rawHeaders);
		return h;
	}

	/**
	 * Tries to return a suitable {@link Charset} to decode the headers
	 */
	private Charset getHeaderCharsetDecoder(IMimePart part) {
		String encoding = part.getContentTransfertEncoding();
		if (encoding == null) {
			return Charset.forName("utf-8");
		} else if (encoding.equalsIgnoreCase("8bit")) {
			return Charset.forName("iso-8859-1");
		} else {
			try {
				return Charset.forName(encoding);
			} catch (UnsupportedCharsetException uee) {
				logger.debug("illegal charset: " + encoding + ", defaulting to utf-8");
				return Charset.forName("utf-8");
			}
		}
	}
	
	@Override
	public InputStream fetchPart(MimeMessage message, IMimePart mimePart) throws IOException {
		InputStream encodedStream = uidFetchPart(message, mimePart);
		return mimePart.decodeMimeStream(encodedStream);
	}

	@Override
	public InputStream fetchPart(MailMessage message, IMimePart mimePart) throws IOException {
		InputStream encodedStream = uidFetchPart(message, mimePart);
		return mimePart.decodeMimeStream(encodedStream);
	}
	
	
}
