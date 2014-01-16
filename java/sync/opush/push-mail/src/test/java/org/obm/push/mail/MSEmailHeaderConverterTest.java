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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.utils.UserEmailParserUtils;

import com.google.common.collect.Lists;


public class MSEmailHeaderConverterTest {

	private final static String FROM = "from@obm.lng.org";
	private final static String REPLY_TO = "from@perso.com";
	private final static String TO = "to@obm.lng.org";
	private final static String CC = "cc@obm.lng.org";
	private final static String SUBJECT = "subject";
	private final static String DATE = "2012-05-01T11:00:00Z";
	
	private MSEmailHeaderConverter msEmailHeaderConverter;

	@Before
	public void before() {
		msEmailHeaderConverter = new MSEmailHeaderConverter(new UserEmailParserUtils());
	}
	
	@Test
	public void testMSEmailHeaderConverter() {
		Envelope envelope = Envelope.builder()
			.from(Lists.newArrayList(new Address(FROM)))
			.replyTo(Lists.newArrayList(new Address(REPLY_TO)))
			.to(Lists.newArrayList(new Address(TO)))
			.cc(Lists.newArrayList(new Address(CC)))
			.subject(SUBJECT)
			.date(DateUtils.date(DATE)).build();
		
		MSEmailHeader msEmailHeader = msEmailHeaderConverter.convertToMSEmailHeader(envelope);
		
		Assertions.assertThat(msEmailHeader).isNotNull();
		Assertions.assertThat(msEmailHeader.getFrom()).isEqualTo(Lists.newArrayList(toMSAddress(FROM)));
		Assertions.assertThat(msEmailHeader.getReplyTo()).isEqualTo(Lists.newArrayList(toMSAddress(REPLY_TO)));
		Assertions.assertThat(msEmailHeader.getTo()).isEqualTo(Lists.newArrayList(toMSAddress(TO)));
		Assertions.assertThat(msEmailHeader.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(msEmailHeader.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(msEmailHeader.getDate()).isEqualTo(DateUtils.date(DATE));
	}
	
	@Test
	public void testMSEmailHeaderConverterWithoutFrom() {
		Envelope envelope = Envelope.builder()
		.to(Lists.newArrayList(new Address(TO)))
		.cc(Lists.newArrayList(new Address(CC)))
		.subject(SUBJECT)
		.date(DateUtils.date(DATE)).build();
		
		MSEmailHeader msEmailHeader = msEmailHeaderConverter.convertToMSEmailHeader(envelope);
		
		Assertions.assertThat(msEmailHeader).isNotNull();
		Assertions.assertThat(msEmailHeader.getFrom()).isEqualTo(Lists.newArrayList(new MSAddress("Empty From", "o-push@linagora.com")));
		Assertions.assertThat(msEmailHeader.getTo()).isEqualTo(Lists.newArrayList(toMSAddress(TO)));
		Assertions.assertThat(msEmailHeader.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(msEmailHeader.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(msEmailHeader.getDate()).isEqualTo(DateUtils.date("2012-05-01T11:00:00Z"));
	}
	
	@Test
	public void testMSEmailHeaderConverterWithoutTo() {
		Envelope envelope = Envelope.builder()
		.from(Lists.newArrayList(new Address(FROM)))
		.cc(Lists.newArrayList(new Address(CC)))
		.subject(SUBJECT)
		.date(DateUtils.date(DATE)).build();
		
		MSEmailHeader msEmailHeader = msEmailHeaderConverter.convertToMSEmailHeader(envelope);
		
		Assertions.assertThat(msEmailHeader).isNotNull();
		Assertions.assertThat(msEmailHeader.getFrom()).isEqualTo(Lists.newArrayList(toMSAddress(FROM)));
		Assertions.assertThat(msEmailHeader.getTo()).isEmpty();
		Assertions.assertThat(msEmailHeader.getCc()).isEqualTo(Lists.newArrayList(toMSAddress(CC)));
		Assertions.assertThat(msEmailHeader.getSubject()).isEqualTo(SUBJECT);
		Assertions.assertThat(msEmailHeader.getDate()).isEqualTo(DateUtils.date(DATE));
	}
	
	@Test(expected=NullPointerException.class)
	public void testMSEmailHeaderConverterNullPointer() {
		msEmailHeaderConverter.convertToMSEmailHeader(null);
	}
	
	private MSAddress toMSAddress(String mail) {
		return new MSAddress(null, mail);
	}
}
