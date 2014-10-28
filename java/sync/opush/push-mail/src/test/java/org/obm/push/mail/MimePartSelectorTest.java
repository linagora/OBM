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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.expect;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;


public class MimePartSelectorTest {

	private MimePartSelector mimeMessageSelector;
	private IMocksControl control;

	@Before
	public void init() {
		mimeMessageSelector = new MimePartSelector();
		control = createStrictControl();
	}
	
	@Test
	public void testSelectPlainText() {
		MimePart expectedMimePart = MimePart.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(
				Lists.newArrayList(bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectHtml() {
		MimePart expectedMimePart = MimePart.builder().contentType("text/html").build();
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(
				Lists.newArrayList(bodyPreference(MSEmailBodyType.HTML)), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectRtf() {
		MimePart expectedMimePart = MimePart.builder().contentType("text/rtf").build();
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(expectedMimePart);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(
				Lists.newArrayList(bodyPreference(MSEmailBodyType.RTF)), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectMime() {
		MimePart expectedMimePart = MimePart.builder().contentType("text/plain").build();
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(expectedMimePart);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(
				Lists.newArrayList(bodyPreference(MSEmailBodyType.MIME)), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(mimeMessage);
	}

	@Test
	public void testSelectEmptyBodyPreferencesTextPlain() {
		MimePart mimePart = MimePart.builder().contentType("text/plain").build();
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(mimePart);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(ImmutableList.<BodyPreference>of(), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}

	@Test
	public void testSelectEmptyBodyPreferencesTextHtml() {
		MimePart mimePart = MimePart.builder().contentType("text/html").build();
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(ImmutableList.<BodyPreference>of(), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}

	@Test
	public void testSelectNullBodyPreferencesTextHtml() {
		MimePart mimePart = MimePart.builder().contentType("text/html").build();
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(null, mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimePart);
	}
	
	@Test
	public void testSelectEmptyBodyPreferencesApplicationPdf() {
		MimePart mimePart = MimePart.builder().contentType("application/pdf").build();
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(ImmutableList.<BodyPreference>of(), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(mimeMessage);
	}

	@Test
	public void testSelectNoMatchingMimePart() {
		control = createControl();
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null).anyTimes();
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null).anyTimes();
	
		control.replay();
		FetchInstruction instruction = mimeMessageSelector.select(ImmutableList.of(bodyPreference(MSEmailBodyType.PlainText)), mimeMessage);
		control.verify();
	
		assertThat(instruction.getMimePart()).isSameAs(mimeMessage);
		assertThat(instruction.getBodyType()).isEqualTo(MSEmailBodyType.MIME);
		assertThat(instruction.getTruncation()).isEqualTo(32*1024);
	}

	
	@Test
	public void testSelectSeveralBodyPreferences() {
		MimePart expectedMimePart = MimePart.builder().contentType("text/html").build();
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
		
		control.replay();
		List<BodyPreference> bodyPreferences = 
				Lists.newArrayList(
						bodyPreference(MSEmailBodyType.RTF), bodyPreference(MSEmailBodyType.HTML));
		FetchInstruction mimePartSelector = mimeMessageSelector.select(bodyPreferences, mimeMessage);
		control.verify();
		
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectSeveralBodyPreferencesReturnMimeMessage() {
		MimeMessage expectedMimeMessage = control.createMock(MimeMessage.class);
		expect(expectedMimeMessage.getMimePart()).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(expectedMimeMessage.findMainMessage(contentType("text/plain"))).andReturn(null);
	
		control.replay();
		List<BodyPreference> bodyPreferences = 
				Lists.newArrayList(
						bodyPreference(MSEmailBodyType.RTF), 
						bodyPreference(MSEmailBodyType.HTML), 
						bodyPreference(MSEmailBodyType.MIME));
		FetchInstruction mimePartSelector = mimeMessageSelector.select(bodyPreferences, expectedMimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimeMessage);
	}

	@Test
	public void testSelectLargerThanQueryPreferencesWithAllOrNone() {
		MimePart mimePart = MimePart.builder().contentType("text/html").build();
	
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
		expect(expectedMimePart.getSize()).andReturn(50);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(mimePart);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(true).build();
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(Lists.newArrayList(bodyPreference), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isSameAs(expectedMimePart);
	}

	@Test
	public void testSelectSmallerThanQueryPreferencesWithAllOrNone() {
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
		expect(expectedMimePart.getSize()).andReturn(10);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(50).allOrNone(true).build();
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(Lists.newArrayList(bodyPreference), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(50);
	}

	@Test
	public void testSelectAllOrNoneWithoutTruncationSize() {
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).allOrNone(true).build();
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(Lists.newArrayList(bodyPreference), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isNull();
	}

	@Test
	public void testSelectWithoutAllOrNoneAndTruncationSize() {
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).allOrNone(false).build();
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(Lists.newArrayList(bodyPreference), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isNull();
	}

	@Test
	public void testSelectTruncationWithoutAllOrNone() {
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		BodyPreference bodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(false).build();
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(Lists.newArrayList(bodyPreference), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(10);
	}

	@Test
	public void testSelectTruncatedMimePartSeveralBodyPreferences() {
		MimePart plainTextMimePart = control.createMock(MimePart.class);
	
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/rtf"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(plainTextMimePart);
		expect(plainTextMimePart.getSize()).andReturn(50);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(expectedMimePart);
		expect(expectedMimePart.getSize()).andReturn(10);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(plainTextMimePart);
		expect(plainTextMimePart.getSize()).andReturn(50);
		
		BodyPreference rtfBodyPreference = BodyPreference.builder().bodyType(MSEmailBodyType.RTF).build();
		BodyPreference plainTextBodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.PlainText).truncationSize(10).allOrNone(true).build();
	
		BodyPreference htmlBodyPreference = BodyPreference.builder().
				bodyType(MSEmailBodyType.HTML).truncationSize(50).allOrNone(true).build();
	
		List<BodyPreference> bodyPreferences = Lists.newArrayList(
				rtfBodyPreference, 
				plainTextBodyPreference, 
				htmlBodyPreference);
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(bodyPreferences, mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getTruncation()).isEqualTo(50);
	}

	@Test
	public void testSelectPreferencesHtmlOrMimeButTextPlainMessage() {
	
		MimePart expectedMimePart = control.createMock(MimePart.class);
	
		MimeMessage mimeMessage = control.createMock(MimeMessage.class);
		MimePart mimeMessageRawPart = control.createMock(MimePart.class);
		expect(mimeMessage.getMimePart()).andReturn(mimeMessageRawPart);
		expect(mimeMessage.findMainMessage(contentType("text/html"))).andReturn(null);
		expect(mimeMessage.findMainMessage(contentType("text/plain"))).andReturn(expectedMimePart);
	
		control.replay();
		FetchInstruction mimePartSelector = mimeMessageSelector.select(bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.MIME), mimeMessage);
		control.verify();
	
		assertThat(mimePartSelector.getMimePart()).isNotNull().isSameAs(expectedMimePart);
		assertThat(mimePartSelector.getMailTransformation()).isEqualTo(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void selectBetterFitNoFetchInstructionEntry() {
		new MimePartSelector().selectBetterFit(
				ImmutableList.<FetchInstruction>of(), 
				bodyPreferences(MSEmailBodyType.HTML));
	}
	
	@Test
	public void selectBetterFitNoBodyPreferenceEntry() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction onlyFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction actual = 
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(onlyFetchInstruction), 
				bodyPreferences());
		assertThat(actual).isSameAs(onlyFetchInstruction);
	}
	
	@Test
	public void selectBetterFitHtmlAndMimePreferences() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction mimeFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction actual = 
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(htmlFetchInstruction, mimeFetchInstruction),
				bodyPreferences());
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
	
	@Test
	public void selectBetterFitHtmlAndMimePreferencesReverseOrder() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction mimeFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction actual = 
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(mimeFetchInstruction, htmlFetchInstruction),
				bodyPreferences());
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}

	@Test
	public void selectBetterFitRespectPreferencesOrdering() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction plainFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		FetchInstruction rtfFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.RTF).mimePart(mimePart).build();
		FetchInstruction actual =
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(plainFetchInstruction, htmlFetchInstruction, rtfFetchInstruction),
				bodyPreferences(MSEmailBodyType.RTF, MSEmailBodyType.PlainText, MSEmailBodyType.HTML));
		assertThat(actual).isSameAs(rtfFetchInstruction);
	}
	
	@Test
	public void selectBetterFitTransformationIsNotTheBestFit() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction transformedHtmlFetchInstruction = FetchInstruction.builder()
				.bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();

		FetchInstruction actual =
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(transformedHtmlFetchInstruction, htmlFetchInstruction),
				bodyPreferences(MSEmailBodyType.HTML));
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
	
	@Test
	public void selectBetterFitTransformationIsBetterThanFollowingPreferences() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction transformedHtmlFetchInstruction = FetchInstruction.builder()
				.bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();

		FetchInstruction actual =
			new MimePartSelector().selectBetterFit(
				ImmutableList.of(transformedHtmlFetchInstruction, htmlFetchInstruction),
				bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.PlainText));
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
	
	private ContentType contentType(String mimeType) {
		return ContentType.builder().contentType(mimeType).build();
	}

	private ImmutableList<BodyPreference> bodyPreferences(MSEmailBodyType... emailBodyTypes) {
		Builder<BodyPreference> preferences = ImmutableList.builder();
		for (MSEmailBodyType bodyType: emailBodyTypes) {
			preferences.add(bodyPreference(bodyType));
		}
		 return preferences.build();
	}
	
	private BodyPreference bodyPreference(MSEmailBodyType emailBodyType) {
		 return BodyPreference.builder().bodyType(emailBodyType).build();
	}
	
	@Test
	public void bestFitComparatorPreferenceMatched() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction plainInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.PlainText)).compare(plainInstruction, htmlInstruction);
		assertThat(actual).isNegative();
	}
	
