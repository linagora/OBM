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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.mime.MimePartImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


public class MimePartSelectorTest {

	private MimePartSelector mimeMessageSelector;

	@Before
	public void init() {
		mimeMessageSelector = new MimePartSelector();
	}
	
	@Test
	public void testSelectPlainText() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectHtml() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/html").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.HTML)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectRtf() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/rtf").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.RTF)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectMime() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.MIME)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(mimeMessage);
	}

	@Test
	public void testSelectEmptyBodyPreferencesTextPlain() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").build();
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(mimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), ImmutableList.<BodyPreference>of(), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}

	@Test
	public void testSelectEmptyBodyPreferencesTextHtml() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/html").build();
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), ImmutableList.<BodyPreference>of(), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}

	@Test
	public void testSelectNullBodyPreferencesTextHtml() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/html").build();
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), null, mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}
	
	@Test
	public void testSelectEmptyBodyPreferencesApplicationPdf() {
		MimePart mimePart = MimePartImpl.builder().contentType("application/pdf").build();
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), ImmutableList.<BodyPreference>of(), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimeMessage);
	}

	@Test
	public void testSelectNoMatchingMimePart() {
		
		MimeMessage mimeMessage = EasyMock.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null).anyTimes();
	
		replay(mimeMessage);
		FetchInstruction instruction = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), ImmutableList.of(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(instruction.getMimePart()).isSameAs(mimeMessage);
		assertThat(instruction.getBodyType()).isEqualTo(MSEmailBodyType.MIME);
		assertThat(instruction.getTruncation()).isEqualTo(32*1024);
	}
	
	@Test
	public void testSelectSeveralBodyPreferences() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/html").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		
		replay(mimeMessage);
		List<BodyPreference> bodyPreferences = 
				Lists.newArrayList(
						BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.RTF), 
						BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.HTML));
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, mimeMessage);
		verify(mimeMessage);
		
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectSeveralBodyPreferencesReturnMimeMessage() {
		MimeMessage expectedMimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(expectedMimeMessage.getMimePart()).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		replay(expectedMimeMessage);
		List<BodyPreference> bodyPreferences = 
				Lists.newArrayList(
						BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.RTF), 
						BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.HTML), 
						BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.MIME));
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, expectedMimeMessage);
		verify(expectedMimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimeMessage);
	}

	@Test
	public void testSelectLargerThanQueryPreferencesWithAllOrNone() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/html").build();
	
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
		expect(expectedMimePart.getSize()).andReturn(50);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(true).build();
	
		replay(mimeMessage, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), Lists.newArrayList(bodyPreference), mimeMessage);
		verify(mimeMessage, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectSmallerThanQueryPreferencesWithAllOrNone() {
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
		expect(expectedMimePart.getSize()).andReturn(10);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(50).allOrNone(true).build();
	
		replay(mimeMessage, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), Lists.newArrayList(bodyPreference), mimeMessage);
		verify(mimeMessage, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(50);
	}

	@Test
	public void testSelectAllOrNoneWithoutTruncationSize() {
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).allOrNone(true).build();
	
		replay(mimeMessage, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), Lists.newArrayList(bodyPreference), mimeMessage);
		verify(mimeMessage, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isNull();
	}

	@Test
	public void testSelectWithoutAllOrNoneAndTruncationSize() {
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).allOrNone(false).build();
	
		replay(mimeMessage, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), Lists.newArrayList(bodyPreference), mimeMessage);
		verify(mimeMessage, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isNull();
	}

	@Test
	public void testSelectTruncationWithoutAllOrNone() {
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(false).build();
	
		replay(mimeMessage, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), Lists.newArrayList(bodyPreference), mimeMessage);
		verify(mimeMessage, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(10);
	}

	@Test
	public void testSelectTruncatedMimePartSeveralBodyPreferences() {
		MimePart plainTextMimePart = EasyMock.createStrictMock(MimePart.class);
		expect(plainTextMimePart.getSize()).andReturn(50).atLeastOnce();
	
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
		expect(expectedMimePart.getSize()).andReturn(10).atLeastOnce();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(plainTextMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(plainTextMimePart);
	
		BodyPreference rtfBodyPreference = BodyPreference.builder().bodyType(MSEmailBodyType.RTF).build();
		BodyPreference plainTextBodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(true).build();
	
		BodyPreference htmlBodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.HTML).truncationSize(50).allOrNone(true).build();
	
		List<BodyPreference> bodyPreferences = Lists.newArrayList(
				rtfBodyPreference, 
				plainTextBodyPreference, 
				htmlBodyPreference);
			replay(mimeMessage, plainTextMimePart, expectedMimePart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, mimeMessage);
		verify(mimeMessage, plainTextMimePart, expectedMimePart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(50);
	}

	@Test
	public void testSelectPreferencesHtmlOrMimeButTextPlainMessage() {
	
		MimePart expectedMimePart = EasyMock.createStrictMock(MimePart.class);
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		MimePart mimeMessageRawPart = EasyMock.createStrictMock(MimePart.class);
		expect(mimeMessage.getMimePart()).andReturn(mimeMessageRawPart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		replay(mimeMessage, expectedMimePart, mimeMessageRawPart);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), BodyPreferencePolicyUtils.bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.MIME), mimeMessage);
		verify(mimeMessage, expectedMimePart, mimeMessageRawPart);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getMailTransformation()).isEqualTo(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML);
	}
	
	@Test
	public void testSelectPlainTextStrictBodyPreference() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new StrictMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectHtmlStrictBodyPreference() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/html").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new StrictMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.HTML)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectRtfStrictBodyPreference() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/rtf").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new StrictMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.RTF)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectMimeStrictBodyPreference() {
		MimePart expectedMimePart = MimePartImpl.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = EasyMock.createStrictMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(expectedMimePart);
	
		replay(mimeMessage);
		FetchInstruction mimePartSelector = mimeMessageSelector.select(new StrictMatchBodyPreferencePolicy(),
				Lists.newArrayList(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.MIME)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(mimeMessage);
	}

	@Test
	public void testSelectNoMatchingMimePartStrictBodyPreference() {
		
		MimeMessage mimeMessage = EasyMock.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null).anyTimes();
	
		replay(mimeMessage);
		FetchInstruction instruction = mimeMessageSelector.select(new StrictMatchBodyPreferencePolicy(), ImmutableList.of(BodyPreferencePolicyUtils.bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		verify(mimeMessage);
	
		assertThat(instruction).isNull();
	}
	
	private ContentType contentType(String mimeType) {
		return ContentType.builder().contentType(mimeType).build();
	}
}