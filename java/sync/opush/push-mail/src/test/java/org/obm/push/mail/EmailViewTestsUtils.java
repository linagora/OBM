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
package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.conversation.EmailView;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

public class EmailViewTestsUtils {

	public static Map<MSEmailBodyType, EmailView> createPlainTextMap(String string) {
		return ImmutableMap.of(MSEmailBodyType.PlainText, 
				EmailView.builder()
					.uid(1)
					.bodyMimePartData(new ByteArrayInputStream(string.getBytes()))
					.bodyType(MSEmailBodyType.PlainText)
					.envelope(Envelope.builder()
							.build())
					.truncated(false)
					.charset(Charsets.UTF_8.name())
					.build());
	}

	public static Map<MSEmailBodyType, EmailView> createPlainTextMapASCII(String string) {
		return ImmutableMap.of(MSEmailBodyType.PlainText, 
				EmailView.builder()
					.uid(1)
					.bodyMimePartData(new ByteArrayInputStream(string.getBytes()))
					.bodyType(MSEmailBodyType.PlainText)
					.envelope(Envelope.builder()
							.build())
					.truncated(false)
					.charset(Charsets.US_ASCII.name())
					.build());
	}
	
	public static Map<MSEmailBodyType, EmailView> createHtmlMap(String text) {
		return ImmutableMap.of(MSEmailBodyType.HTML, 
				EmailView.builder()
					.uid(1)
					.bodyMimePartData(new ByteArrayInputStream(htmlBold(text).getBytes()))
					.bodyType(MSEmailBodyType.HTML)
					.envelope(Envelope.builder()
							.build())
					.truncated(false)
					.charset(Charsets.UTF_8.name())
					.build());
	}

	public static Map<MSEmailBodyType, EmailView> createPlainTextAndHTMLMap(String text) {
		return ImmutableMap.of(MSEmailBodyType.PlainText, 
				EmailView.builder()
					.uid(1)
					.bodyMimePartData(new ByteArrayInputStream(text.getBytes()))
					.bodyType(MSEmailBodyType.PlainText)
					.envelope(Envelope.builder()
							.build())
					.truncated(false)
					.charset(Charsets.UTF_8.name())
					.build(),
				MSEmailBodyType.HTML, 
				EmailView.builder()
					.uid(1)
					.bodyMimePartData(new ByteArrayInputStream(htmlBold(text).getBytes()))
					.bodyType(MSEmailBodyType.HTML)
					.envelope(Envelope.builder()
							.build())
					.truncated(false)
					.charset(Charsets.UTF_8.name())
					.build());
	}

	private static String htmlBold(String content) {
		return "<b>"+content+"</b>";
	}
}