	@Test
	public void bestFitComparatorPreferencesOrderingTextThenHtml() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction plainInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.PlainText, MSEmailBodyType.HTML)).compare(plainInstruction, htmlInstruction);
		assertThat(actual).isNegative();
	}

	@Test
	public void bestFitComparatorPreferencesOrderingHtmlThenText() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction htmlInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction plainInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.PlainText)).compare(plainInstruction, htmlInstruction);
		assertThat(actual).isPositive();
	}
	
	@Test
	public void bestFitComparatorMimeAlwaysLast() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction mimeInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction plainInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.MIME, MSEmailBodyType.PlainText)).compare(plainInstruction, mimeInstruction);
		assertThat(actual).isNegative();
	}
	
	@Test
	public void bestFitComparatorMimeAlwaysLastButCanMatch() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction mimeInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction plainInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.MIME)).compare(plainInstruction, mimeInstruction);
		assertThat(actual).isNegative();
	}

	@Test
	public void bestFitComparatorTransformationComesAfter() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction transformedInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.HTML)).compare(transformedInstruction, htmlInstruction);
		assertThat(actual).isPositive();
	}
	
	@Test
	public void bestFitComparatorTransformationComesAfterNextType() {
		control = createNiceControl();
		MimePart mimePart = control.createMock(MimePart.class);
		FetchInstruction transformedInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		int actual = MimePartSelector.betterFitComparator(bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.PlainText)).compare(transformedInstruction, htmlInstruction);
		assertThat(actual).isPositive();
	}
}