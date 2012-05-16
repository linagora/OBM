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
package org.minig.imap;

import java.io.InputStream;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimePart;
import org.obm.DateUtils;
import org.obm.opush.mail.StreamMailTestsUtils;

import com.google.common.collect.Lists;


public class EmailViewTest {


	@Test(expected=IllegalStateException.class)
	public void testUidDefault() {
		new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.build();
	}

	@Test
	public void testUid() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
		
		Assertions.assertThat(emailView.getUid()).isEqualTo(155);
	}
	
	@Test
	public void testUidNegativeValue() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(-115)
			.build();
		
		Assertions.assertThat(emailView.getUid()).isEqualTo(-115);
	}

	@Test(expected=IllegalStateException.class)
	public void testEnvelopeRequired() {
		new EmailView.Builder().envelope(null)
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBodyMimePartRequired() {
		new EmailView.Builder().bodyMimePart(null)
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBodyMimePartDataRequired() {
		new EmailView.Builder().bodyMimePartData(null)
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.uid(155)
			.build();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFlagsAtNull() {
		new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.flags(null);
	}

	@Test
	public void testFlags() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.build();
		
		Assertions.assertThat(emailView.getFlags()).containsOnly(Flag.ANSWERED);
	}

	@Test
	public void testFlagsDefault() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
		
		Assertions.assertThat(emailView.getFlags()).isEmpty();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFlagsDefaultIsImmutable() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
		
		emailView.getFlags().add(Flag.ANSWERED);
	}

	@Test
	public void testFlagsIsNotLinkedToCollectionArg() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.build();
		
		mutableFlagsList.add(Flag.DELETED);
		
		Assertions.assertThat(emailView.getFlags()).containsOnly(Flag.ANSWERED);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFlagsIsImmutable() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePart(anyBodyMimePart())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.build();
		
		emailView.getFlags().add(Flag.DELETED);
	}
	
	private IMimePart anyBodyMimePart() {
		return new MimePart();
	}
	
	private InputStream anyBodyMimePartData() {
		return StreamMailTestsUtils.newInputStreamFromString("data test");
	}

	private Envelope anyEnvelope() {
		Envelope envelope = new Envelope.Builder()
			.from(Lists.newArrayList(new Address("from@domain.org")))
			.to(Lists.newArrayList(new Address("to@domain.org")))
			.subject("subject")
			.date(DateUtils.date("2004-12-14T22:00:00"))
			.build();
		return envelope;
	}
}
