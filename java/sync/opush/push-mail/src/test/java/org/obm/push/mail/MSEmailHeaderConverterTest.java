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
package org.obm.push.mail;

import java.util.HashMap;
import java.util.Map;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.minig.imap.IMAPHeaders;
import org.obm.DateUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailHeader;

import com.google.common.collect.Lists;


public class MSEmailHeaderConverterTest {

	private final static String FROM = "from@obm.lng.org";
	private final static String TO = "to@obm.lng.org";
	private final static String CC = "cc@obm.lng.org";
	private final static String BCC = "bcc@obm.lng.org";
	private final static String SUBJECT = "subject";
	private final static String DATE = "Tue May 01 11:00:00 CEST 2012";
	
	private MSEmailHeaderConverter msEmailHeaderConverter;

	@Before
	public void before() {
		msEmailHeaderConverter = new MSEmailHeaderConverter();
	}
	
	@Test
	public void testMSEmailHeaderConverter() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("from", FROM);
		headers.put("to", TO);
		headers.put("cc", CC);
		headers.put("bcc", BCC);
		headers.put("subject", SUBJECT);
		headers.put("date", DATE);
		
		IMAPHeaders imapHeaders = new IMAPHeaders(headers);
		MSEmailHeader convertToMSMeetingRequest = msEmailHeaderConverter.convertToMSEmailHeader(imapHeaders);
		
		Assertions.assertThat(convertToMSMeetingRequest).isNotNull();
		Assertions.assertThat(convertToMSMeetingRequest.getFrom()).isEqualTo(toMSAddress(FROM));
		Assertions.assertThat(convertToMSMeetingRequest.getTo()).isEqualTo(Lists.newArrayList(toMSAddress(TO)));
		Assertions.assertThat(convertToMSMeetingRequest.getBcc()).isEqualTo(Lists.newArrayList(toMSAddress(BCC)));
		Assertions.assertThat(convertToMSMeetingRequest.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(convertToMSMeetingRequest.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(convertToMSMeetingRequest.getDate()).isEqualTo(DateUtils.date("2012-05-01T11:00:00Z"));
	}
	
	@Test
	public void testMSEmailHeaderConverterWithoutFrom() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("to", TO);
		headers.put("cc", CC);
		headers.put("bcc", BCC);
		headers.put("subject", SUBJECT);
		headers.put("date", DATE);
		
		IMAPHeaders imapHeaders = new IMAPHeaders(headers);
		MSEmailHeader convertToMSMeetingRequest = msEmailHeaderConverter.convertToMSEmailHeader(imapHeaders);
		
		Assertions.assertThat(convertToMSMeetingRequest).isNotNull();
		Assertions.assertThat(convertToMSMeetingRequest.getFrom()).isNull();
		Assertions.assertThat(convertToMSMeetingRequest.getTo()).isEqualTo(Lists.newArrayList(toMSAddress(TO)));
		Assertions.assertThat(convertToMSMeetingRequest.getBcc()).isEqualTo(Lists.newArrayList(toMSAddress(BCC)));
		Assertions.assertThat(convertToMSMeetingRequest.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(convertToMSMeetingRequest.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(convertToMSMeetingRequest.getDate()).isEqualTo(DateUtils.date("2012-05-01T11:00:00Z"));
	}
	
	@Test
	public void testMSEmailHeaderConverterWithoutTo() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("from", FROM);
		headers.put("cc", CC);
		headers.put("bcc", BCC);
		headers.put("subject", SUBJECT);
		headers.put("date", DATE);
		
		IMAPHeaders imapHeaders = new IMAPHeaders(headers);
		MSEmailHeader convertToMSMeetingRequest = msEmailHeaderConverter.convertToMSEmailHeader(imapHeaders);
		
		Assertions.assertThat(convertToMSMeetingRequest).isNotNull();
		Assertions.assertThat(convertToMSMeetingRequest.getFrom()).isEqualTo(toMSAddress(FROM));
		Assertions.assertThat(convertToMSMeetingRequest.getTo()).isEmpty();
		Assertions.assertThat(convertToMSMeetingRequest.getBcc()).isEqualTo(Lists.newArrayList(toMSAddress(BCC)));
		Assertions.assertThat(convertToMSMeetingRequest.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(convertToMSMeetingRequest.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(convertToMSMeetingRequest.getDate()).isEqualTo(DateUtils.date("2012-05-01T11:00:00Z"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testMSEmailHeaderConverterNullPointer() {
		msEmailHeaderConverter.convertToMSEmailHeader(null);
	}
	
	private MSAddress toMSAddress(String mail) {
		return new MSAddress(null, mail);
	}
}
