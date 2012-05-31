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

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.minig.imap.Address;
import org.minig.imap.EmailView;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimePart;
import org.obm.DateUtils;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.ms.MSEmail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;


public class MailViewToMSEmailConverterImplTest {

	public static class EmailViewFixture {
		long uid = 1l;
		
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
		
		List<Address> from = ImmutableList.<Address>of(new Address("from@domain.test")); 
		List<Address> to = ImmutableList.<Address>of(new Address("to@domain.test")); 
		List<Address> cc = ImmutableList.<Address>of(new Address("cc@domain.test"));
		String subject = "a subject";
		Date date = DateUtils.date("2004-12-14T22:00:00");

		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
	}

	private EmailViewFixture emailViewFixture;
	
	@Before
	public void setUp() {
		emailViewFixture = new EmailViewFixture();
	}
	
	@Test
	public void testFlagAnsweredPresent() {
		emailViewFixture.answered = true;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isAnswered()).isTrue();
	}

	@Test
	public void testFlagAnsweredNotPresent() {
		emailViewFixture.answered = false;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isAnswered()).isFalse();
	}
	
	@Test
	public void testFlagStarredPresent() {
		emailViewFixture.starred = true;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isStarred()).isTrue();
	}

	@Test
	public void testFlagStarredNotPresent() {
		emailViewFixture.starred = false;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isStarred()).isFalse();
	}
	
	@Test
	public void testFlagReadPresent() {
		emailViewFixture.read = true;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isRead()).isTrue();
	}

	@Test
	public void testFlagReadNotPresent() {
		emailViewFixture.read = false;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.isRead()).isFalse();
	}

	@Test
	public void testUid() {
		emailViewFixture.uid = 54;
		
		MSEmail convertedMSEmail = makeConvertionFromEmailViewFixture();
		
		Assertions.assertThat(convertedMSEmail.getUid()).isEqualTo(54);
	}

	private MSEmail makeConvertionFromEmailViewFixture() {
		return new MailViewToMSEmailConverterImpl().convert(newEmailViewFromFixture());
	}
	
	private EmailView newEmailViewFromFixture() {
		return new EmailView.Builder()
			.uid(emailViewFixture.uid)
			.flags(flagsListFromFixture())
			.envelope(envelopeFromFixture())
			.bodyMimePart(bodyMimePartFromFixture())
			.bodyMimePartData(emailViewFixture.bodyData)
			.build();
	}

	private Collection<Flag> flagsListFromFixture() {
		Builder<Flag> flagsListBuilder = ImmutableSet.<Flag>builder();
		if (emailViewFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (emailViewFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		if (emailViewFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		return flagsListBuilder.build();
	}

	private Envelope envelopeFromFixture() {
		return new Envelope.Builder()
			.from(emailViewFixture.from)
			.to(emailViewFixture.to)
			.cc(emailViewFixture.cc)
			.subject(emailViewFixture.subject)
			.date(emailViewFixture.date)
			.build();
	}

	private IMimePart bodyMimePartFromFixture() {
		return new MimePart();
	}
}
